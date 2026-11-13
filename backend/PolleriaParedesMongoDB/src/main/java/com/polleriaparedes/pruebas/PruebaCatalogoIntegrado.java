package com.polleriaparedes.pruebas;

// Importa nuestra clase encargada de conectarse a MongoDB.
import com.polleriaparedes.config.ConexionMongoDB;

// Importa el servicio que combina PostgreSQL y MongoDB.
import com.polleriaparedes.servicio.CatalogoServicio;

// Representa la base de datos seleccionada en MongoDB.
import com.mongodb.client.MongoDatabase;

// Representa un documento BSON recuperado desde MongoDB.
import org.bson.Document;

// Permite almacenar y recorrer listas de documentos.
import java.util.List;

/**
 * Prueba de integracion del catalogo.
 *
 * PostgreSQL proporciona: - Identificador. - Nombre. - Categoria. - Precio
 * oficial.
 *
 * MongoDB proporciona: - Imagen. - Descripcion. - Etiqueta. - Ingredientes.
 */
public class PruebaCatalogoIntegrado {

    // Metodo principal para ejecutar esta prueba.
    public static void main(String[] args) {

        // Declaramos la conexion fuera del bloque try
        // para poder cerrarla posteriormente.
        ConexionMongoDB conexionMongo = null;

        try {

            // 1. Conectarse a la base de datos principal.
            conexionMongo = new ConexionMongoDB(
                    "PolleriaParedes_Catalogo"
            );

            // 2. Obtener la referencia a la base MongoDB.
            MongoDatabase bdMongo
                    = conexionMongo.getBaseDatos();

            // 3. Crear el servicio de integracion.
            // Le entregamos la base de datos MongoDB.
            CatalogoServicio servicio
                    = new CatalogoServicio(bdMongo);

            // 4. Consultar ambos motores y combinar los datos.
            List<Document> catalogo
                    = servicio.listarCatalogoIntegrado();

            // 5. Mostrar los documentos integrados.
            for (Document plato : catalogo) {

                // getString() recupera campos de texto.
                String id = plato.getString("id");
                String nombre = plato.getString("nombre");
                // Recupera el arreglo de imagenes del documento integrado.
                Object imagenes = plato.get("imagenes");

                // get() recupera el valor sin asumir su tipo.
                // El precio oficial procede de PostgreSQL.
                Object precio = plato.get("precio");

                // Muestra el arreglo de imagenes recuperado desde MongoDB.
                System.out.println(
                        id + " | "
                        + nombre + " | S/ "
                        + precio + " | Imagenes: "
                        + imagenes
                );
            }

            // 6. Mostrar la cantidad de platos integrados.
            System.out.println(
                    "----------------------------------"
            );

            System.out.println(
                    "Total de platos integrados: "
                    + catalogo.size()
            );

        } catch (Exception e) {

            // Captura errores de conexion o consultas.
            System.err.println(
                    "Error al integrar el catalogo: "
                    + e.getMessage()
            );

            // Muestra detalles tecnicos para diagnosticar.
            e.printStackTrace();

        } finally {

            // Este bloque se ejecuta incluso si ocurre
            // una excepcion durante la prueba.
            if (conexionMongo != null) {

                // Libera los recursos del cliente MongoDB.
                conexionMongo.cerrarConexion();
            }
        }
    }
}
