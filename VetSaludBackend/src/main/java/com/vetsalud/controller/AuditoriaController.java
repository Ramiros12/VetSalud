/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.controller;

import com.vetsalud.service.AuditoriaService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Consulta de auditoría (solo ADMINISTRADOR, lo exige AuthFilter). Es de solo lectura:
 * no hay POST, PUT ni DELETE, así que el historial no se puede alterar desde la API.
 *
 *   GET /api/auditoria                       -> los más recientes (500 por defecto)
 *   GET /api/auditoria?usuarioId=1           -> acciones de un usuario
 *   GET /api/auditoria?entidad=Mascota       -> acciones sobre una entidad
 *   GET /api/auditoria?limite=100            -> cantidad máxima de filas (tope 5000)
 */
@WebServlet("/api/auditoria")
public class AuditoriaController extends BaseController {

    private final AuditoriaService service = new AuditoriaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long usuarioId = parametroLong(req, "usuarioId");
        String entidad = req.getParameter("entidad");
        Long limite = parametroLong(req, "limite");
        Integer tope = (limite == null) ? null : (int) Math.min(limite, Integer.MAX_VALUE);

        if (usuarioId != null) {
            ok(resp, service.listarPorUsuario(usuarioId, tope));
        } else if (entidad != null && !entidad.isBlank()) {
            ok(resp, service.listarPorEntidad(entidad.trim(), tope));
        } else {
            ok(resp, service.listarTodos(tope));
        }
    }
}