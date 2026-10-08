package modelo;

public class Psicosocial extends Usuario {

    private String area;
    private String perfilProfesional;
    private String gradosMonitorear;

    public Psicosocial() {
    }

    public Psicosocial(int idUsuario, Integer idInstitucion, String nombre, String documento, String telefono, String correo, String usuario, String contrasena, String estado, String area, String perfilProfesional, String gradosMonitorear) {
        super(idUsuario, idInstitucion, nombre, documento, telefono, correo, usuario, contrasena, estado);
        this.area = area;
        this.perfilProfesional = perfilProfesional;
        this.gradosMonitorear = gradosMonitorear;
    }

    @Override
    public String getRol() {
        return "PSICOSOCIAL";
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPerfilProfesional() {
        return perfilProfesional;
    }

    public void setPerfilProfesional(String perfilProfesional) {
        this.perfilProfesional = perfilProfesional;
    }

    public String getGradosMonitorear() {
        return gradosMonitorear;
    }

    public void setGradosMonitorear(String gradosMonitorear) {
        this.gradosMonitorear = gradosMonitorear;
    }
}
