/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.dao;

import com.vetsalud.entity.Auditoria;
import jakarta.persistence.EntityManager;
import java.util.List;

/** Consultas de auditoría: siempre de la más reciente a la más antigua y con un tope de filas. */
public class AuditoriaDAO extends GenericDAO<Auditoria> {

    public AuditoriaDAO(EntityManager em) { super(em, Auditoria.class); }

    public List<Auditoria> listarTodosConUsuario(int limite) {
        return em.createQuery(
                "SELECT a FROM Auditoria a LEFT JOIN FETCH a.usuario "
                + "ORDER BY a.fechaHora DESC, a.id DESC", Auditoria.class)
                .setMaxResults(limite)
                .getResultList();
    }

    public List<Auditoria> listarPorUsuario(Long idUsuario, int limite) {
        return em.createQuery(
                "SELECT a FROM Auditoria a LEFT JOIN FETCH a.usuario u "
                + "WHERE u.id = :idUsuario "
                + "ORDER BY a.fechaHora DESC, a.id DESC", Auditoria.class)
                .setParameter("idUsuario", idUsuario)
                .setMaxResults(limite)
                .getResultList();
    }

    public List<Auditoria> listarPorEntidad(String entidad, int limite) {
        return em.createQuery(
                "SELECT a FROM Auditoria a LEFT JOIN FETCH a.usuario "
                + "WHERE a.entidad = :entidad "
                + "ORDER BY a.fechaHora DESC, a.id DESC", Auditoria.class)
                .setParameter("entidad", entidad)
                .setMaxResults(limite)
                .getResultList();
    }
}
