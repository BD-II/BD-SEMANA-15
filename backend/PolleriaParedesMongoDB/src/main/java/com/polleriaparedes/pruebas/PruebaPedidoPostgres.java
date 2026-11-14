package com.polleriaparedes.pruebas;

// Importamos el DAO encargado de consultar los pedidos.
import com.polleriaparedes.dao.postgres.PedidoPostgresDAO;

// Permite manejar errores producidos por consultas SQL.
import java.sql.SQLException;

// Permite trabajar con listas de registros.
import java.util.List;

// Permite representar cada registro mediante campos y valores.
import java.util.Map;

public class PruebaPedidoPostgres {

    // main() = Punto de entrada para ejecutar esta prueba.
    public static void main(String[] args) {

        // Crear una instancia de nuestro DAO.
        // Esta instancia permite llamar a sus metodos.
        PedidoPostgresDAO dao = new PedidoPostgresDAO();

        try {

            System.out.println("================================");
            System.out.println("CONSULTA DE PEDIDOS POSTGRESQL");
            System.out.println("================================");

            // READ: consultar los pedidos registrados.
            List<Map<String, Object>> pedidos
                    = dao.listarPedidos();

            // Mostrar la cantidad total de pedidos encontrados.
            System.out.println(
                    "Total de pedidos: " + pedidos.size()
            );

            System.out.println("--------------------------------");

            // Mostrar solamente los primeros cinco pedidos
            // para evitar una salida demasiado extensa.
            int limite = Math.min(5, pedidos.size());

            for (int i = 0; i < limite; i++) {

                // Obtener un pedido de la lista.
                Map<String, Object> pedido = pedidos.get(i);

                System.out.println(
                        "Pedido: " + pedido.get("id_pedido")
                        + " | Cliente: " + pedido.get("id_cliente")
                        + " | Empleado: " + pedido.get("id_empleado")
                        + " | Estado: " + pedido.get("estado")
                        + " | Legado: " + pedido.get("legado")
                );
            }

            System.out.println();
            System.out.println("================================");
            System.out.println("DETALLES DEL PEDIDO PD001");
            System.out.println("================================");

            // READ: consultar los detalles de un pedido especifico.
            List<Map<String, Object>> detalles
                    = dao.listarDetalles("PD001");

            // Mostrar cuantos detalles pertenecen al pedido.
            System.out.println(
                    "Total de detalles: " + detalles.size()
            );

            System.out.println("--------------------------------");

            // Recorrer los detalles obtenidos.
            for (Map<String, Object> detalle : detalles) {

                System.out.println(
                        "Detalle: " + detalle.get("id_detalle")
                        + " | Plato: " + detalle.get("nombre_plato")
                        + " | Cantidad: " + detalle.get("cantidad")
                        + " | Precio: S/ " + detalle.get("precio_unitario")
                        + " | Subtotal: S/ " + detalle.get("subtotal")
                );
            }

        } catch (SQLException e) {

            // Capturar errores de conexion o consultas SQL.
            System.out.println(
                    "Error al consultar PostgreSQL: "
                    + e.getMessage()
            );

            // Mostrar informacion tecnica para identificar el error.
            e.printStackTrace();
        }
    }
}
