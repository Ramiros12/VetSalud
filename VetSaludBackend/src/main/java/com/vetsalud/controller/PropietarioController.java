package com.vetsalud.controller;

import com.vetsalud.dto.PropietarioDto;
import com.vetsalud.service.PropietarioService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** CRUD REST de propietarios: GET (lista o por id), POST, PUT /{id}, DELETE /{id}. */
@WebServlet("/api/propietarios/*")
public class PropietarioController extends BaseController {

    private final PropietarioService service = new PropietarioService();

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
        PropietarioDto creado = service.crear(leerJson(req, PropietarioDto.class), idUsuarioActual(req));
        enviar(resp, HttpServletResponse.SC_CREATED, creado);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ok(resp, service.actualizar(idObligatorio(req), leerJson(req, PropietarioDto.class), idUsuarioActual(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(idObligatorio(req), idUsuarioActual(req));
        mensaje(resp, "Propietario eliminado");
    }
}
