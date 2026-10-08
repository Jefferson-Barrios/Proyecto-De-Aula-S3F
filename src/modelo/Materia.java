package modelo;

public class Materia {

    private int idMateria;
    private int idInstitucion;
    private String nombre;
    private String descripcion;

    public Materia() {
    }

    public Materia(int idMateria, int idInstitucion, String nombre, String descripcion) {
        this.idMateria = idMateria;
        this.idInstitucion = idInstitucion;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }

    public int getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(int idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
