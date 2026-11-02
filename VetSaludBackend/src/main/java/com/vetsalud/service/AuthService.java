package com.vetsalud.service;

import com.vetsalud.dao.UsuarioDAO;
import com.vetsalud.dto.UsuarioDto;
import com.vetsalud.entity.Usuario;
import com.vetsalud.util.PasswordUtil;
import com.vetsalud.util.TransactionManager;

public class AuthService {

    /**
     * Valida usuario y contraseña (BCrypt). Devuelve el DTO (sin contraseña) o null si las credenciales
     * son inválidas o el usuario está inactivo. No distingue el motivo para no dar pistas a un atacante.
     */
    public UsuarioDto login(String username, String passwordPlano) {
        if (username == null || passwordPlano == null) return null;
        return TransactionManager.soloLectura(em -> {
            Usuario usuario = new UsuarioDAO(em).buscarPorUsername(username.trim());
            if (usuario == null) return null;
            if (!Boolean.TRUE.equals(usuario.getActivo())) return null;
            if (!PasswordUtil.verificar(passwordPlano, usuario.getPassword())) return null;
            return UsuarioService.toDto(usuario);
        });
    }
}
