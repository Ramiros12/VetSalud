package com.vetsalud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vetsalud.dto.UsuarioDto;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.JsonUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Base de los servlets REST: lectura/escritura de JSON, extracción del id de la URL
 * y traducción centralizada de errores a respuestas JSON ({"error": "..."}).
 */
public abstract class BaseController extends HttpServlet {

    public static final String ATRIBUTO_USUARIO = "usuario";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            super.service(req, resp);
        } catch (ApiException e) {
            error(resp, e.getStatus(), e.getMessage());
        } catch (JsonProcessingException e) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON inválido o con formato incorrecto");
        } catch (PersistenceException e) {
            log("Error de persistencia", e);
            error(resp, HttpServletResponse.SC_CONFLICT,
                    "La operación viola una restricción de la base de datos (dato duplicado o registros relacionados)");
        } catch (RuntimeException e) {
            log("Error interno no controlado", e);
            error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    // ---------- respuestas ----------
    protected void enviar(HttpServletResponse resp, int status, Object cuerpo) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        JsonUtil.MAPPER.writeValue(resp.getWriter(), cuerpo);
    }

    protected void ok(HttpServletResponse resp, Object cuerpo) throws IOException {
        enviar(resp, HttpServletResponse.SC_OK, cuerpo);
    }

    protected void mensaje(HttpServletResponse resp, String texto) throws IOException {
        enviar(resp, HttpServletResponse.SC_OK, Map.of("mensaje", texto));
    }

    protected void error(HttpServletResponse resp, int status, String texto) throws IOException {
        if (resp.isCommitted()) return;
        enviar(resp, status, Map.of("error", texto));
    }

    // ---------- entrada ----------
    protected <T> T leerJson(HttpServletRequest req, Class<T> tipo) throws IOException {
        return JsonUtil.MAPPER.readValue(req.getInputStream(), tipo);
    }

    /** Devuelve el id de /api/recurso/{id} o null si la URL no lo trae. */
    protected Long idDeLaRuta(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) return null;
        try {
            return Long.valueOf(info.substring(1));
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("El id de la URL no es válido");
        }
    }

    protected Long idObligatorio(HttpServletRequest req) {
        Long id = idDeLaRuta(req);
        if (id == null) throw ApiException.badRequest("Debe indicar el id en la URL (ej: /api/recurso/1)");
        return id;
    }

    protected Long parametroLong(HttpServletRequest req, String nombre) {
        String valor = req.getParameter(nombre);
        if (valor == null || valor.isBlank()) return null;
        try {
            return Long.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("El parámetro '" + nombre + "' no es válido");
        }
    }

    /** Id del usuario con sesión iniciada (el que realiza la acción, para la auditoría). */
    protected Long idUsuarioActual(HttpServletRequest req) {
        UsuarioDto usuario = usuarioActual(req);
        if (usuario == null) throw ApiException.unauthorized("No autenticado: inicie sesión");
        return usuario.getId();
    }

    protected UsuarioDto usuarioActual(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (UsuarioDto) session.getAttribute(ATRIBUTO_USUARIO);
    }
}