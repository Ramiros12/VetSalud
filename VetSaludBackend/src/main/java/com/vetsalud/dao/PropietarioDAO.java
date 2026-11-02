package com.vetsalud.dao;

import com.vetsalud.entity.Propietario;
import jakarta.persistence.EntityManager;

public class PropietarioDAO extends GenericDAO<Propietario> {

    public PropietarioDAO(EntityManager em) { super(em, Propietario.class); }

    public Propietario buscarPorDocumento(String documento) {
        return em.createQuery("SELECT p FROM Propietario p WHERE p.documento = :doc", Propietario.class)
                .setParameter("doc", documento)
                .getResultStream().findFirst().orElse(null);
    }
}
