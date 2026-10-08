package modelo;

public class Institucion {

    private int idInstitucion;
    private Integer idAdministrador;
    private String nombre;
    private String codigoOficial;
    private String sedePrincipal;
    private String barrioSector;
    private String telefonoInstitucional;
    private String correoInstitucional;
    private String nombreRector;
    private String correoResponsable;
    private String telefonoResponsable;
    private String estado;

    public Institucion() {
    }

    public Institucion(int idInstitucion, Integer idAdministrador, String nombre, String codigoOficial, String sedePrincipal, String barrioSector, String telefonoInstitucional, String correoInstitucional, String nombreRector, String correoResponsable, String telefonoResponsable, String estado) {
        this.idInstitucion = idInstitucion;
        this.idAdministrador = idAdministrador;
        this.nombre = nombre;
        this.codigoOficial = codigoOficial;
        this.sedePrincipal = sedePrincipal;
        this.barrioSector = barrioSector;
        this.telefonoInstitucional = telefonoInstitucional;
        this.correoInstitucional = correoInstitucional;
        this.nombreRector = nombreRector;
        this.correoResponsable = correoResponsable;
        this.telefonoResponsable = telefonoResponsable;
        this.estado = estado;
    }

    public int getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(int idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public Integer getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(Integer idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoOficial() {
        return codigoOficial;
    }

    public void setCodigoOficial(String codigoOficial) {
        this.codigoOficial = codigoOficial;
    }

    public String getSedePrincipal() {
        return sedePrincipal;
    }

    public void setSedePrincipal(String sedePrincipal) {
        this.sedePrincipal = sedePrincipal;
    }

    public String getBarrioSector() {
        return barrioSector;
    }

    public void setBarrioSector(String barrioSector) {
        this.barrioSector = barrioSector;
    }

    public String getTelefonoInstitucional() {
        return telefonoInstitucional;
    }

    public void setTelefonoInstitucional(String telefonoInstitucional) {
        this.telefonoInstitucional = telefonoInstitucional;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getNombreRector() {
        return nombreRector;
    }

    public void setNombreRector(String nombreRector) {
        this.nombreRector = nombreRector;
    }

    public String getCorreoResponsable() {
        return correoResponsable;
    }

    public void setCorreoResponsable(String correoResponsable) {
        this.correoResponsable = correoResponsable;
    }

    public String getTelefonoResponsable() {
        return telefonoResponsable;
    }

    public void setTelefonoResponsable(String telefonoResponsable) {
        this.telefonoResponsable = telefonoResponsable;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
