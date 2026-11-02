package com.vetsalud.service;

import com.vetsalud.dao.HistorialClinicoDAO;
import com.vetsalud.dao.MascotaDAO;
import com.vetsalud.dao.VeterinarioDAO;
import com.vetsalud.dto.HistorialClinicoDto;
import com.vetsalud.entity.HistorialClinico;
import com.vetsalud.entity.Mascota;
import com.vetsalud.entity.Veterinario;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.TransactionManager;
import com.vetsalud.util.ValidationUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class HistorialClinicoService {

    private final AuditoriaService auditoria = new AuditoriaService();

    /** Lista todos los historiales o, si se indica mascotaId, solo los de esa mascota. */
    public List<HistorialClinicoDto> listar(Long mascotaId) {
        return TransactionManager.soloLectura(em -> {
            HistorialClinicoDAO dao = new HistorialClinicoDAO(em);
            List<HistorialClinico> lista = (mascotaId == null) ? dao.listarConDetalle() : dao.listarPorMascota(mascotaId);
            return lista.stream().map(HistorialClinicoService::toDto).collect(Collectors.toList());
        });
    }

    public HistorialClinicoDto buscar(Long id) {
        return TransactionManager.soloLectura(em -> toDto(obtener(new HistorialClinicoDAO(em), id)));
    }

    public HistorialClinicoDto crear(HistorialClinicoDto dto, Long idUsuario) {
        validarBasico(dto);
        return TransactionManager.enTransaccion(em -> {
            HistorialClinico h = new HistorialClinico();
            aplicar(h, dto, em);
            new HistorialClinicoDAO(em).guardar(h);
            auditoria.registrar(em, idUsuario, AuditoriaService.CREAR, AuditoriaService.HISTORIAL, h.getId(),
                    "Consulta registrada de la mascota " + h.getMascota().getNombre()
                    + " (atendida por " + h.getVeterinario().getNombre() + ")");
            return toDto(h);
        });
    }

    public HistorialClinicoDto actualizar(Long id, HistorialClinicoDto dto, Long idUsuario) {
        validarBasico(dto);
        return TransactionManager.enTransaccion(em -> {
            HistorialClinico h = obtener(new HistorialClinicoDAO(em), id);
            aplicar(h, dto, em);
            auditoria.registrar(em, idUsuario, AuditoriaService.ACTUALIZAR, AuditoriaService.HISTORIAL, h.getId(),
                    "Actualización de la consulta de la mascota " + h.getMascota().getNombre());
            return toDto(h);
        });
    }

    public void eliminar(Long id, Long idUsuario) {
        TransactionManager.enTransaccion(em -> {
            HistorialClinicoDAO dao = new HistorialClinicoDAO(em);
            HistorialClinico h = obtener(dao, id);
            String mascota = h.getMascota().getNombre(); // se guarda antes de borrar
            dao.eliminar(h);
            auditoria.registrar(em, idUsuario, AuditoriaService.ELIMINAR, AuditoriaService.HISTORIAL, id,
                    "Eliminación de una consulta de la mascota " + mascota);
            return null;
        });
    }

    // ---------- helpers ----------
    private static HistorialClinico obtener(HistorialClinicoDAO dao, Long id) {
        HistorialClinico h = dao.buscarPorId(id);
        if (h == null) throw ApiException.notFound("Historial clínico no encontrado (id " + id + ")");
        return h;
    }

    private static void validarBasico(HistorialClinicoDto dto) {
        if (dto == null) throw ApiException.badRequest("El cuerpo de la petición es obligatorio");
        if (dto.getMascotaId() == null) throw ApiException.badRequest("El campo 'mascotaId' es obligatorio");
        if (dto.getVeterinarioId() == null) throw ApiException.badRequest("El campo 'veterinarioId' es obligatorio");
    }

    private static void aplicar(HistorialClinico h, HistorialClinicoDto dto, EntityManager em) {
        String diagnostico = ValidationUtil.requerido(dto.getDiagnostico(), "diagnostico", 65000);
        Mascota m = new MascotaDAO(em).buscarPorId(dto.getMascotaId());
        if (m == null) throw ApiException.badRequest("La mascota indicada no existe (id " + dto.getMascotaId() + ")");
        Veterinario v = new VeterinarioDAO(em).buscarPorId(dto.getVeterinarioId());
        if (v == null) throw ApiException.badRequest("El veterinario indicado no existe (id " + dto.getVeterinarioId() + ")");

        h.setDiagnostico(diagnostico);
        h.setFechaConsulta(dto.getFechaConsulta() != null ? dto.getFechaConsulta() : LocalDateTime.now().withNano(0));
        h.setMascota(m);
        h.setVeterinario(v);
    }

    static HistorialClinicoDto toDto(HistorialClinico h) {
        HistorialClinicoDto dto = new HistorialClinicoDto();
        dto.setId(h.getId());
        dto.setFechaConsulta(h.getFechaConsulta());
        dto.setDiagnostico(h.getDiagnostico());
        dto.setMascotaId(h.getMascota().getId());
        dto.setMascotaNombre(h.getMascota().getNombre());
        dto.setVeterinarioId(h.getVeterinario().getId());
        dto.setVeterinarioNombre(h.getVeterinario().getNombre());
        return dto;
    }
}