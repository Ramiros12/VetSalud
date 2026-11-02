package com.vetsalud.service;

import com.vetsalud.dao.VeterinarioDAO;
import com.vetsalud.dto.VeterinarioDto;
import com.vetsalud.entity.Veterinario;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.TransactionManager;
import com.vetsalud.util.ValidationUtil;
import java.util.List;
import java.util.stream.Collectors;

public class VeterinarioService {

    private final AuditoriaService auditoria = new AuditoriaService();

    private record Datos(String tarjeta, String nombre, String especialidad) {}

    public List<VeterinarioDto> listar() {
        return TransactionManager.soloLectura(em ->
                new VeterinarioDAO(em).listarTodos().stream().map(VeterinarioService::toDto).collect(Collectors.toList()));
    }

    public VeterinarioDto buscar(Long id) {
        return TransactionManager.soloLectura(em -> toDto(obtener(new VeterinarioDAO(em), id)));
    }

    public VeterinarioDto crear(VeterinarioDto dto, Long idUsuario) {
        Datos d = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            VeterinarioDAO dao = new VeterinarioDAO(em);
            if (dao.buscarPorTarjeta(d.tarjeta()) != null) {
                throw ApiException.conflict("Ya existe un veterinario con la tarjeta profesional " + d.tarjeta());
            }
            Veterinario v = new Veterinario();
            aplicar(v, d);
            dao.guardar(v);
            auditoria.registrar(em, idUsuario, AuditoriaService.CREAR, AuditoriaService.VETERINARIO, v.getId(),
                    "Creación de veterinario: " + v.getNombre());
            return toDto(v);
        });
    }

    public VeterinarioDto actualizar(Long id, VeterinarioDto dto, Long idUsuario) {
        Datos d = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            VeterinarioDAO dao = new VeterinarioDAO(em);
            Veterinario v = obtener(dao, id);
            Veterinario otro = dao.buscarPorTarjeta(d.tarjeta());
            if (otro != null && !otro.getId().equals(id)) {
                throw ApiException.conflict("Ya existe otro veterinario con la tarjeta profesional " + d.tarjeta());
            }
            aplicar(v, d);
            auditoria.registrar(em, idUsuario, AuditoriaService.ACTUALIZAR, AuditoriaService.VETERINARIO, v.getId(),
                    "Actualización de veterinario: " + v.getNombre());
            return toDto(v);
        });
    }

    public void eliminar(Long id, Long idUsuario) {
        TransactionManager.enTransaccion(em -> {
            VeterinarioDAO dao = new VeterinarioDAO(em);
            Veterinario v = obtener(dao, id);
            if (dao.contarHistoriales(id) > 0) {
                throw ApiException.conflict("No se puede eliminar: el veterinario tiene historiales clínicos asociados");
            }
            String nombre = v.getNombre();
            dao.eliminar(v);
            auditoria.registrar(em, idUsuario, AuditoriaService.ELIMINAR, AuditoriaService.VETERINARIO, id,
                    "Eliminación de veterinario: " + nombre);
            return null;
        });
    }

    // ---------- helpers ----------
    private static Veterinario obtener(VeterinarioDAO dao, Long id) {
        Veterinario v = dao.buscarPorId(id);
        if (v == null) throw ApiException.notFound("Veterinario no encontrado (id " + id + ")");
        return v;
    }

    private static Datos validar(VeterinarioDto dto) {
        if (dto == null) throw ApiException.badRequest("El cuerpo de la petición es obligatorio");
        return new Datos(
                ValidationUtil.requerido(dto.getTarjetaProfesional(), "tarjetaProfesional", 30),
                ValidationUtil.requerido(dto.getNombre(), "nombre", 100),
                ValidationUtil.opcional(dto.getEspecialidad(), "especialidad", 100));
    }

    private static void aplicar(Veterinario v, Datos d) {
        v.setTarjetaProfesional(d.tarjeta());
        v.setNombre(d.nombre());
        v.setEspecialidad(d.especialidad());
    }

    static VeterinarioDto toDto(Veterinario v) {
        VeterinarioDto dto = new VeterinarioDto();
        dto.setId(v.getId());
        dto.setTarjetaProfesional(v.getTarjetaProfesional());
        dto.setNombre(v.getNombre());
        dto.setEspecialidad(v.getEspecialidad());
        return dto;
    }
}