package vista.docente;

import dao.AsignacionDAO;
import dao.PeriodoDAO;
import eipi.util.Sesion;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.JComboBox;
import modelo.PeriodoAcademico;
import modelo.Usuario;
import vista.componentes.Opcion;

final class Selectores {

    private Selectores() {
    }

    static int idDocente() {
        Usuario u = Sesion.getUsuarioActual();
        if (u == null) {
            throw new IllegalStateException("No hay una sesión iniciada.");
        }
        return u.getIdUsuario();
    }

    static int idInstitucion() {
        Usuario u = Sesion.getUsuarioActual();
        if (u == null || u.getIdInstitucion() == null) {
            throw new IllegalStateException("Tu usuario no está asociado a ninguna institución.");
        }
        return u.getIdInstitucion();
    }

    static void cargarAsignaciones(JComboBox<Opcion> combo) throws DatosException {
        int anterior = combo.getSelectedItem() == null ? -1 : ((Opcion) combo.getSelectedItem()).getId();
        List<Object[]> filas = new AsignacionDAO().listarDeDocente(idDocente());
        combo.removeAllItems();
        Opcion elegida = null;
        for (Object[] f : filas) {
            Opcion o = new Opcion((Integer) f[0], f[3] + " · " + f[2] + " (" + f[4] + ")", f);
            combo.addItem(o);
            if (o.getId() == anterior) {
                elegida = o;
            }
        }
        if (elegida != null) {
            combo.setSelectedItem(elegida);
        }
    }

    static void cargarPeriodos(JComboBox<Opcion> combo) throws DatosException {
        int anterior = combo.getSelectedItem() == null ? -1 : ((Opcion) combo.getSelectedItem()).getId();
        List<PeriodoAcademico> periodos = new PeriodoDAO().listar(idInstitucion());
        combo.removeAllItems();
        Opcion elegida = null;
        Opcion primeraAbierta = null;
        for (PeriodoAcademico p : periodos) {
            boolean abierto = "ABIERTO".equals(p.getEstado());
            Opcion o = new Opcion(p.getIdPeriodo(), p.getNombre() + (abierto ? "" : " (cerrado)"), p);
            combo.addItem(o);
            if (o.getId() == anterior) {
                elegida = o;
            }
            if (abierto && primeraAbierta == null) {
                primeraAbierta = o;
            }
        }
        if (elegida != null) {
            combo.setSelectedItem(elegida);
        } else if (primeraAbierta != null) {
            combo.setSelectedItem(primeraAbierta);
        }
    }
}
