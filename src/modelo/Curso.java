package modelo;

public class Curso {

    private int idCurso;
    private int idInstitucion;
    private String grado;
    private String grupo;
    private String jornada;

    public Curso() {
    }

    public Curso(int idCurso, int idInstitucion, String grado, String grupo, String jornada) {
        this.idCurso = idCurso;
        this.idInstitucion = idInstitucion;
        this.grado = grado;
        this.grupo = grupo;
        this.jornada = jornada;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public int getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(int idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }
}
