package com.vetsalud.service;

import com.vetsalud.dao.RolDAO;
import com.vetsalud.dto.RolDto;
import com.vetsalud.entity.Rol;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.TransactionManager;
import com.vetsalud.util.ValidationUtil;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RolService {

    /** Roles que usa el sistema para el control de acceso: no se pueden renombrar ni borrar. */
    public static final Set<String> ROLES_DEL_SISTEMA = Set.of("ADMINISTRADOR", "VETERINARIO");

    private final AuditoriaService auditoria = new AuditoriaService();

    public List<RolDto> listar() {
        return TransactionManager.soloLectura(em ->
                new RolDAO(em).listarTodos().stream().map(RolService::toDto).collect(Collectors.toList()));
    }

    public RolDto buscar(Long id) {
        return TransactionManager.soloLectura(em -> toDto(obtener(new RolDAO(em), id)));
    }

    public RolDto crear(RolDto dto, Long idUsuario) {
        String nombre = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            RolDAO dao = new RolDAO(em);
            if (dao.buscarPorNombre(nombre) != null) {
                throw ApiException.conflict("Ya existe el rol " + nombre);
            }
            Rol r = new Rol();
            r.setNombre(nombre);
            dao.guardar(r);
            auditoria.registrar(em, idUsuario, AuditoriaService.CREAR, AuditoriaService.ROL, r.getId(),
                    "Creación de rol: " + r.getNombre());
            return toDto(r);
        });
    }

    public RolDto actualizar(Long id, RolDto dto, Long idUsuario) {
        String nombre = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            RolDAO dao = new RolDAO(em);
            Rol r = obtener(dao, id);
            if (ROLES_DEL_SISTEMA.contains(r.getNombre())) {
                throw ApiException.badRequest("El rol " + r.getNombre() + " es del sistema y no se puede modificar");
            }
            Rol otro = dao.buscarPorNombre(nombre);
            if (otro != null && !otro.getId().equals(id)) {
                throw ApiException.conflict("Ya existe el rol " + nombre);
            }
            String anterior = r.getNombre();
            r.setNombre(nombre);
            auditoria.registrar(em, idUsuario, AuditoriaService.ACTUALIZAR, AuditoriaService.ROL, r.getId(),
                    "Actualización de rol: de " + anterior + " a " + nombre);
            return toDto(r);
        });
    }

    public void eliminar(Long id, Long idUsuario) {
        TransactionManager.enTransaccion(em -> {
            RolDAO dao = new RolDAO(em);
            Rol r = obtener(dao, id);
            if (ROLES_DEL_SISTEMA.contains(r.getNombre())) {
                throw ApiException.badRequest("El rol " + r.getNombre() + " es del sistema y no se puede eliminar");
            }
            if (dao.contarUsuarios(id) > 0) {
                throw ApiException.conflict("No se puede eliminar: hay usuarios que tienen este rol");
            }
            String nombreRol = r.getNombre();
            dao.eliminar(r);
            auditoria.registrar(em, idUsuario, AuditoriaService.ELIMINAR, AuditoriaService.ROL, id,
                    "Eliminación de rol: " + nombreRol);
            return null;
        });
    }

    // ---------- helpers ----------
    private static Rol obtener(RolDAO dao, Long id) {
        Rol r = dao.buscarPorId(id);
        if (r == null) throw ApiException.notFound("Rol no encontrado (id " + id + ")");
        return r;
    }

    private static String validar(RolDto dto) {
        if (dto == null) throw ApiException.badRequest("El cuerpo de la petición es obligatorio");
        return ValidationUtil.requerido(dto.getNombre(), "nombre", 50).toUpperCase();
    }

    static RolDto toDto(Rol r) {
        return new RolDto(r.getId(), r.getNombre());
    }
}