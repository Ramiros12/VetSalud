package com.vetsalud.dao;

import com.vetsalud.entity.Veterinario;
import jakarta.persistence.EntityManager;

public class VeterinarioDAO extends GenericDAO<Veterinario> {

    public VeterinarioDAO(EntityManager em) { super(em, Veterinario.class); }

    public Veterinario buscarPorTarjeta(String tarjeta) {
        return em.createQuery("SELECT v FROM Veterinario v WHERE v.tarjetaProfesional = :t", Veterinario.class)
                .setParameter("t", tarjeta)
                .getResultStream().findFirst().orElse(null);
    }

    public long contarHistoriales(Long veterinarioId) {
        return em.createQuery("SELECT COUNT(h) FROM HistorialClinico h WHERE h.veterinario.id = :id", Long.class)
                .setParameter("id", veterinarioId)
                .getSingleResult();
    }
}
