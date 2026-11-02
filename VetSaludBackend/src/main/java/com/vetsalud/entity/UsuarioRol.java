package com.vetsalud.entity;

import jakarta.persistence.*;
import java.util.Objects;

/** Entidad intermedia de la relación muchos-a-muchos entre Usuario y Rol. */
@Entity
@Table(name = "usuarios_roles")
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id = new UsuarioRolId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("rolId")
    @JoinColumn(name = "rol_id")
    private Rol rol;

    public UsuarioRol() {}

    /** El usuario y el rol deben estar ya persistidos (con id). */
    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
        this.id = new UsuarioRolId(usuario.getId(), rol.getId());
    }

    public UsuarioRolId getId() { return id; }
    public void setId(UsuarioRolId id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioRol other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
