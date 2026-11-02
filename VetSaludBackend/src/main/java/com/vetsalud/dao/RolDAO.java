package com.vetsalud.dao;

import com.vetsalud.entity.Rol;
import jakarta.persistence.EntityManager;

public class RolDAO extends GenericDAO<Rol> {

    public RolDAO(EntityManager em) { super(em, Rol.class); }

    public Rol buscarPorNombre(String nombre) {
        return em.createQuery("SELECT r FROM Rol r WHERE r.nombre = :n", Rol.class)
                .setParameter("n", nombre)
                .getResultStream().findFirst().orElse(null);
    }

    public long contarUsuarios(Long rolId) {
        return em.createQuery("SELECT COUNT(ur) FROM UsuarioRol ur WHERE ur.rol.id = :id", Long.class)
                .setParameter("id", rolId)
                .getSingleResult();
    }
}
