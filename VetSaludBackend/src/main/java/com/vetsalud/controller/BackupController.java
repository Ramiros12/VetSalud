/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.controller;

import com.vetsalud.service.BackupService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * POST /api/backup -> genera la copia de seguridad (mysqldump) y la envía como descarga .sql.
 * Solo ADMINISTRADOR (lo exige AuthFilter). Es POST porque ejecuta un proceso en el servidor:
 * así un enlace externo no puede dispararlo con una simple visita.
 */
@WebServlet("/api/backup")
public class BackupController extends BaseController {

    private final BackupService backupService = new BackupService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        File backup = backupService.generarBackup(); // si falla lanza ApiException -> JSON de error

        resp.setContentType("application/sql");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + backup.getName() + "\"");
        resp.setHeader("Cache-Control", "no-store");
        resp.setContentLengthLong(backup.length());

        try (OutputStream os = resp.getOutputStream()) {
            Files.copy(backup.toPath(), os);
        }
    }
}