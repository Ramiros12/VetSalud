package com.vetsalud.controller;

import com.vetsalud.dto.HistorialClinicoDto;
import com.vetsalud.service.HistorialClinicoService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Historial clínico. GET /api/historiales?mascotaId=1 consulta el historial de una mascota. */
@WebServlet("/api/historiales/*")
public class HistorialClinicoController extends BaseController {

    private final HistorialClinicoService service = new HistorialClinicoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = idDeLaRuta(req);
        if (id == null) {
            ok(resp, service.listar(parametroLong(req, "mascotaId")));
        } else {
            ok(resp, service.buscar(id));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        enviar(resp, HttpServletResponse.SC_CREATED, service.crear(leerJson(req, HistorialClinicoDto.class), idUsuarioActual(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ok(resp, service.actualizar(idObligatorio(req), leerJson(req, HistorialClinicoDto.class), idUsuarioActual(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(idObligatorio(req), idUsuarioActual(req));
        mensaje(resp, "Historial clínico eliminado");
    }
}
