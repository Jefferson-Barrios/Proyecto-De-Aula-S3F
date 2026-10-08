package modelo;

public abstract class Usuario {

    private int idUsuario;
    private Integer idInstitucion;
    private String nombre;
    private String documento;
    private String telefono;
    private String correo;
    private String usuario;
    private String contrasena;
    private String estado;

    public Usuario() {
    }

    public Usuario(int idUsuario, Integer idInstitucion, String nombre, String documento, String telefono, String correo, String usuario, String contrasena, String estado) {
        this.idUsuario = idUsuario;
        this.idInstitucion = idInstitucion;
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.correo = correo;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    public abstract String getRol();

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(Integer idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
