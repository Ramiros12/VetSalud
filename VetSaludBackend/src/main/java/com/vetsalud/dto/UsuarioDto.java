package com.vetsalud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO de usuario. La contraseña es WRITE_ONLY: se acepta al recibir JSON
 * pero jamás se escribe en una respuesta. Se guarda en sesión (Serializable).
 */
public class UsuarioDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Boolean activo;
    private Set<String> roles = new HashSet<>();

    public UsuarioDto() {}

    public UsuarioDto(Long id, String username, Boolean activo, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.activo = activo;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public boolean tieneRol(String rol) {
        return roles != null && roles.contains(rol);
    }
}
