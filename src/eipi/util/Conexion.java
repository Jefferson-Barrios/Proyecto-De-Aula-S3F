package eipi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/eipi"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    public static void main(String[] args) {
        try (Connection c = getConexion()) {
            System.out.println("Conexion exitosa a: " + c.getCatalog());
        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        }
    }
}