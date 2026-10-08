package modelo;

import java.time.LocalDate;

public class Comentario {

    private int idComentario;
    private int idExpediente;
    private int idDocente;
    private String contenido;
    private LocalDate fecha;

    public Comentario() {
    }

    public Comentario(int idComentario, int idExpediente, int idDocente, String contenido, LocalDate fecha) {
        this.idComentario = idComentario;
        this.idExpediente = idExpediente;
        this.idDocente = idDocente;
        this.contenido = contenido;
        this.fecha = fecha;
    }

    public int getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(int idComentario) {
        this.idComentario = idComentario;
    }

    public int getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
