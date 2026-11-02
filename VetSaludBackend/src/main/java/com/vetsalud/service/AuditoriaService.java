/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.service;

import com.vetsalud.dao.AuditoriaDAO;
import com.vetsalud.dto.AuditoriaDto;
import com.vetsalud.entity.Auditoria;
import com.vetsalud.entity.Usuario;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.TransactionManager;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Auditoría: registra quién creó, actualizó o eliminó cada registro y permite consultarlo.
 *
 * Los demás servicios llaman a {@link #registrar(EntityManager, Long, String, String, Long, String)}
 * DENTRO de su propia transacción: o se guardan el cambio y su auditoría juntos, o ninguno de los dos.
 */
public class AuditoriaService {

    // Acciones
    public static final String CREAR = "CREAR";
    public static final String ACTUALIZAR = "ACTUALIZAR";
    public static final String ELIMINAR = "ELIMINAR";

    // Entidades (el filtro del panel usa estos mismos textos)
    public static final String MASCOTA = "Mascota";
    public static final String PROPIETARIO = "Propietario";
    public static final String VETERINARIO = "Veterinario";
    public static final String HISTORIAL = "Historial clínico";
    public static final String USUARIO = "Usuario";
    public static final String ROL = "Rol";

    private static final int LIMITE_POR_DEFECTO = 500;
    private static final int LIMITE_MAXIMO = 5000;
    private static final int MAX_DESCRIPCION = 500;

    /** Registra una acción usando el EntityManager (y la transacción) de quien la llama. */
    public void registrar(EntityManager em, Long idUsuario, String accion, String entidad,
                          Long idRegistro, String descripcion) {
        if (idUsuario == null) {
            throw ApiException.unauthorized("No se pudo identificar al usuario que realiza la acción");
        }
        Usuario usuario = em.find(Usuario.class, idUsuario);
        if (usuario == null) {
            throw ApiException.unauthorized("El usuario de la sesión ya no existe");
        }

        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setUsername(usuario.getUsername());
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setIdRegistro(idRegistro);
        auditoria.setFechaHora(LocalDateTime.now().withNano(0));
        auditoria.setDescripcion(recortar(descripcion));
        new AuditoriaDAO(em).guardar(auditoria);
    }

    /** Versión independiente (abre su propia transacción), para registrar eventos que no cambian datos. */
    public void registrar(Long idUsuario, String accion, String entidad, Long idRegistro, String descripcion) {
        TransactionManager.enTransaccion(em -> {
            registrar(em, idUsuario, accion, entidad, idRegistro, descripcion);
            return null;
        });
    }

    // ---------- consultas ----------
    public List<AuditoriaDto> listarTodos(Integer limite) {
        int tope = normalizar(limite);
        return TransactionManager.soloLectura(em ->
                convertir(new AuditoriaDAO(em).listarTodosConUsuario(tope)));
    }

    public List<AuditoriaDto> listarPorUsuario(Long idUsuario, Integer limite) {
        int tope = normalizar(limite);
        return TransactionManager.soloLectura(em ->
                convertir(new AuditoriaDAO(em).listarPorUsuario(idUsuario, tope)));
    }

    public List<AuditoriaDto> listarPorEntidad(String entidad, Integer limite) {
        int tope = normalizar(limite);
        return TransactionManager.soloLectura(em ->
                convertir(new AuditoriaDAO(em).listarPorEntidad(entidad, tope)));
    }

    // ---------- helpers ----------
    private static int normalizar(Integer limite) {
        if (limite == null || limite <= 0) return LIMITE_POR_DEFECTO;
        return Math.min(limite, LIMITE_MAXIMO);
    }

    private static String recortar(String texto) {
        if (texto == null) return null;
        return texto.length() <= MAX_DESCRIPCION ? texto : texto.substring(0, MAX_DESCRIPCION - 3) + "...";
    }

    private static List<AuditoriaDto> convertir(List<Auditoria> lista) {
        return lista.stream().map(AuditoriaService::toDto).collect(Collectors.toList());
    }

    static AuditoriaDto toDto(Auditoria a) {
        return new AuditoriaDto(
                a.getId(),
                a.getUsuario() == null ? null : a.getUsuario().getId(),
                a.getUsername(),
                a.getAccion(),
                a.getEntidad(),
                a.getIdRegistro(),
                a.getFechaHora(),
                a.getDescripcion());
    }
}