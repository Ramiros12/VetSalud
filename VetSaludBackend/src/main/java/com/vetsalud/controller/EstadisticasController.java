/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.controller;

import com.vetsalud.service.EstadisticasService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** GET /api/estadisticas -> {"mascotas":2,"propietarios":2,"veterinarios":2,"historiales":1} */
@WebServlet("/api/estadisticas")
public class EstadisticasController extends BaseController {

    private final EstadisticasService service = new EstadisticasService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ok(resp, service.obtener());
    }
}