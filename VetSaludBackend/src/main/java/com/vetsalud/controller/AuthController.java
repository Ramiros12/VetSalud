package com.vetsalud.controller;

import com.vetsalud.dto.LoginRequestDto;
import com.vetsalud.dto.UsuarioDto;
import com.vetsalud.service.AuthService;
import com.vetsalud.util.ApiException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * POST /api/login   -> inicia sesión HTTP (body: {"username": "...", "password": "..."}).
 * GET  /api/session -> devuelve el usuario de la sesión actual (el frontend lo usa para saber si hay sesión).
 */
@WebServlet({"/api/login", "/api/session"})
public class AuthController extends BaseController {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequestDto login = leerJson(req, LoginRequestDto.class);
        if (login.getUsername() == null || login.getUsername().isBlank()
                || login.getPassword() == null || login.getPassword().isEmpty()) {
            throw ApiException.badRequest("Usuario y contraseña son obligatorios");
        }

        UsuarioDto usuario = authService.login(login.getUsername(), login.getPassword());
        if (usuario == null) {
            throw ApiException.unauthorized("Credenciales inválidas");
        }

        // Evita fijación de sesión: se descarta cualquier sesión previa y se crea una nueva
        HttpSession anterior = req.getSession(false);
        if (anterior != null) anterior.invalidate();
        HttpSession session = req.getSession(true);
        session.setAttribute(ATRIBUTO_USUARIO, usuario); // DTO, no entidad

        ok(resp, usuario);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UsuarioDto usuario = usuarioActual(req);
        if (usuario == null) throw ApiException.unauthorized("No hay sesión activa");
        ok(resp, usuario);
    }
}
