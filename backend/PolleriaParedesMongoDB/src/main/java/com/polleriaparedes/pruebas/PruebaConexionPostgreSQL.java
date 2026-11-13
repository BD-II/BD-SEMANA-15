package com.polleriaparedes.pruebas;

// Representa una conexion activa con la base de datos PostgreSQL.
import com.polleriaparedes.config.ConexionPostgreSQL;

import java.sql.Connection;

// Permite almacenar y consultar los resultados devueltos por SQL.
import java.sql.ResultSet;

// Permite ejecutar instrucciones SQL desde Java.
import java.sql.Statement;

public class PruebaConexionPostgreSQL {

    // Metodo principal que inicia nuestra prueba de conexion.
    public static void main(String[] args) {

        // try-with-resources cierra automaticamente los recursos JDBC.
        // Esto evita dejar conexiones abiertas innecesariamente.
        try (
                // Solicita la conexion mediante ConexionPostgreSQL.java.
                Connection conexion = ConexionPostgreSQL.conectar(); // Crea un objeto que permite ejecutar consultas SQL.
                 Statement sentencia = conexion.createStatement(); // Ejecuta una consulta para conocer la base y usuario actuales.
                // Esta consulta es de lectura: no modifica registros.
                 ResultSet resultado = sentencia.executeQuery(
                        "SELECT current_database(), current_user"
                )) {

            // next() avanza hasta la primera fila del resultado.
            // Devuelve true si existe una fila disponible.
            if (resultado.next()) {

                System.out.println("CONEXION EXITOSA A POSTGRESQL");

                // getString(1) obtiene la primera columna:
                // el nombre de la base de datos.
                System.out.println(
                        "Base de datos: " + resultado.getString(1)
                );

                // getString(2) obtiene la segunda columna:
                // el usuario conectado.
                System.out.println(
                        "Usuario: " + resultado.getString(2)
                );
            }

        } catch (Exception e) {

            // Captura y muestra los errores de conexion o consulta.
            System.err.println(
                    "Error de conexion: " + e.getMessage()
            );
        }
    }
}
