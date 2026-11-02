package com.vetsalud.dao;

import com.vetsalud.entity.HistorialClinico;
import jakarta.persistence.EntityManager;
import java.util.List;

public class HistorialClinicoDAO extends GenericDAO<HistorialClinico> {

    public HistorialClinicoDAO(EntityManager em) { super(em, HistorialClinico.class); }

    public List<HistorialClinico> listarConDetalle() {
        return em.createQuery(
                "SELECT h FROM HistorialClinico h JOIN FETCH h.mascota JOIN FETCH h.veterinario "
                + "ORDER BY h.fechaConsulta DESC", HistorialClinico.class)
                .getResultList();
    }

    /** Consultar historial por mascota. */
    public List<HistorialClinico> listarPorMascota(Long mascotaId) {
        return em.createQuery(
                "SELECT h FROM HistorialClinico h JOIN FETCH h.mascota m JOIN FETCH h.veterinario "
                + "WHERE m.id = :mid ORDER BY h.fechaConsulta DESC", HistorialClinico.class)
                .setParameter("mid", mascotaId)
                .getResultList();
    }
}
