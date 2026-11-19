package eipi.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

    private Json() {
    }

    public static Object parse(String texto) {
        Parser p = new Parser(texto);
        Object valor = p.valor();
        p.saltarEspacios();
        if (!p.fin()) {
            throw new IllegalArgumentException("Contenido JSON sobrante en la posición " + p.posicion());
        }
        return valor;
    }

    public static String escribir(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribir(valor, sb, 0);
        sb.append('\n');
        return sb.toString();
    }

    private static void escribir(Object valor, StringBuilder sb, int nivel) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof String s) {
            comillas(s, sb);
        } else if (valor instanceof Boolean b) {
            sb.append(b);
        } else if (valor instanceof Number n) {
            sb.append(numero(n));
        } else if (valor instanceof Map<?, ?> mapa) {
            escribirMapa(mapa, sb, nivel);
        } else if (valor instanceof List<?> lista) {
            escribirLista(lista, sb, nivel);
        } else {
            comillas(valor.toString(), sb);
        }
    }

    private static void escribirMapa(Map<?, ?> mapa, StringBuilder sb, int nivel) {
        if (mapa.isEmpty()) {
            sb.append("{}");
            return;
        }
        sb.append("{\n");
        int i = 0;
        int total = mapa.size();
        for (Map.Entry<?, ?> e : mapa.entrySet()) {
            sangria(sb, nivel + 1);
            comillas(String.valueOf(e.getKey()), sb);
            sb.append(": ");
            escribir(e.getValue(), sb, nivel + 1);
            if (++i < total) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sangria(sb, nivel);
        sb.append('}');
    }

    private static void escribirLista(List<?> lista, StringBuilder sb, int nivel) {
        if (lista.isEmpty()) {
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        int i = 0;
        int total = lista.size();
        for (Object o : lista) {
            sangria(sb, nivel + 1);
            escribir(o, sb, nivel + 1);
            if (++i < total) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sangria(sb, nivel);
        sb.append(']');
    }

    private static void sangria(StringBuilder sb, int nivel) {
        for (int i = 0; i < nivel; i++) {
            sb.append("  ");
        }
    }

    private static String numero(Number n) {
        if (n instanceof Double d) {
            return Double.isFinite(d) ? d.toString() : "null";
        }
        if (n instanceof Float f) {
            return Float.isFinite(f) ? f.toString() : "null";
        }
        return n.toString();
    }

    private static void comillas(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static final class Parser {

        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
        }

        boolean fin() {
            return i >= s.length();
        }

        int posicion() {
            return i;
        }

        void saltarEspacios() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        Object valor() {
            saltarEspacios();
            if (fin()) {
                throw new IllegalArgumentException("JSON incompleto");
            }
            char c = s.charAt(i);
            return switch (c) {
                case '{' -> objeto();
                case '[' -> arreglo();
                case '"' -> cadena();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> numero();
            };
        }

        private Object literal(String palabra, Object valor) {
            if (!s.startsWith(palabra, i)) {
                throw new IllegalArgumentException("Token inesperado en la posición " + i);
            }
            i += palabra.length();
            return valor;
        }

        private Map<String, Object> objeto() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            i++;
            saltarEspacios();
            if (i < s.length() && s.charAt(i) == '}') {
                i++;
                return mapa;
            }
            while (true) {
                saltarEspacios();
                String clave = cadena();
                saltarEspacios();
                if (i >= s.length() || s.charAt(i) != ':') {
                    throw new IllegalArgumentException("Se esperaba ':' en la posición " + i);
                }
                i++;
                mapa.put(clave, valor());
                saltarEspacios();
                if (i >= s.length()) {
                    throw new IllegalArgumentException("Objeto sin cerrar");
                }
                char c = s.charAt(i++);
                if (c == '}') {
                    return mapa;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("Se esperaba ',' o '}' en la posición " + (i - 1));
                }
            }
        }

        private List<Object> arreglo() {
            List<Object> lista = new ArrayList<>();
            i++;
            saltarEspacios();
            if (i < s.length() && s.charAt(i) == ']') {
                i++;
                return lista;
            }
            while (true) {
                lista.add(valor());
                saltarEspacios();
                if (i >= s.length()) {
                    throw new IllegalArgumentException("Arreglo sin cerrar");
                }
                char c = s.charAt(i++);
                if (c == ']') {
                    return lista;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("Se esperaba ',' o ']' en la posición " + (i - 1));
                }
            }
        }

        private String cadena() {
            if (i >= s.length() || s.charAt(i) != '"') {
                throw new IllegalArgumentException("Se esperaba una cadena en la posición " + i);
            }
            i++;
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (i >= s.length()) {
                    throw new IllegalArgumentException("Cadena sin cerrar");
                }
                char c = s.charAt(i++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (i >= s.length()) {
                    throw new IllegalArgumentException("Escape incompleto");
                }
                char e = s.charAt(i++);
                switch (e) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 > s.length()) {
                            throw new IllegalArgumentException("Escape unicode incompleto");
                        }
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                    }
                    default -> throw new IllegalArgumentException("Escape inválido: \\" + e);
                }
            }
        }

        private Object numero() {
            int inicio = i;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E' || (c >= '0' && c <= '9')) {
                    i++;
                } else {
                    break;
                }
            }
            String token = s.substring(inicio, i);
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Número inválido en la posición " + inicio);
            }
            if (token.indexOf('.') >= 0 || token.indexOf('e') >= 0 || token.indexOf('E') >= 0) {
                return Double.valueOf(token);
            }
            long valor = Long.parseLong(token);
            if (valor >= Integer.MIN_VALUE && valor <= Integer.MAX_VALUE) {
                return (int) valor;
            }
            return valor;
        }
    }
}
