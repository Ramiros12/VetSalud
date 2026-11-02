package com.vetsalud.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;

/**
 * Manejo MANUAL de transacciones con EntityManager (requisito del proyecto).
 * La capa de servicios decide qué es una unidad de trabajo; los DAO solo reciben el EntityManager.
 */
public final class TransactionManager {

    private TransactionManager() {}

    /** Ejecuta el trabajo dentro de una transacción: commit si todo sale bien, rollback si falla. */
    public static <R> R enTransaccion(Function<EntityManager, R> trabajo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R resultado = trabajo.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Consultas de solo lectura (sin transacción explícita). */
    public static <R> R soloLectura(Function<EntityManager, R> trabajo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return trabajo.apply(em);
        } finally {
            em.close();
        }
    }
}
