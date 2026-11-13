package com.polleriaparedes.dao.postgres;

// Permite establecer una conexion activa con PostgreSQL.
import java.sql.Connection;

// Permite ejecutar consultas SQL con parametros.
import java.sql.PreparedStatement;

// Representa los resultados devueltos por una consulta.
import java.sql.ResultSet;

// Permite manejar errores relacionados con SQL.
import java.sql.SQLException;

// Permite almacenar registros en una lista.
import java.util.ArrayList;

// Representa una lista de elementos.
import java.util.List;

// Permite almacenar datos mediante pares clave-valor.
import java.util.Map;

// Implementacion de Map que conserva el orden de los campos.
import java.util.LinkedHashMap;

// Importamos nuestra clase de conexion PostgreSQL.
import com.polleriaparedes.config.ConexionPostgreSQL;

import java.math.BigDecimal;  // Representa importes monetarios con precision.
import java.util.Objects;     // Permite comprobar valores nulos.

public class PedidoPostgresDAO {

    // ==================================================
    // READ: CONSULTAR TODOS LOS PEDIDOS
    // ==================================================
    public List<Map<String, Object>> listarPedidos()
            throws SQLException {

        // Lista donde guardaremos los pedidos consultados.
        List<Map<String, Object>> pedidos = new ArrayList<>();

        // Consulta SQL para obtener los pedidos existentes.
        String sql = """
            SELECT
                id_pedido,
                id_cliente,
                id_empleado,
                fecha,
                tipo_pedido,
                estado,
                legado
            FROM public.pedido
            ORDER BY fecha DESC, id_pedido DESC
            """;

        // try-with-resources cierra automaticamente
        // la conexion y los recursos SQL utilizados.
        try (
                // Abrir conexion con PostgreSQL.
                Connection conexion
                = ConexionPostgreSQL.conectar(); // Preparar la consulta SQL.
                 PreparedStatement sentencia
                = conexion.prepareStatement(sql); // Ejecutar la consulta y obtener resultados.
                 ResultSet resultado
                = sentencia.executeQuery()) {

            // Recorrer cada fila obtenida.
            while (resultado.next()) {

                // Map representa un pedido mediante
                // nombres de campos y sus valores.
                Map<String, Object> pedido
                        = new LinkedHashMap<>();

                pedido.put(
                        "id_pedido",
                        resultado.getString("id_pedido")
                );

                pedido.put(
                        "id_cliente",
                        resultado.getString("id_cliente")
                );

                pedido.put(
                        "id_empleado",
                        resultado.getString("id_empleado")
                );

                pedido.put(
                        "fecha",
                        resultado.getTimestamp("fecha")
                );

                pedido.put(
                        "tipo_pedido",
                        resultado.getString("tipo_pedido")
                );

                pedido.put(
                        "estado",
                        resultado.getInt("estado")
                );

                pedido.put(
                        "legado",
                        resultado.getBoolean("legado")
                );

                // Agregar el pedido a la lista.
                pedidos.add(pedido);
            }
        }

        // Devolver todos los pedidos encontrados.
        return pedidos;
    }

