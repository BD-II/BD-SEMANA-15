
package com.polleriaparedes.mongodb;

import com.mongodb.client.MongoDatabase; // Representa una base de datos de MongoDB.
import org.bson.Document;                // Permite representar documentos BSON en Java.

public class PolleriaParedesMongoDB {

    public static void main(String[] args) {

        // Crear una referencia para administrar la conexion.
        ConexionMongoDB conexion = null;

        try {

            // Establecer la conexion con MongoDB.
            conexion = new ConexionMongoDB();

            // Obtener la base de datos configurada.
            MongoDatabase bd = conexion.getBaseDatos();

            // Verificar que el servidor responda.
            bd.runCommand(new Document("ping", 1));

            // Crear el objeto que administra los platos.
            PlatoDAO platoDAO = new PlatoDAO(bd);

            System.out.println("=== POLLERIA PAREDES ===");
            System.out.println("Conexion con MongoDB establecida.");

            // Consultar los platos sin modificar la informacion.
            int totalPlatos = 0;

            for (Document plato : platoDAO.listarPlatos()) {
                totalPlatos++;
            }

            System.out.println("Total de platos registrados: "
                    + totalPlatos);

            System.out.println("Sistema preparado para continuar.");

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());

        } finally {

            // Cerrar el cliente al finalizar.
            if (conexion != null) {
                conexion.cerrarConexion();
            }
        }
    }
}
