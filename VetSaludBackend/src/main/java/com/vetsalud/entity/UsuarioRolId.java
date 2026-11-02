package com.vetsalud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Clave primaria compuesta de usuarios_roles (usuario_id, rol_id). */
@Embeddable
public class UsuarioRolId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "rol_id")
    private Long rolId;

    public UsuarioRolId() {}

    public UsuarioRolId(Long usuarioId, Long rolId) {
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioRolId other)) return false;
        return Objects.equals(usuarioId, other.usuarioId) && Objects.equals(rolId, other.rolId);
    }

    @Override
    public int hashCode() { return Objects.hash(usuarioId, rolId); }
}
