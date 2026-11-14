package com.polleriaparedes.pruebas;

// DAO que permite registrar pedidos en PostgreSQL.
import com.polleriaparedes.dao.postgres.PedidoPostgresDAO;

// Representa una coleccion de pares clave-valor.
import java.util.Map;

// Conserva el orden en el que agregamos los platos.
import java.util.LinkedHashMap;

// Permite manejar errores SQL.
import java.sql.SQLException;

public class PruebaRollbackPedidoPostgres {

    public static void main(String[] args) {

        // Crear el DAO que ejecutara la transaccion.
        PedidoPostgresDAO dao = new PedidoPostgresDAO();

        // Preparar un pedido con dos platos.
        Map<String, Integer> platos = new LinkedHashMap<>();

        // Primer plato: existe y tiene receta.
        platos.put("P001", 1);

        // Segundo plato: codigo inexistente utilizado
        // intencionalmente para comprobar el rollback.
        platos.put("P999", 1);

        System.out.println("================================");
        System.out.println("PRUEBA DE ROLLBACK POSTGRESQL");
        System.out.println("================================");

        try {

            // Intentar registrar ambos platos.
            String idPedido = dao.registrarPedido(
                    "C001",
                    "E001",
                    "Presencial",
                    platos
            );

            // No esperamos llegar a esta instruccion.
            System.out.println(
                    "ATENCION: pedido registrado: " + idPedido
            );

        } catch (SQLException e) {

            // PostgreSQL debe rechazar el plato inexistente.
            System.out.println(
                    "Error esperado de PostgreSQL: "
                    + e.getMessage()
            );

            // El DAO debe haber ejecutado rollback().
            System.out.println(
                    "Verificar que la transaccion fue revertida."
            );

        } catch (IllegalArgumentException e) {

            // Capturar errores de validacion de Java.
            System.out.println(
                    "Error de validacion: " + e.getMessage()
            );
        }
    }
}
