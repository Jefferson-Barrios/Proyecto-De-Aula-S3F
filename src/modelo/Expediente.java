package modelo;

import java.time.LocalDate;

public class Expediente {

    private int idExpediente;
    private int idEstudiante;
    private LocalDate fechaCreacion;
    private String estado;

    public Expediente() {
    }

    public Expediente(int idExpediente, int idEstudiante, LocalDate fechaCreacion, String estado) {
        this.idExpediente = idExpediente;
        this.idEstudiante = idEstudiante;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
    }

    public int getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
