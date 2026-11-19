package eipi.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BaseDatos {

    public static final String ACUDIENTE = "acudiente";
    public static final String ADMINISTRADOR_GENERAL = "administrador_general";
    public static final String ALERTA_DESERCION = "alerta_desercion";
    public static final String ASIGNACION_DOCENTE = "asignacion_docente";
    public static final String ASISTENCIA = "asistencia";
    public static final String CALIFICACION = "calificacion";
    public static final String CITA_PSICOSOCIAL = "cita_psicosocial";
    public static final String COMENTARIO = "comentario";
    public static final String CURSO = "curso";
    public static final String DIRECTIVA = "directiva";
    public static final String DOCENTE = "docente";
    public static final String ESTUDIANTE = "estudiante";
    public static final String EXPEDIENTE = "expediente";
    public static final String HORARIO = "horario";
    public static final String INSTITUCION = "institucion";
    public static final String MATERIA = "materia";
    public static final String NOTA_PERIODO = "nota_periodo";
    public static final String PERIODO_ACADEMICO = "periodo_academico";
    public static final String PSICOSOCIAL = "psicosocial";
    public static final String USUARIO = "usuario";

    public static final String[] TABLAS = {
            ACUDIENTE, ADMINISTRADOR_GENERAL, ALERTA_DESERCION, ASIGNACION_DOCENTE,
            ASISTENCIA, CALIFICACION, CITA_PSICOSOCIAL, COMENTARIO, CURSO, DIRECTIVA,
            DOCENTE, ESTUDIANTE, EXPEDIENTE, HORARIO, INSTITUCION, MATERIA,
            NOTA_PERIODO, PERIODO_ACADEMICO, PSICOSOCIAL, USUARIO
    };

    private static final Path CARPETA = Paths.get("datos");
    private static final Map<String, List<Map<String, Object>>> DATOS = new LinkedHashMap<>();
    private static final Set<String> SUCIAS = new LinkedHashSet<>();
    private static final Object CANDADO = new Object();
    private static boolean cargado;
    private static int profundidad;

    private BaseDatos() {
    }

    public interface Accion<T> {
        T ejecutar() throws DatosException;
    }

    public static synchronized void inicializar() throws DatosException {
        asegurar();
    }

    public static List<Map<String, Object>> tabla(String nombre) throws DatosException {
        asegurar();
        List<Map<String, Object>> filas = DATOS.get(nombre);
        if (filas == null) {
            throw new DatosException("Tabla desconocida: " + nombre);
        }
        return filas;
    }

    private static void asegurar() throws DatosException {
        if (cargado) {
            return;
        }
        synchronized (CANDADO) {
            if (cargado) {
                return;
            }
            try {
                Files.createDirectories(CARPETA);
                for (String nombre : TABLAS) {
                    DATOS.put(nombre, leerTabla(nombre));
                }
            } catch (IOException ex) {
                throw new DatosException("No se pudo abrir la carpeta de datos: " + ex.getMessage(), ex);
            }
            cargado = true;
            sembrar();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> leerTabla(String nombre) throws IOException {
        List<Map<String, Object>> filas = new ArrayList<>();
        Path archivo = CARPETA.resolve(nombre + ".json");
        if (!Files.exists(archivo)) {
            return filas;
        }
        String texto = Files.readString(archivo, StandardCharsets.UTF_8);
        if (texto.isBlank()) {
            return filas;
        }
        Object contenido = Json.parse(texto);
        if (contenido instanceof List<?> lista) {
            for (Object item : lista) {
                if (item instanceof Map<?, ?> mapa) {
                    filas.add(new LinkedHashMap<>((Map<String, Object>) mapa));
                }
            }
        }
        return filas;
    }

    private static void sembrar() throws DatosException {
        List<Map<String, Object>> usuarios = DATOS.get(USUARIO);
        if (!usuarios.isEmpty()) {
            return;
        }
        Map<String, Object> admin = nuevo();
        admin.put("id_usuario", 1);
        admin.put("id_institucion", null);
        admin.put("rol", "ADMINISTRADOR");
        admin.put("nombre", "Administrador General EIPI");
        admin.put("documento", "90000001");
        admin.put("telefono", "3001000001");
        admin.put("correo", "admin@eipi.test");
        admin.put("usuario", "admin");
        admin.put("contrasena", Contrasenas.cifrar("admin123"));
        admin.put("estado", "ACTIVO");
        usuarios.add(admin);

        Map<String, Object> general = nuevo();
        general.put("id_usuario", 1);
        DATOS.get(ADMINISTRADOR_GENERAL).add(general);

        SUCIAS.add(USUARIO);
        SUCIAS.add(ADMINISTRADOR_GENERAL);
        escribirSucias();
    }

    public static void guardar() throws DatosException {
        if (profundidad > 0) {
            return;
        }
        escribirSucias();
    }

    public static <T> T enTransaccion(Accion<T> accion) throws DatosException {
        asegurar();
        Map<String, List<Map<String, Object>>> respaldo = copia();
        profundidad++;
        T resultado;
        try {
            resultado = accion.ejecutar();
        } catch (DatosException | RuntimeException ex) {
            profundidad--;
            restaurar(respaldo);
            SUCIAS.clear();
            throw ex;
        }
        profundidad--;
        escribirSucias();
        return resultado;
    }

    public static void insertar(String nombre, Map<String, Object> fila) throws DatosException {
        tabla(nombre).add(fila);
        SUCIAS.add(nombre);
        guardar();
    }

    public static boolean eliminar(String nombre, Map<String, Object> fila) throws DatosException {
        boolean quitado = tabla(nombre).remove(fila);
        if (quitado) {
            SUCIAS.add(nombre);
            guardar();
        }
        return quitado;
    }

    public static void actualizada(String nombre) throws DatosException {
        SUCIAS.add(nombre);
        guardar();
    }

    public static void marcarSucia(String nombre) {
        SUCIAS.add(nombre);
    }

    public static int nuevoId(String nombre, String columnaId) throws DatosException {
        int maximo = 0;
        for (Map<String, Object> fila : tabla(nombre)) {
            Integer id = entero(fila, columnaId);
            if (id != null && id > maximo) {
                maximo = id;
            }
        }
        return maximo + 1;
    }

    public static Map<String, Object> nuevo() {
        return new LinkedHashMap<>();
    }

    public static Map<String, Object> porId(String nombre, String columnaId, Object valor) throws DatosException {
        if (valor == null) {
            return null;
        }
        for (Map<String, Object> fila : tabla(nombre)) {
            if (igual(valor, fila.get(columnaId))) {
                return fila;
            }
        }
        return null;
    }

    public static Map<Object, Map<String, Object>> indice(String nombre, String columna) throws DatosException {
        Map<Object, Map<String, Object>> indice = new LinkedHashMap<>();
        for (Map<String, Object> fila : tabla(nombre)) {
            indice.putIfAbsent(fila.get(columna), fila);
        }
        return indice;
    }

    public static Map<Object, List<Map<String, Object>>> agrupar(String nombre, String columna) throws DatosException {
        Map<Object, List<Map<String, Object>>> indice = new LinkedHashMap<>();
        for (Map<String, Object> fila : tabla(nombre)) {
            indice.computeIfAbsent(fila.get(columna), k -> new ArrayList<>()).add(fila);
        }
        return indice;
    }

    public static String texto(Map<String, Object> fila, String columna) {
        Object valor = fila.get(columna);
        return valor == null ? null : valor.toString();
    }

    public static Integer entero(Map<String, Object> fila, String columna) {
        Object valor = fila.get(columna);
        if (valor instanceof Number n) {
            return n.intValue();
        }
        return valor == null ? null : Integer.valueOf(valor.toString());
    }

    public static int enteroPrimitivo(Map<String, Object> fila, String columna) {
        Integer valor = entero(fila, columna);
        return valor == null ? 0 : valor;
    }

    public static Double decimal(Map<String, Object> fila, String columna) {
        Object valor = fila.get(columna);
        if (valor instanceof Number n) {
            return n.doubleValue();
        }
        return valor == null ? null : Double.valueOf(valor.toString());
    }

    public static double decimalPrimitivo(Map<String, Object> fila, String columna) {
        Double valor = decimal(fila, columna);
        return valor == null ? 0 : valor;
    }

    public static LocalDate fecha(Map<String, Object> fila, String columna) {
        String valor = texto(fila, columna);
        return valor == null || valor.isEmpty() ? null : LocalDate.parse(valor);
    }

    public static boolean igual(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a instanceof Number x && b instanceof Number y) {
            return x.doubleValue() == y.doubleValue();
        }
        return a.equals(b);
    }

    private static void escribirSucias() throws DatosException {
        if (SUCIAS.isEmpty()) {
            return;
        }
        List<String> pendientes = new ArrayList<>(SUCIAS);
        for (String nombre : pendientes) {
            escribirTabla(nombre);
        }
        SUCIAS.clear();
    }

    private static void escribirTabla(String nombre) throws DatosException {
        Path destino = CARPETA.resolve(nombre + ".json");
        Path temporal = CARPETA.resolve(nombre + ".json.tmp");
        Path respaldo = CARPETA.resolve(nombre + ".json.bak");
        try {
            Files.createDirectories(CARPETA);
            Files.writeString(temporal, Json.escribir(DATOS.get(nombre)), StandardCharsets.UTF_8);
            if (Files.exists(destino)) {
                Files.copy(destino, respaldo, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new DatosException("No se pudo guardar " + nombre + ".json: " + ex.getMessage(), ex);
        }
    }

    private static Map<String, List<Map<String, Object>>> copia() {
        Map<String, List<Map<String, Object>>> copia = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entrada : DATOS.entrySet()) {
            List<Map<String, Object>> filas = new ArrayList<>(entrada.getValue().size());
            for (Map<String, Object> fila : entrada.getValue()) {
                filas.add(new LinkedHashMap<>(fila));
            }
            copia.put(entrada.getKey(), filas);
        }
        return copia;
    }

    private static void restaurar(Map<String, List<Map<String, Object>>> respaldo) {
        DATOS.clear();
        DATOS.putAll(respaldo);
    }
}
