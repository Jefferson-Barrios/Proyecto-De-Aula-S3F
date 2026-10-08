package modelo;

import java.time.LocalDate;

public class Calificacion {

    private int idCalificacion;
    private int idEstudiante;
    private int idAsignacion;
    private int idPeriodo;
    private double valor;
    private String tipo;
    private LocalDate fecha;

    public Calificacion() {
    }

    public Calificacion(int idCalificacion, int idEstudiante, int idAsignacion, int idPeriodo, double valor, String tipo, LocalDate fecha) {
        this.idCalificacion = idCalificacion;
        this.idEstudiante = idEstudiante;
        this.idAsignacion = idAsignacion;
        this.idPeriodo = idPeriodo;
        this.valor = valor;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    public int getIdCalificacion() {
        return idCalificacion;
    }

    public void setIdCalificacion(int idCalificacion) {
        this.idCalificacion = idCalificacion;
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

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
