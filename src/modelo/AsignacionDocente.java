package modelo;

public class AsignacionDocente {

    private int idAsignacion;
    private int idDocente;
    private int idCurso;
    private int idMateria;
    private String jornada;
    private String estado;

    public AsignacionDocente() {
    }

    public AsignacionDocente(int idAsignacion, int idDocente, int idCurso, int idMateria, String jornada, String estado) {
        this.idAsignacion = idAsignacion;
        this.idDocente = idDocente;
        this.idCurso = idCurso;
        this.idMateria = idMateria;
        this.jornada = jornada;
        this.estado = estado;
    }

    public int getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(int idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
