package com.vetsalud.filter;

import com.vetsalud.dto.UsuarioDto;
import com.vetsalud.util.JsonUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Control de acceso por sesión y por rol. Intercepta TODO lo que entra a la aplicación (/*).
 *
 * Recursos públicos (no piden sesión): raíz, index.html, /css/, /js/, /images/, /api/login, /api/logout.
 *
 * Sin sesión:
 *  - Llamada a la API (/api/...)  -> responde 401 con JSON.
 *  - Página web (dashboard.html)  -> redirige a index.html.
 *
 * Con sesión, en /api/ se valida el rol:
 *  - /api/usuarios y /api/roles: solo ADMINISTRADOR.
 *  - /api/propietarios, /api/mascotas, /api/veterinarios: lectura para cualquier rol; escritura solo ADMINISTRADOR.
 *  - /api/historiales: ADMINISTRADOR y VETERINARIO consultan, crean y editan; solo ADMINISTRADOR elimina.
 *  - /api/estadisticas y /api/export/...: ADMINISTRADOR y VETERINARIO (solo GET).
 *  - /api/backup: solo ADMINISTRADOR (cae en la regla final, igual que usuarios y roles).
 */
public class AuthFilter implements Filter {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String VETERINARIO = "VETERINARIO";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ruta = req.getRequestURI().substring(req.getContextPath().length());
        String metodo = req.getMethod();
        boolean esApi = ruta.startsWith("/api/");

        if ("OPTIONS".equalsIgnoreCase(metodo) || esPublico(ruta)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        UsuarioDto usuario = (session == null) ? null : (UsuarioDto) session.getAttribute("usuario");

        if (usuario == null) {
            if (esApi) {
                rechazar(resp, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado: inicie sesión");
            } else {
                resp.sendRedirect(req.getContextPath() + "/index.html"); // página web: al login
            }
            return;
        }

        if (esApi) {
            if (!tienePermiso(ruta, metodo, usuario)) {
                rechazar(resp, HttpServletResponse.SC_FORBIDDEN, "No tiene permisos para esta operación");
                return;
            }
        } else {
            // Páginas protegidas: que el navegador no las guarde en caché, así al cerrar sesión
            // el botón "Atrás" no vuelve a mostrar el panel.
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", "0");
        }
        chain.doFilter(request, response);
    }

    private boolean esPublico(String ruta) {
        return ruta.isEmpty() || ruta.equals("/")
                || ruta.equals("/index.html")
                || ruta.equals("/favicon.ico")
                || ruta.equals("/api/login")
                || ruta.equals("/api/logout")
                || ruta.startsWith("/css/")
                || ruta.startsWith("/js/")
                || ruta.startsWith("/images/");
    }

    private boolean tienePermiso(String ruta, String metodo, UsuarioDto usuario) {
        if (usuario.tieneRol(ADMIN)) return true;

        boolean lectura = "GET".equalsIgnoreCase(metodo) || "HEAD".equalsIgnoreCase(metodo);
        boolean esVeterinario = usuario.tieneRol(VETERINARIO);

        if (ruta.equals("/api/session")) return true;
        if (ruta.startsWith("/api/historiales")) {
            return esVeterinario && !"DELETE".equalsIgnoreCase(metodo);
        }
        if (ruta.startsWith("/api/propietarios") || ruta.startsWith("/api/mascotas")
                || ruta.startsWith("/api/veterinarios")) {
            return esVeterinario && lectura;
        }
        if (ruta.startsWith("/api/estadisticas") || ruta.startsWith("/api/export/")) {
            return esVeterinario && lectura;
        }
        return false; // usuarios, roles, backup y cualquier otra ruta: solo ADMINISTRADOR
    }

    private void rechazar(HttpServletResponse resp, int status, String mensaje) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonUtil.MAPPER.writeValue(resp.getWriter(), Map.of("error", mensaje));
    }
}