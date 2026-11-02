package com.vetsalud.service;

import com.vetsalud.dao.RolDAO;
import com.vetsalud.dao.UsuarioDAO;
import com.vetsalud.dto.UsuarioDto;
import com.vetsalud.entity.Rol;
import com.vetsalud.entity.Usuario;
import com.vetsalud.entity.UsuarioRol;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.PasswordUtil;
import com.vetsalud.util.TransactionManager;
import com.vetsalud.util.ValidationUtil;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UsuarioService {

    private final AuditoriaService auditoria = new AuditoriaService();

    private static final int MIN_PASSWORD = 6;
    private static final int MAX_PASSWORD = 72; // límite de BCrypt

    public List<UsuarioDto> listar() {
        return TransactionManager.soloLectura(em ->
                new UsuarioDAO(em).listarConRoles().stream().map(UsuarioService::toDto).collect(Collectors.toList()));
    }

    public UsuarioDto buscar(Long id) {
        return TransactionManager.soloLectura(em -> toDto(obtener(new UsuarioDAO(em), id)));
    }

    public UsuarioDto crear(UsuarioDto dto, Long idActor) {
        if (dto == null) throw ApiException.badRequest("El cuerpo de la petición es obligatorio");
        String username = ValidationUtil.requerido(dto.getUsername(), "username", 50);
        validarPassword(dto.getPassword());
        Set<String> nombresRoles = normalizarRoles(dto.getRoles());
        if (nombresRoles.isEmpty()) throw ApiException.badRequest("Debe asignar al menos un rol");

        return TransactionManager.enTransaccion(em -> {
            UsuarioDAO dao = new UsuarioDAO(em);
            if (dao.buscarPorUsername(username) != null) {
                throw ApiException.conflict("El nombre de usuario '" + username + "' ya existe");
            }
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(PasswordUtil.cifrar(dto.getPassword()));
            u.setActivo(dto.getActivo() == null || dto.getActivo());
            dao.guardar(u); // IDENTITY: el id queda asignado tras persist
            sincronizarRoles(u, nombresRoles, new RolDAO(em));
            auditoria.registrar(em, idActor, AuditoriaService.CREAR, AuditoriaService.USUARIO, u.getId(),
                    "Creación de usuario: " + u.getUsername() + " (roles: " + String.join(", ", nombresRoles) + ")");
            return toDto(u);
        });
    }

    public UsuarioDto actualizar(Long id, UsuarioDto dto, Long usuarioActualId) {
        if (dto == null) throw ApiException.badRequest("El cuerpo de la petición es obligatorio");
        String username = ValidationUtil.requerido(dto.getUsername(), "username", 50);
        boolean cambiaPassword = dto.getPassword() != null && !dto.getPassword().isEmpty();
        if (cambiaPassword) validarPassword(dto.getPassword());
        Set<String> nombresRoles = (dto.getRoles() == null) ? null : normalizarRoles(dto.getRoles());
        if (nombresRoles != null && nombresRoles.isEmpty()) throw ApiException.badRequest("Debe asignar al menos un rol");

        if (id.equals(usuarioActualId)) {
            if (Boolean.FALSE.equals(dto.getActivo())) {
                throw ApiException.badRequest("No puedes desactivar tu propio usuario");
            }
            if (nombresRoles != null && !nombresRoles.contains("ADMINISTRADOR")) {
                throw ApiException.badRequest("No puedes quitarte a ti mismo el rol ADMINISTRADOR");
            }
        }

        return TransactionManager.enTransaccion(em -> {
            UsuarioDAO dao = new UsuarioDAO(em);
            Usuario u = obtener(dao, id);
            Usuario otro = dao.buscarPorUsername(username);
            if (otro != null && !otro.getId().equals(id)) {
                throw ApiException.conflict("El nombre de usuario '" + username + "' ya existe");
            }
            u.setUsername(username);
            if (cambiaPassword) u.setPassword(PasswordUtil.cifrar(dto.getPassword()));
            if (dto.getActivo() != null) u.setActivo(dto.getActivo());
            if (nombresRoles != null) sincronizarRoles(u, nombresRoles, new RolDAO(em));
            // Nunca se escribe la contraseña en la auditoría: solo se deja constancia de que cambió
            auditoria.registrar(em, usuarioActualId, AuditoriaService.ACTUALIZAR, AuditoriaService.USUARIO, u.getId(),
                    "Actualización de usuario: " + u.getUsername() + (cambiaPassword ? " (contraseña cambiada)" : ""));
            return toDto(u);
        });
    }

    public void eliminar(Long id, Long usuarioActualId) {
        if (id.equals(usuarioActualId)) {
            throw ApiException.badRequest("No puedes eliminar tu propio usuario");
        }
        TransactionManager.enTransaccion(em -> {
            UsuarioDAO dao = new UsuarioDAO(em);
            Usuario u = obtener(dao, id);
            String username = u.getUsername();
            dao.eliminar(u); // cascade + orphanRemoval borran sus filas en usuarios_roles
            auditoria.registrar(em, usuarioActualId, AuditoriaService.ELIMINAR, AuditoriaService.USUARIO, id,
                    "Eliminación de usuario: " + username);
            return null;
        });
    }

    // ---------- helpers ----------
    private static Usuario obtener(UsuarioDAO dao, Long id) {
        Usuario u = dao.buscarPorId(id);
        if (u == null) throw ApiException.notFound("Usuario no encontrado (id " + id + ")");
        return u;
    }

    private static void validarPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD) {
            throw ApiException.badRequest("La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");
        }
        if (password.length() > MAX_PASSWORD) {
            throw ApiException.badRequest("La contraseña no puede superar " + MAX_PASSWORD + " caracteres");
        }
    }

    private static Set<String> normalizarRoles(Set<String> roles) {
        Set<String> resultado = new LinkedHashSet<>();
        if (roles != null) {
            for (String r : roles) {
                if (r != null && !r.trim().isEmpty()) resultado.add(r.trim().toUpperCase());
            }
        }
        return resultado;
    }

    /** Deja al usuario con exactamente los roles indicados (agrega los nuevos y quita los que sobran). */
    private static void sincronizarRoles(Usuario u, Set<String> nombres, RolDAO rolDAO) {
        u.getRoles().removeIf(ur -> !nombres.contains(ur.getRol().getNombre()));
        Set<String> actuales = u.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .collect(Collectors.toSet());
        for (String nombre : nombres) {
            if (actuales.contains(nombre)) continue;
            Rol rol = rolDAO.buscarPorNombre(nombre);
            if (rol == null) throw ApiException.badRequest("El rol '" + nombre + "' no existe");
            u.getRoles().add(new UsuarioRol(u, rol));
        }
    }

    static UsuarioDto toDto(Usuario u) {
        Set<String> roles = u.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new UsuarioDto(u.getId(), u.getUsername(), u.getActivo(), roles);
    }
}