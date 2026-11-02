/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.dto;

import java.time.LocalDateTime;

public class AuditoriaDto {
    private Long id;
    private Long idUsuario;
    private String username;
    private String accion;
    private String entidad;
    private Long idRegistro;
    private LocalDateTime fechaHora;
    private String descripcion;

    public AuditoriaDto() {}

    public AuditoriaDto(Long id, Long idUsuario, String username, String accion, String entidad,
                        Long idRegistro, LocalDateTime fechaHora, String descripcion) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.username = username;
        this.accion = accion;
        this.entidad = entidad;
        this.idRegistro = idRegistro;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public Long getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Long idRegistro) { this.idRegistro = idRegistro; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
