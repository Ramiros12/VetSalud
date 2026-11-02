package com.vetsalud.dao;

import com.vetsalud.entity.Mascota;
import jakarta.persistence.EntityManager;
import java.util.List;

public class MascotaDAO extends GenericDAO<Mascota> {

    public MascotaDAO(EntityManager em) { super(em, Mascota.class); }

    /** Trae las mascotas junto con su propietario (evita LazyInitializationException y N+1). */
    public List<Mascota> listarConPropietario() {
        return em.createQuery(
                "SELECT m FROM Mascota m JOIN FETCH m.propietario ORDER BY m.id", Mascota.class)
                .getResultList();
    }

    public List<Mascota> listarPorPropietario(Long propietarioId) {
        return em.createQuery(
                "SELECT m FROM Mascota m JOIN FETCH m.propietario p WHERE p.id = :pid ORDER BY m.id", Mascota.class)
                .setParameter("pid", propietarioId)
                .getResultList();
    }
}
