package modelo;

public class Directiva extends Usuario {

    public Directiva() {
    }

    public Directiva(int idUsuario, Integer idInstitucion, String nombre, String documento, String telefono, String correo, String usuario, String contrasena, String estado) {
        super(idUsuario, idInstitucion, nombre, documento, telefono, correo, usuario, contrasena, estado);
    }

    @Override
    public String getRol() {
        return "DIRECTIVA";
    }
}
