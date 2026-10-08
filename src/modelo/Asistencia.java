package modelo;

import java.time.LocalDate;

public class Asistencia {

    private int idAsistencia;
    private int idEstudiante;
    private int idAsignacion;
    private LocalDate fecha;
    private String estado;
    private String observacion;

    public Asistencia() {
    }

    public Asistencia(int idAsistencia, int idEstudiante, int idAsignacion, LocalDate fecha, String estado, String observacion) {
        this.idAsistencia = idAsistencia;
        this.idEstudiante = idEstudiante;
        this.idAsignacion = idAsignacion;
        this.fecha = fecha;
        this.estado = estado;
        this.observacion = observacion;
    }

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(int idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
