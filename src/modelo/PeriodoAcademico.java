package modelo;

import java.time.LocalDate;

public class PeriodoAcademico {

    private int idPeriodo;
    private int idInstitucion;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    public PeriodoAcademico() {
    }

    public PeriodoAcademico(int idPeriodo, int idInstitucion, String nombre, LocalDate fechaInicio,
                            LocalDate fechaFin, String estado) {
        this.idPeriodo = idPeriodo;
        this.idInstitucion = idInstitucion;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public int getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(int idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
