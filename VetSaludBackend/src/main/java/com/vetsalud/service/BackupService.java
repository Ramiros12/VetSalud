/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.service;

import com.vetsalud.util.ApiException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Copia de seguridad de la base de datos con mysqldump (incluido en XAMPP).
 *
 * >>> AJUSTE: estas constantes deben coincidir con las de META-INF/persistence.xml <<<
 */
public class BackupService {

    private static final String DB_NAME = "vetsalud_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";          // contraseña de MySQL (en XAMPP por defecto está vacía)
    private static final String DB_HOST = "localhost";  // MUY IMPORTANTE
    private static final String DB_PORT = "3306";

    /** Ruta de mysqldump. Déjela en null para que se detecte sola (XAMPP en Windows/Linux o PATH). */
    private static final String MYSQL_PATH = null;      // ej: "C:\\xampp\\mysql\\bin\\mysqldump.exe"

    private static final long TIMEOUT_SEGUNDOS = 120;

    /** Carpeta donde queda guardada cada copia: C:\Users\<usuario>\backups\vetsalud */
    private static File directorioBackups() {
        return new File(System.getProperty("user.home"), "backups" + File.separator + "vetsalud");
    }

    public File generarBackup() throws IOException {
        File directorio = directorioBackups();
        if (!directorio.exists() && !directorio.mkdirs()) {
            throw new ApiException(500, "No se pudo crear la carpeta de copias: " + directorio);
        }

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        File backup = new File(directorio, "backup_" + DB_NAME + "_" + fecha + ".sql");
        File archivoErrores = new File(directorio, "backup_" + fecha + ".err");

        List<String> comando = new ArrayList<>();
        comando.add(resolverMysqldump());
        comando.add("--host=" + DB_HOST);
        comando.add("--port=" + DB_PORT);
        comando.add("--user=" + DB_USER);
        comando.add("--single-transaction");
        comando.add("--routines");
        comando.add("--default-character-set=utf8mb4");
        comando.add("--result-file=" + backup.getAbsolutePath()); // mysqldump escribe el .sql directamente
        comando.add(DB_NAME);

        ProcessBuilder pb = new ProcessBuilder(comando);
        if (!DB_PASS.isEmpty()) {
            pb.environment().put("MYSQL_PWD", DB_PASS); // así la contraseña no aparece en la lista de procesos
        }
        pb.redirectError(archivoErrores); // errores a un archivo temporal: no hay riesgo de bloqueo por buffer lleno

        try {
            Process proceso = pb.start();
            if (!proceso.waitFor(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
                proceso.destroyForcibly();
                backup.delete();
                throw new ApiException(500, "mysqldump tardó más de " + TIMEOUT_SEGUNDOS + " segundos y fue cancelado");
            }
            int codigo = proceso.exitValue();
            if (codigo != 0) {
                backup.delete();
                throw new ApiException(500, "mysqldump falló (código " + codigo + "): " + leerErrores(archivoErrores));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            backup.delete();
            throw new ApiException(500, "Proceso de copia interrumpido");
        } catch (IOException e) {
            // no se pudo ni arrancar el ejecutable
            throw new ApiException(500, "No se pudo ejecutar mysqldump (" + comando.get(0)
                    + "). Si XAMPP está en otra ruta, escríbala en MYSQL_PATH de BackupService.");
        } finally {
            archivoErrores.delete();
        }

        if (!backup.isFile() || backup.length() == 0) {
            throw new ApiException(500, "mysqldump terminó pero no generó el archivo de copia");
        }
        return backup;
    }

    private static String resolverMysqldump() {
        if (MYSQL_PATH != null && !MYSQL_PATH.isBlank()) return MYSQL_PATH;
        String sistema = System.getProperty("vetsalud.mysqldump"); // opcional: -Dvetsalud.mysqldump=ruta
        if (sistema != null && new File(sistema).isFile()) return sistema;
        String[] candidatos = {
            "C:\\xampp\\mysql\\bin\\mysqldump.exe",   // XAMPP en Windows
            "/opt/lampp/bin/mysqldump",               // XAMPP en Linux
            "/usr/bin/mysqldump",
            "/usr/local/bin/mysqldump"
        };
        for (String c : candidatos) {
            if (new File(c).isFile()) return c;
        }
        return "mysqldump"; // último recurso: que esté en el PATH
    }

    private static String leerErrores(File archivo) {
        try {
            String texto = Files.readString(archivo.toPath(), StandardCharsets.UTF_8).trim();
            return texto.length() > 500 ? texto.substring(0, 500) + "..." : texto;
        } catch (IOException e) {
            return "(sin detalle)";
        }
    }
}
