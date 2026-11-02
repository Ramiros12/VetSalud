package com.vetsalud.dto;

public class VeterinarioDto {
    private Long id;
    private String tarjetaProfesional;
    private String nombre;
    private String especialidad;

    public VeterinarioDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTarjetaProfesional() { return tarjetaProfesional; }
    public void setTarjetaProfesional(String tarjetaProfesional) { this.tarjetaProfesional = tarjetaProfesional; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
