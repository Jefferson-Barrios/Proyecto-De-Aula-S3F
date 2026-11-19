package dao;

final class Ordenes {

    private Ordenes() {
    }

    static String limpio(String valor) {
        return valor == null ? "" : valor;
    }

    static int grado(String valor) {
        if (valor == null) {
            return 0;
        }
        int i = 0;
        while (i < valor.length() && Character.isDigit(valor.charAt(i))) {
            i++;
        }
        return i == 0 ? 0 : Integer.parseInt(valor.substring(0, i));
    }
}
