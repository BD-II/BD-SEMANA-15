package com.polleriaparedes.pruebas;

import com.polleriaparedes.config.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PruebaBaseDatosActiva {

    public static void main(String[] args) {

        String sql = """
            SELECT
                current_database() AS base_datos,
                current_user AS usuario,
                (SELECT COUNT(*) FROM public.pedido)
                    AS total_pedidos
            """;

        try (
                Connection conexion
                = ConexionPostgreSQL.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(sql); ResultSet resultado
                = sentencia.executeQuery()) {

            if (resultado.next()) {

                String baseDatos
                        = resultado.getString("base_datos");

                System.out.println(
                        "Base de datos conectada: " + baseDatos
                );

                System.out.println(
                        "Usuario: "
                        + resultado.getString("usuario")
                );

                System.out.println(
                        "Total de pedidos: "
                        + resultado.getInt("total_pedidos")
                );

                // Comprobar que estamos en la base de pruebas.
                if ("PolleriaParedesBD_Pruebas".equals(baseDatos)) {

                    System.out.println(
                            "VERIFICACION EXITOSA: "
                            + "estamos trabajando con la base de pruebas."
                    );

                } else {

                    System.out.println(
                            "ADVERTENCIA: no estamos conectados "
                            + "a la base de pruebas."
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error de conexion: " + e.getMessage()
            );
        }
    }
}
