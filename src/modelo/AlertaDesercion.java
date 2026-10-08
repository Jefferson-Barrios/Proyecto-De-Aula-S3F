package modelo;

import java.time.LocalDate;

public class AlertaDesercion {

    private int idAlerta;
    private int idEstudiante;
    private Integer idDocente;
    private Integer idPsicosocial;
    private LocalDate fecha;
    private String nivelRiesgo;
    private String motivo;
    private String estado;

    public AlertaDesercion() {
    }

    public AlertaDesercion(int idAlerta, int idEstudiante, Integer idDocente, Integer idPsicosocial, LocalDate fecha, String nivelRiesgo, String motivo, String estado) {
        this.idAlerta = idAlerta;
        this.idEstudiante = idEstudiante;
        this.idDocente = idDocente;
        this.idPsicosocial = idPsicosocial;
        this.fecha = fecha;
        this.nivelRiesgo = nivelRiesgo;
        this.motivo = motivo;
        this.estado = estado;
    }

    public int getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(int idAlerta) {
        this.idAlerta = idAlerta;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Integer getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Integer idDocente) {
        this.idDocente = idDocente;
    }

    public Integer getIdPsicosocial() {
        return idPsicosocial;
    }

    public void setIdPsicosocial(Integer idPsicosocial) {
        this.idPsicosocial = idPsicosocial;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
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
