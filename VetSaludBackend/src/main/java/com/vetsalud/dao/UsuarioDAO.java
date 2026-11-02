package com.vetsalud.dao;

import com.vetsalud.entity.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;

public class UsuarioDAO extends GenericDAO<Usuario> {

    public UsuarioDAO(EntityManager em) { super(em, Usuario.class); }

    public Usuario buscarPorUsername(String username) {
        return em.createQuery(
                "SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles ur LEFT JOIN FETCH ur.rol "
                + "WHERE u.username = :username", Usuario.class)
                .setParameter("username", username)
                .getResultStream().findFirst().orElse(null);
    }

    public List<Usuario> listarConRoles() {
        return em.createQuery(
                "SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles ur LEFT JOIN FETCH ur.rol "
                + "ORDER BY u.id", Usuario.class)
                .getResultList();
    }
}
