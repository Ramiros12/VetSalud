/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.controller;

import com.vetsalud.service.ExportService;
import com.vetsalud.service.ExportService.Tabla;
import com.vetsalud.util.ApiException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exportación de listados.
 *   GET /api/export/mascotas/excel      GET /api/export/mascotas/pdf
 *   GET /api/export/propietarios/excel  GET /api/export/propietarios/pdf
 *   GET /api/export/veterinarios/excel  GET /api/export/veterinarios/pdf
 */
@WebServlet("/api/export/*")
public class ExportController extends BaseController {

    private static final String TIPO_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String TIPO_PDF = "application/pdf";

    private final ExportService service = new ExportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String info = req.getPathInfo(); // ej: /mascotas/excel
        String[] partes = (info == null) ? new String[0] : info.replaceFirst("^/", "").split("/");
        if (partes.length != 2) {
            throw ApiException.notFound("Use /api/export/{mascotas|propietarios|veterinarios}/{excel|pdf}");
        }

        Tabla tabla = switch (partes[0]) {
            case "mascotas" -> service.mascotas();
            case "propietarios" -> service.propietarios();
            case "veterinarios" -> service.veterinarios();
            default -> throw ApiException.notFound("Recurso a exportar desconocido: " + partes[0]);
        };

        String marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        byte[] contenido;
        String tipo, extension;
        switch (partes[1]) {
            case "excel" -> { contenido = service.excel(tabla); tipo = TIPO_EXCEL; extension = ".xlsx"; }
            case "pdf" -> { contenido = service.pdf(tabla); tipo = TIPO_PDF; extension = ".pdf"; }
            default -> throw ApiException.notFound("Formato desconocido: " + partes[1] + " (use excel o pdf)");
        }

        // Todo el archivo ya está generado en memoria: si algo falló arriba, el error sale como JSON limpio
        resp.setContentType(tipo);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + tabla.archivoBase() + "_" + marca + extension + "\"");
        resp.setHeader("Cache-Control", "no-store");
        resp.setContentLength(contenido.length);
        resp.getOutputStream().write(contenido);
    }
}
