package com.vetsalud.dao;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * DAO genérico. Recibe el EntityManager y NO maneja transacciones:
 * la transacción la controla la capa de servicios (TransactionManager).
 */
public class GenericDAO<T> {
    protected final EntityManager em;
    protected final Class<T> entityClass;

    public GenericDAO(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    public T guardar(T entity) {
        em.persist(entity);
        return entity;
    }

    public T buscarPorId(Object id) {
        return em.find(entityClass, id);
    }

    public List<T> listarTodos() {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id", entityClass)
                .getResultList();
    }

    /** Cuenta los registros de la entidad (lo usan las estadísticas). */
    public long count() {
        return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }

    public T actualizar(T entity) {
        return em.merge(entity);
    }

    public void eliminar(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}