package modelo;

import java.time.LocalDate;

public class Estudiante extends Usuario {

    private Integer idCurso;
    private LocalDate fechaNacimiento;
    private int edad;
    private String direccion;
    private String barrio;
    private String tipoDocumento;
    private String grado;
    private String jornada;

    public Estudiante() {
    }

    public Estudiante(int idUsuario, Integer idInstitucion, String nombre, String documento, String telefono, String correo, String usuario, String contrasena, String estado, Integer idCurso, LocalDate fechaNacimiento, int edad, String direccion, String barrio, String tipoDocumento, String grado, String jornada) {
        super(idUsuario, idInstitucion, nombre, documento, telefono, correo, usuario, contrasena, estado);
        this.idCurso = idCurso;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.direccion = direccion;
        this.barrio = barrio;
        this.tipoDocumento = tipoDocumento;
        this.grado = grado;
        this.jornada = jornada;
    }

    @Override
    public String getRol() {
        return "ESTUDIANTE";
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }
}
