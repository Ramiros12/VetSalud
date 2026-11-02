package com.vetsalud.controller;

import com.vetsalud.dto.MascotaDto;
import com.vetsalud.service.MascotaService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** CRUD REST de mascotas. GET /api/mascotas?propietarioId=1 filtra por propietario. */
@WebServlet("/api/mascotas/*")
public class MascotaController extends BaseController {

    private final MascotaService service = new MascotaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = idDeLaRuta(req);
        if (id == null) {
            ok(resp, service.listar(parametroLong(req, "propietarioId")));
        } else {
            ok(resp, service.buscar(id));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        enviar(resp, HttpServletResponse.SC_CREATED, service.crear(leerJson(req, MascotaDto.class), idUsuarioActual(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ok(resp, service.actualizar(idObligatorio(req), leerJson(req, MascotaDto.class), idUsuarioActual(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(idObligatorio(req), idUsuarioActual(req));
        mensaje(resp, "Mascota eliminada");
    }
}