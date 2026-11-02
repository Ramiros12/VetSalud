package com.vetsalud.service;

import com.vetsalud.dao.MascotaDAO;
import com.vetsalud.dao.PropietarioDAO;
import com.vetsalud.dto.MascotaDto;
import com.vetsalud.entity.Mascota;
import com.vetsalud.entity.Propietario;
import com.vetsalud.util.ApiException;
import com.vetsalud.util.TransactionManager;
import com.vetsalud.util.ValidationUtil;
import java.util.List;
import java.util.stream.Collectors;

public class MascotaService {

    private final AuditoriaService auditoria = new AuditoriaService();

    private record Datos(String nombre, String especie, String raza, Integer edad, Long propietarioId) {}

    public List<MascotaDto> listar(Long propietarioId) {
        return TransactionManager.soloLectura(em -> {
            MascotaDAO dao = new MascotaDAO(em);
            List<Mascota> lista = (propietarioId == null) ? dao.listarConPropietario() : dao.listarPorPropietario(propietarioId);
            return lista.stream().map(MascotaService::toDto).collect(Collectors.toList());
        });
    }

    public MascotaDto buscar(Long id) {
        return TransactionManager.soloLectura(em -> toDto(obtener(new MascotaDAO(em), id)));
    }

    public MascotaDto crear(MascotaDto dto, Long idUsuario) {
        Datos d = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            Mascota m = new Mascota();
            aplicar(m, d, new PropietarioDAO(em));
            new MascotaDAO(em).guardar(m);
            auditoria.registrar(em, idUsuario, AuditoriaService.CREAR, AuditoriaService.MASCOTA, m.getId(),
                    "Creación de mascota: " + m.getNombre());
            return toDto(m);
        });
    }

    public MascotaDto actualizar(Long id, MascotaDto dto, Long idUsuario) {
        Datos d = validar(dto);
        return TransactionManager.enTransaccion(em -> {
            Mascota m = obtener(new MascotaDAO(em), id);
            aplicar(m, d, new PropietarioDAO(em));
            auditoria.registrar(em, idUsuario, AuditoriaService.ACTUALIZAR, AuditoriaService.MASCOTA, m.getId(),
                    "Actualización de mascota: " + m.getNombre());
            return toDto(m);
        });
    }

    public void eliminar(Long id, Long idUsuario) {
        TransactionManager.enTransaccion(em -> {
            MascotaDAO dao = new MascotaDAO(em);
            Mascota m = obtener(dao, id);
            String nombre = m.getNombre(); // se guarda antes de borrar
            dao.eliminar(m);
            auditoria.registrar(em, idUsuario, AuditoriaService.ELIMINAR, AuditoriaService.MASCOTA, id,
                    "Eliminación de mascota: " + nombre);
            return null;
        });
    }

    // ---------- helpers ----------
    private static Mascota obtener(MascotaDAO dao, Long id) {
        Mascota m = dao.buscarPorId(id);
        if (m == null) throw ApiException.notFound("Mascota no encontrada (id " + id + ")");
        return m;
    }

    private static Datos validar(MascotaDto dto) {
        if (dto == null) throw ApiException.badRequest("El cuerpo de la petición es obligatorio");
        if (dto.getPropietarioId() == null) throw ApiException.badRequest("El campo 'propietarioId' es obligatorio");
        if (dto.getEdad() != null && (dto.getEdad() < 0 || dto.getEdad() > 60)) {
            throw ApiException.badRequest("La edad debe estar entre 0 y 60");
        }
        return new Datos(
                ValidationUtil.requerido(dto.getNombre(), "nombre", 50),
                ValidationUtil.requerido(dto.getEspecie(), "especie", 50),
                ValidationUtil.opcional(dto.getRaza(), "raza", 255),
                dto.getEdad(),
                dto.getPropietarioId());
    }

    private static void aplicar(Mascota m, Datos d, PropietarioDAO propietarioDAO) {
        Propietario p = propietarioDAO.buscarPorId(d.propietarioId());
        if (p == null) throw ApiException.badRequest("El propietario indicado no existe (id " + d.propietarioId() + ")");
        m.setNombre(d.nombre());
        m.setEspecie(d.especie());
        m.setRaza(d.raza());
        m.setEdad(d.edad());
        m.setPropietario(p);
    }

    static MascotaDto toDto(Mascota m) {
        MascotaDto dto = new MascotaDto();
        dto.setId(m.getId());
        dto.setNombre(m.getNombre());
        dto.setEspecie(m.getEspecie());
        dto.setRaza(m.getRaza());
        dto.setEdad(m.getEdad());
        dto.setPropietarioId(m.getPropietario().getId());
        dto.setPropietarioNombre(m.getPropietario().getNombre());
        return dto;
    }
}