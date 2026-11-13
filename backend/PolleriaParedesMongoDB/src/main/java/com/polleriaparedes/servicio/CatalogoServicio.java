package com.polleriaparedes.servicio;

// Permite trabajar con una base de datos MongoDB.
import com.mongodb.client.MongoDatabase;

// Permite recorrer documentos recuperados desde MongoDB.
import com.mongodb.client.FindIterable;

// Representa los documentos BSON de MongoDB.
import org.bson.Document;

// Importa el DAO encargado de consultar MongoDB.
import com.polleriaparedes.dao.mongo.PlatoDAO;

// Importa el DAO encargado de consultar PostgreSQL.
import com.polleriaparedes.dao.postgres.PlatoPostgresDAO;

// Importa el modelo de platos oficiales de PostgreSQL.
import com.polleriaparedes.modelo.Plato;

// Permite utilizar listas de objetos.
import java.util.List;

// Permite crear listas dinamicas.
import java.util.ArrayList;

// Permite manejar errores provenientes de PostgreSQL.
import java.sql.SQLException;

// Permite relacionar identificadores con documentos.
import java.util.Map;

// Implementacion de Map basada en una tabla hash.
import java.util.HashMap;

/**
 * Servicio encargado de integrar el catalogo de PostgreSQL con la informacion
 * complementaria de MongoDB.
 *
 * PostgreSQL proporciona los datos oficiales. MongoDB proporciona los datos
 * descriptivos.
 */
public class CatalogoServicio {

    // DAO que consulta los platos de PostgreSQL.
    private final PlatoPostgresDAO postgresDAO;

    // DAO que consulta los documentos de MongoDB.
    private final PlatoDAO mongoDAO;

    /**
     * Constructor del servicio.
     *
     * Recibe la base de datos MongoDB ya seleccionada.
     */
    public CatalogoServicio(MongoDatabase bdMongo) {

        // Inicializa el DAO de PostgreSQL.
        postgresDAO = new PlatoPostgresDAO();

        // Inicializa el DAO de MongoDB.
        mongoDAO = new PlatoDAO(bdMongo);
    }

    /**
     * Consulta ambos motores y combina sus datos.
     *
     * @return Lista de documentos con el catalogo integrado.
     * @throws SQLException Si ocurre un error en PostgreSQL.
     */
    public List<Document> listarCatalogoIntegrado()
            throws SQLException {

        // Recupera los platos oficiales desde PostgreSQL.
        List<Plato> platosPostgres
                = postgresDAO.listarPlatos();

        // Recupera los documentos complementarios de MongoDB.
        FindIterable<Document> platosMongo
                = mongoDAO.listarPlatos();

        // Mapa que relaciona cada ID con su documento MongoDB.
        Map<String, Document> documentosMongo
                = new HashMap<>();

        // Recorre los documentos obtenidos de MongoDB.
        for (Document documento : platosMongo) {

            // Obtiene el identificador compartido.
            String id = documento.getString("id_plato");

            // Almacena el documento si tiene un ID valido.
            if (id != null && !id.isBlank()) {
                documentosMongo.put(id, documento);
            }
        }

        // Lista donde guardaremos el catalogo combinado.
        List<Document> catalogo = new ArrayList<>();

        // Recorre los platos oficiales de PostgreSQL.
        for (Plato plato : platosPostgres) {

            // Busca la informacion complementaria en MongoDB.
            Document complemento
                    = documentosMongo.get(plato.getIdPlato());

            // Construye el documento con los datos oficiales.
            Document integrado = new Document()
                    .append("id", plato.getIdPlato())
                    .append("nombre", plato.getNombrePlato())
                    .append("categoria", plato.getCategoria())
                    .append("precio", plato.getPrecio());

            if (complemento != null) {

                // Recupera la descripcion almacenada en MongoDB.
                // Es un campo de tipo String.
                integrado.append(
                        "descripcion",
                        complemento.getString("descripcion")
                );

                // Recupera el arreglo completo de imagenes.
                // get() conserva la estructura original del campo.
                integrado.append(
                        "imagenes",
                        complemento.get("imagenes")
                );

                // Recupera las etiquetas del plato.
                // MongoDB almacena este campo como un arreglo.
                integrado.append(
                        "etiquetas",
                        complemento.get("etiquetas")
                );

                // Recupera las presentaciones disponibles.
                integrado.append(
                        "presentaciones",
                        complemento.get("presentaciones")
                );

                // Recupera el estado del documento.
                integrado.append(
                        "estado",
                        complemento.getString("estado")
                );
            }

            // Agrega el documento integrado al resultado.
            catalogo.add(integrado);
        }

        // Devuelve el catalogo completo.
        return catalogo;
    }
}