    // ==================================================
    // READ: CONSULTAR DETALLES DE UN PEDIDO
    // ==================================================
    public List<Map<String, Object>> listarDetalles(
            String idPedido) throws SQLException {

        // Lista donde almacenaremos los detalles.
        List<Map<String, Object>> detalles = new ArrayList<>();

        // Consultar los detalles junto con el nombre
        // oficial del plato almacenado en PostgreSQL.
        String sql = """
            SELECT
                d.id_detalle,
                d.id_pedido,
                d.id_plato,
                p.nombre_plato,
                d.cantidad,
                d.precio_unitario,
                d.subtotal
            FROM public.detalle_pedido d
            INNER JOIN public.plato p
                ON d.id_plato = p.id_plato
            WHERE d.id_pedido = ?
            ORDER BY d.id_detalle
            """;

        try (
                Connection conexion
                = ConexionPostgreSQL.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(sql)) {

            // setString() reemplaza el signo ?
            // por el codigo del pedido recibido.
            // Esto evita concatenar datos directamente
            // dentro de la consulta SQL.
            sentencia.setString(1, idPedido);

            try (ResultSet resultado
                    = sentencia.executeQuery()) {

                while (resultado.next()) {

                    Map<String, Object> detalle
                            = new LinkedHashMap<>();

                    detalle.put(
                            "id_detalle",
                            resultado.getString("id_detalle")
                    );

                    detalle.put(
                            "id_pedido",
                            resultado.getString("id_pedido")
                    );

                    detalle.put(
                            "id_plato",
                            resultado.getString("id_plato")
                    );

                    detalle.put(
                            "nombre_plato",
                            resultado.getString("nombre_plato")
                    );

                    detalle.put(
                            "cantidad",
                            resultado.getInt("cantidad")
                    );

                    detalle.put(
                            "precio_unitario",
                            resultado.getBigDecimal("precio_unitario")
                    );

                    detalle.put(
                            "subtotal",
                            resultado.getBigDecimal("subtotal")
                    );

                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

    // ==================================================
    // CREATE: REGISTRAR UN PEDIDO Y SUS DETALLES
    // ==================================================
    public String registrarPedido(
            String idCliente,
            String idEmpleado,
            String tipoPedido,
            Map<String, Integer> platos
    ) throws SQLException {

        // Comprobar los datos obligatorios del pedido.
        if (idCliente == null || idCliente.isBlank()
                || idEmpleado == null || idEmpleado.isBlank()
                || tipoPedido == null || tipoPedido.isBlank()) {

            throw new IllegalArgumentException(
                    "Cliente, empleado y tipo de pedido son obligatorios."
            );
        }

        // Verificar que se haya seleccionado al menos un plato.
        if (platos == null || platos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un plato."
            );
        }

        // Validar los codigos y cantidades antes de escribir.
        for (Map.Entry<String, Integer> item : platos.entrySet()) {

            if (item.getKey() == null || item.getKey().isBlank()
                    || item.getValue() == null || item.getValue() <= 0) {

                throw new IllegalArgumentException(
                        "Los platos deben tener codigo y cantidad positiva."
                );
            }
        }

        // Generar el codigo del pedido utilizando su secuencia.
        String sqlCodigoPedido = """
            SELECT 'PD' || LPAD(
                nextval('public.seq_pedido')::text, 3, '0'
            )
            """;

        // Generar el codigo de cada detalle.
        String sqlCodigoDetalle = """
            SELECT 'D' || LPAD(
                nextval('public.seq_detalle_pedido')::text, 3, '0'
            )
            """;

        // Registrar la cabecera del pedido.
        String sqlPedido = """
            INSERT INTO public.pedido (
                id_pedido,
                id_empleado,
                id_cliente,
                fecha,
                tipo_pedido,
                estado,
                legado
            )
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, 0, false)
            """;

        // Registrar un plato dentro del pedido.
        // Los triggers validaran el precio, subtotal e inventario.
        String sqlDetalle = """
            INSERT INTO public.detalle_pedido (
                id_detalle,
                id_pedido,
                id_plato,
                cantidad,
                precio_unitario,
                subtotal
            )
            VALUES (?, ?, ?, ?, 0, 0)
            """;

        // Abrir una conexion con la base configurada.
        try (Connection conexion = ConexionPostgreSQL.conectar()) {

            // Iniciar una transaccion manual.
            conexion.setAutoCommit(false);

            try {

                String idPedido;

                // Solicitar el siguiente identificador del pedido.
                try (PreparedStatement sentencia
                        = conexion.prepareStatement(sqlCodigoPedido); ResultSet resultado = sentencia.executeQuery()) {

                    resultado.next();
                    idPedido = resultado.getString(1);
                }

                // Insertar el pedido con estado pendiente.
                try (PreparedStatement sentencia
                        = conexion.prepareStatement(sqlPedido)) {

                    sentencia.setString(1, idPedido);
                    sentencia.setString(2, idEmpleado);
                    sentencia.setString(3, idCliente);
                    sentencia.setString(4, tipoPedido);

                    sentencia.executeUpdate();
                }

                // Registrar los platos seleccionados.
                for (Map.Entry<String, Integer> item : platos.entrySet()) {

                    String idDetalle;

                    // Obtener el siguiente identificador del detalle.
                    try (PreparedStatement sentencia
                            = conexion.prepareStatement(sqlCodigoDetalle); ResultSet resultado = sentencia.executeQuery()) {

                        resultado.next();
                        idDetalle = resultado.getString(1);
                    }

                    // Insertar el detalle del pedido.
                    try (PreparedStatement sentencia
                            = conexion.prepareStatement(sqlDetalle)) {

                        sentencia.setString(1, idDetalle);
                        sentencia.setString(2, idPedido);
                        sentencia.setString(3, item.getKey());
                        sentencia.setInt(4, item.getValue());

                        sentencia.executeUpdate();
                    }
                }

                // Confirmar todos los cambios de la transaccion.
                conexion.commit();

                // Devolver el codigo del pedido registrado.
                return idPedido;

            } catch (SQLException | RuntimeException e) {

                // Deshacer los cambios si falla cualquier operacion.
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }

                throw e;
            }
        }
    }

    public void cambiarEstado(String idPedido, int estado) throws SQLException {
        // CALL ejecuta el procedimiento que ya existe en PostgreSQL.
        // Los signos ? se reemplazan con el código del pedido y el nuevo estado.
        String sql = "CALL public.sp_cambiar_estado_pedido(?, ?)";

        try (Connection conexion = ConexionPostgreSQL.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, idPedido);
            sentencia.setInt(2, estado);
            sentencia.execute();
        }
    }

    public void registrarPago(String idPedido, String metodoPago) throws SQLException {
        // Obtener el importe desde los detalles guardados y asignar los identificadores.
        // El procedimiento inserta la factura y el trigger actualiza el estado.
        String sql = """
        CALL public.sp_registrar_pago_pedido(
            ('F' || to_char(nextval('public.seq_factura'), 'FM999999000'))::varchar,
            ?,
            nextval('public.seq_numero_comprobante')::integer,
            ?,
            public.fn_calcular_total_pedido(?)
        )
        """;

        try (Connection conexion = ConexionPostgreSQL.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, idPedido);
            sentencia.setString(2, metodoPago);
            sentencia.setString(3, idPedido);
            sentencia.execute();
        }
    }

}
