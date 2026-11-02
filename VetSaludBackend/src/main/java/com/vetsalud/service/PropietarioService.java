package com.vetsalud.service;

import com.vetsalud.dao.PropietarioDAO;
import com.vetsalud.dto.PropietarioDto;
import com.vetsalud.entity.Propietario;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.TransactionManager;
import com.vetsalud.util.ValidationUtil;
import java.util.List;
import java.util.stream.Collectors;

public class PropietarioService {

    private final AuditoriaService auditoria = new AuditoriaService();

    private record Datos(String documento, String nombre, String telefono, String email) {}

    public List<PropietarioDto> listar() {
        return TransactionManager.soloLectura(em ->
                new PropietarioDAO(em).listarTodos().stream().map(PropietarioService::toDto).collect(Collectors.toList()));
    }

    public PropietarioDto buscar(Long id) {
        return TransactionManager.soloLectura(em -> toDto(obtener(new PropietarioDAO(em), id)));
    }

    public PropietarioDto crear(PropietarioDto dto, Long idUsuario) {
        Datos d = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            PropietarioDAO dao = new PropietarioDAO(em);
            if (dao.buscarPorDocumento(d.documento()) != null) {
                throw ApiException.conflict("Ya existe un propietario con el documento " + d.documento());
            }
            Propietario p = new Propietario();
            aplicar(p, d);
            dao.guardar(p);
            auditoria.registrar(em, idUsuario, AuditoriaService.CREAR, AuditoriaService.PROPIETARIO, p.getId(),
                    "Creación de propietario: " + p.getNombre());
            return toDto(p);
        });
    }

    public PropietarioDto actualizar(Long id, PropietarioDto dto, Long idUsuario) {
        Datos d = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            PropietarioDAO dao = new PropietarioDAO(em);
            Propietario p = obtener(dao, id);
            Propietario otro = dao.buscarPorDocumento(d.documento());
            if (otro != null && !otro.getId().equals(id)) {
                throw ApiException.conflict("Ya existe otro propietario con el documento " + d.documento());
            }
            aplicar(p, d);
            auditoria.registrar(em, idUsuario, AuditoriaService.ACTUALIZAR, AuditoriaService.PROPIETARIO, p.getId(),
                    "Actualización de propietario: " + p.getNombre());
            return toDto(p);
        });
    }

    /** Elimina el propietario y, en cascada, sus mascotas (y los historiales de estas). */
    public void eliminar(Long id, Long idUsuario) {
        TransactionManager.enTransaccion(em -> {
            PropietarioDAO dao = new PropietarioDAO(em);
            Propietario p = obtener(dao, id);
            String nombre = p.getNombre();
            int mascotas = p.getMascotas().size(); // se eliminan en cascada
            dao.eliminar(p);
            auditoria.registrar(em, idUsuario, AuditoriaService.ELIMINAR, AuditoriaService.PROPIETARIO, id,
                    "Eliminación de propietario: " + nombre + " (con " + mascotas + " mascota(s) asociada(s))");
            return null;
        });
    }

    // ---------- helpers ----------
    private static Propietario obtener(PropietarioDAO dao, Long id) {
        Propietario p = dao.buscarPorId(id);
        if (p == null) throw ApiException.notFound("Propietario no encontrado (id " + id + ")");
        return p;
    }

    private static Datos validar(PropietarioDto dto) {
        if (dto == null) throw ApiException.badRequest("El cuerpo de la petición es obligatorio");
        return new Datos(
                ValidationUtil.requerido(dto.getDocumento(), "documento", 20),
                ValidationUtil.requerido(dto.getNombre(), "nombre", 100),
                ValidationUtil.opcional(dto.getTelefono(), "telefono", 255),
                ValidationUtil.emailOpcional(dto.getEmail()));
    }

    private static void aplicar(Propietario p, Datos d) {
        p.setDocumento(d.documento());
        p.setNombre(d.nombre());
        p.setTelefono(d.telefono());
        p.setEmail(d.email());
    }

    static PropietarioDto toDto(Propietario p) {
        return new PropietarioDto(p.getId(), p.getDocumento(), p.getNombre(), p.getTelefono(), p.getEmail());
    }
}