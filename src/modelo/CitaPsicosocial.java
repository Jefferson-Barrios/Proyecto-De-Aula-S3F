package modelo;

import java.time.LocalDate;

public class CitaPsicosocial {

    private int idCita;
    private int idEstudiante;
    private int idPsicosocial;
    private Integer idAlerta;
    private LocalDate fecha;
    private String hora;
    private String motivo;
    private String estado;

    public CitaPsicosocial() {
    }

    public CitaPsicosocial(int idCita, int idEstudiante, int idPsicosocial, Integer idAlerta, LocalDate fecha, String hora, String motivo, String estado) {
        this.idCita = idCita;
        this.idEstudiante = idEstudiante;
        this.idPsicosocial = idPsicosocial;
        this.idAlerta = idAlerta;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdPsicosocial() {
        return idPsicosocial;
    }

    public void setIdPsicosocial(int idPsicosocial) {
        this.idPsicosocial = idPsicosocial;
    }

    public Integer getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(Integer idAlerta) {
        this.idAlerta = idAlerta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
