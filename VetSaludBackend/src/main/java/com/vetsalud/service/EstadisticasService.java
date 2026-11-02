/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.service;
 
import com.vetsalud.dao.HistorialClinicoDAO;
import com.vetsalud.dao.MascotaDAO;
import com.vetsalud.dao.PropietarioDAO;
import com.vetsalud.dao.VeterinarioDAO;
import com.vetsalud.util.TransactionManager;
import java.util.LinkedHashMap;
import java.util.Map;
 
/** Conteo de registros de cada entidad para el panel de estadísticas. */
public class EstadisticasService {
 
    public Map<String, Long> obtener() {
        return TransactionManager.soloLectura(em -> {
            // Map con put: cada clave es el nombre que luego lee el frontend (data.mascotas, ...)
            Map<String, Long> estadisticas = new LinkedHashMap<>();
            estadisticas.put("mascotas", new MascotaDAO(em).count());
            estadisticas.put("propietarios", new PropietarioDAO(em).count());
            estadisticas.put("veterinarios", new VeterinarioDAO(em).count());
            estadisticas.put("historiales", new HistorialClinicoDAO(em).count());
            return estadisticas;
        });
    }
}