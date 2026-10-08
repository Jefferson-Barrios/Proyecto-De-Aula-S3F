package modelo;

public class Docente extends Usuario {

    private String cargaAcademica;
    private String jornada;

    public Docente() {
    }

    public Docente(int idUsuario, Integer idInstitucion, String nombre, String documento, String telefono, String correo, String usuario, String contrasena, String estado, String cargaAcademica, String jornada) {
        super(idUsuario, idInstitucion, nombre, documento, telefono, correo, usuario, contrasena, estado);
        this.cargaAcademica = cargaAcademica;
        this.jornada = jornada;
    }

    @Override
    public String getRol() {
        return "DOCENTE";
    }

    public String getCargaAcademica() {
        return cargaAcademica;
    }

    public void setCargaAcademica(String cargaAcademica) {
        this.cargaAcademica = cargaAcademica;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }
}
