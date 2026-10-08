package modelo;

public class Acudiente {

    private int idAcudiente;
    private int idEstudiante;
    private String nombre;
    private String correo;
    private String telefono;

    public Acudiente() {
    }

    public Acudiente(int idAcudiente, int idEstudiante, String nombre, String correo, String telefono) {
        this.idAcudiente = idAcudiente;
        this.idEstudiante = idEstudiante;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
    }

    public int getIdAcudiente() {
        return idAcudiente;
    }

    public void setIdAcudiente(int idAcudiente) {
        this.idAcudiente = idAcudiente;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
