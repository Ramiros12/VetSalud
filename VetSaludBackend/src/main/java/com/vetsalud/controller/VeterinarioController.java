package com.vetsalud.controller;

import com.vetsalud.dto.VeterinarioDto;
import com.vetsalud.service.VeterinarioService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** CRUD REST de veterinarios: GET (lista o por id), POST, PUT /{id}, DELETE /{id}. */
@WebServlet("/api/veterinarios/*")
public class VeterinarioController extends BaseController {

    private final VeterinarioService service = new VeterinarioService();

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
        VeterinarioDto creado = service.crear(leerJson(req, VeterinarioDto.class), idUsuarioActual(req));
        enviar(resp, HttpServletResponse.SC_CREATED, creado);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ok(resp, service.actualizar(idObligatorio(req), leerJson(req, VeterinarioDto.class), idUsuarioActual(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(idObligatorio(req), idUsuarioActual(req));
        mensaje(resp, "Veterinario eliminado");
    }
}