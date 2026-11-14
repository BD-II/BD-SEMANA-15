package com.polleriaparedes.dao.postgres;

import com.polleriaparedes.config.ConexionPostgreSQL; // Usa la conexión existente.
import java.sql.Connection;                         // Conexión activa con PostgreSQL.
import java.sql.PreparedStatement;                  // Consulta SQL con parámetros.
import java.sql.ResultSet;                          // Filas devueltas por la consulta.
import java.sql.SQLException;                       // Errores que comunica JDBC.
import java.util.ArrayList;                         // Acumula las coincidencias.
import java.util.List;                              // Tipo de la lista que devuelve el DAO.

public class ClientePostgresDAO {

    // Cada resultado contiene los datos necesarios para elegir al cliente.
    public record ClienteEncontrado(
            String idCliente, String nombre, String apellido, String telefonoFinal
            ) {

    }

    public record ResultadoAlta(ClienteEncontrado cliente, boolean creado) {

    }

    public List<ClienteEncontrado> buscarPorNombre(String texto) throws SQLException {
        // Esperar al menos dos caracteres antes de consultar.
        if (texto == null || texto.strip().length() < 2) {
            return List.of();
        }

        String sql = """
            SELECT id_cliente, nombre, apellido, RIGHT(telefono, 4) AS telefono_final
            FROM public.cliente
            WHERE STRPOS(LOWER(nombre || ' ' || apellido), LOWER(?)) > 0
            ORDER BY apellido, nombre, id_cliente
            LIMIT 20
            """;

        List<ClienteEncontrado> clientes = new ArrayList<>();

        try (Connection conexion = ConexionPostgreSQL.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // JDBC asigna el texto al signo ? como un valor de búsqueda.
            sentencia.setString(1, texto.strip());

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    clientes.add(new ClienteEncontrado(
                            filas.getString("id_cliente"),
                            filas.getString("nombre"),
                            filas.getString("apellido"),
                            filas.getString("telefono_final")
                    ));
                }
            }
        }

        return clientes;
    }

    public ResultadoAlta registrarCliente(
            String nombre, String apellido, String telefono, String direccion
    ) throws SQLException {
        String insertar = """
        INSERT INTO public.cliente (id_cliente, nombre, apellido, telefono, direccion)
        VALUES (
            'C' || to_char(nextval('public.seq_cliente'), 'FM999999000'),
            ?, ?, ?, ?
        )
        ON CONFLICT (
            LOWER(BTRIM(nombre)),
            LOWER(BTRIM(apellido)),
            BTRIM(telefono),
            LOWER(BTRIM(direccion))
        ) DO NOTHING
        RETURNING id_cliente, nombre, apellido, RIGHT(telefono, 4) AS telefono_final
        """;

        String buscarFicha = """
        SELECT id_cliente, nombre, apellido, RIGHT(telefono, 4) AS telefono_final
        FROM public.cliente
        WHERE LOWER(BTRIM(nombre)) = LOWER(BTRIM(?))
          AND LOWER(BTRIM(apellido)) = LOWER(BTRIM(?))
          AND BTRIM(telefono) = BTRIM(?)
          AND LOWER(BTRIM(direccion)) = LOWER(BTRIM(?))
        """;

        try (Connection conexion = ConexionPostgreSQL.conectar()) {
            // Si la ficha es nueva, RETURNING devuelve el código recién creado.
            try (PreparedStatement sentencia = conexion.prepareStatement(insertar)) {
                sentencia.setString(1, nombre);
                sentencia.setString(2, apellido);
                sentencia.setString(3, telefono);
                sentencia.setString(4, direccion);

                try (ResultSet fila = sentencia.executeQuery()) {
                    if (fila.next()) {
                        return new ResultadoAlta(leerCliente(fila), true);
                    }
                }
            }

            // Si el índice detectó una coincidencia, buscar su código existente.
            try (PreparedStatement sentencia = conexion.prepareStatement(buscarFicha)) {
                sentencia.setString(1, nombre);
                sentencia.setString(2, apellido);
                sentencia.setString(3, telefono);
                sentencia.setString(4, direccion);

                try (ResultSet fila = sentencia.executeQuery()) {
                    if (fila.next()) {
                        return new ResultadoAlta(leerCliente(fila), false);
                    }
                }
            }
        }

        throw new SQLException("No se pudo recuperar la ficha del cliente.");
    }

    private ClienteEncontrado leerCliente(ResultSet fila) throws SQLException {
        return new ClienteEncontrado(
                fila.getString("id_cliente"),
                fila.getString("nombre"),
                fila.getString("apellido"),
                fila.getString("telefono_final")
        );
    }
}
