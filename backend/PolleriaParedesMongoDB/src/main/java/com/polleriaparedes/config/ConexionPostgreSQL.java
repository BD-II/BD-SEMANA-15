package com.polleriaparedes.config;

// Permite representar una conexion activa con PostgreSQL.
import java.sql.Connection;

// Permite establecer conexiones JDBC.
import java.sql.DriverManager;

// Permite manejar errores relacionados con SQL.
import java.sql.SQLException;

public class ConexionPostgreSQL {

    // Base de datos utilizada por defecto durante el desarrollo.
    private static final String BD_PREDETERMINADA
            = "PolleriaParedesBD_Pruebas";

    // Nombre de la variable de entorno que permite elegir la BD.
    private static final String VARIABLE_BD
            = "POLLERIA_PG_DATABASE";

    // Nombre de la variable de entorno que contiene la contraseña.
    private static final String VARIABLE_PASSWORD
            = "POLLERIA_DB_PASSWORD";

    // Usuario utilizado para conectarse a PostgreSQL.
    private static final String USUARIO = "postgres";

    // Metodo que establece una conexion con PostgreSQL.
    public static Connection conectar() throws SQLException {

        // Obtener el nombre de la base de datos desde el entorno.
        String nombreBD = System.getenv(VARIABLE_BD);

        // Si no se configuro, utilizar la base de pruebas.
        if (nombreBD == null || nombreBD.isBlank()) {
            nombreBD = BD_PREDETERMINADA;
        }

        // Construir la URL JDBC con la base seleccionada.
        String url = "jdbc:postgresql://localhost:5432/"
                + nombreBD;

        // Obtener la contraseña desde la variable de entorno.
        String password = System.getenv(VARIABLE_PASSWORD);

        // Verificar que la contraseña este configurada.
        if (password == null || password.isBlank()) {
            throw new SQLException(
                    "Falta configurar la variable POLLERIA_DB_PASSWORD."
            );
        }

        // Establecer y devolver la conexion JDBC.
        return DriverManager.getConnection(
                url,
                USUARIO,
                password
        );
    }
}
