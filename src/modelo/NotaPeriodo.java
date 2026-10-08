package modelo;

public class NotaPeriodo {

    private int idNotaPeriodo;
    private int idEstudiante;
    private int idAsignacion;
    private int idPeriodo;
    private double notaDefinitiva;

    public NotaPeriodo() {
    }

    public NotaPeriodo(int idNotaPeriodo, int idEstudiante, int idAsignacion, int idPeriodo, double notaDefinitiva) {
        this.idNotaPeriodo = idNotaPeriodo;
        this.idEstudiante = idEstudiante;
        this.idAsignacion = idAsignacion;
        this.idPeriodo = idPeriodo;
        this.notaDefinitiva = notaDefinitiva;
    }

    public int getIdNotaPeriodo() {
        return idNotaPeriodo;
    }

    public void setIdNotaPeriodo(int idNotaPeriodo) {
        this.idNotaPeriodo = idNotaPeriodo;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(int idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public double getNotaDefinitiva() {
        return notaDefinitiva;
    }

    public void setNotaDefinitiva(double notaDefinitiva) {
        this.notaDefinitiva = notaDefinitiva;
    }
}
