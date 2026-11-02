package com.vetsalud.controller;

import com.vetsalud.dto.UsuarioDto;
import com.vetsalud.service.UsuarioService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Gestión de usuarios y asignación de roles (solo ADMINISTRADOR, lo exige AuthFilter). */
@WebServlet("/api/usuarios/*")
public class UsuarioController extends BaseController {

    private final UsuarioService service = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = idDeLaRuta(req);
        if (id == null) {
            ok(resp, service.listar());
        } else {
            ok(resp, service.buscar(id));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        enviar(resp, HttpServletResponse.SC_CREATED, service.crear(leerJson(req, UsuarioDto.class), idUsuarioActual(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long actual = idUsuarioActual(req);
        ok(resp, service.actualizar(idObligatorio(req), leerJson(req, UsuarioDto.class), actual));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long actual = idUsuarioActual(req);
        service.eliminar(idObligatorio(req), actual);
        mensaje(resp, "Usuario eliminado");
    }
}