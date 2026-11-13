package com.polleriaparedes.pruebas;

// Importamos el DAO que contiene el metodo registrarPedido().
import com.polleriaparedes.dao.postgres.PedidoPostgresDAO;

// Permite almacenar datos mediante pares clave-valor.
import java.util.Map;

// Implementacion de Map que conserva el orden de insercion.
import java.util.LinkedHashMap;

// Permite manejar errores relacionados con PostgreSQL.
import java.sql.SQLException;

public class PruebaRegistrarPedidoPostgres {

    public static void main(String[] args) {

        // Crear un objeto de nuestro DAO de PostgreSQL.
        PedidoPostgresDAO dao = new PedidoPostgresDAO();

        // Crear una coleccion de platos y cantidades.
        // String representa el codigo del plato.
        // Integer representa la cantidad solicitada.
        Map<String, Integer> platos = new LinkedHashMap<>();

        // Agregar una unidad del plato P001.
        platos.put("P001", 1);

        // Datos que utilizaremos para registrar el pedido.
        String idCliente = "C001";
        String idEmpleado = "E001";
        String tipoPedido = "Presencial";

        System.out.println("================================");
        System.out.println("PRUEBA DE REGISTRO DE PEDIDO");
        System.out.println("================================");

        try {

            // Invocar el metodo CREATE de PedidoPostgresDAO.
            // Este metodo administra la transaccion JDBC.
            String idPedido = dao.registrarPedido(
                    idCliente,
                    idEmpleado,
                    tipoPedido,
                    platos
            );

            // Mostrar el codigo generado por PostgreSQL.
            System.out.println("Pedido registrado correctamente.");
            System.out.println("Codigo: " + idPedido);
            System.out.println("Cliente: " + idCliente);
            System.out.println("Empleado: " + idEmpleado);
            System.out.println("Tipo: " + tipoPedido);
            System.out.println("Plato: P001");
            System.out.println("Cantidad: 1");

        } catch (SQLException e) {

            // Mostrar el error comunicado por PostgreSQL.
            System.out.println(
                    "Error SQL al registrar el pedido: "
                    + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            // Mostrar errores de validacion de datos.
            System.out.println(
                    "Datos invalidos: " + e.getMessage()
            );
        }
    }
}
