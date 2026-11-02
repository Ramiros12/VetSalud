package com.vetsalud.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * POST /api/logout -> cierra la sesión HTTP y responde 200.
 * Es idempotente: si no había sesión (por ejemplo, ya expiró) también responde 200.
 * Solo acepta POST para que un enlace o una imagen externa no pueda cerrarle la sesión al usuario.
 */
@WebServlet("/api/logout")
public class LogoutController extends BaseController {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false); // false: no crear una sesión nueva solo para destruirla
        if (session != null) {
            session.invalidate();
        }
        mensaje(resp, "Sesión cerrada"); // 200 OK
    }
}