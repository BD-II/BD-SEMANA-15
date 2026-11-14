package com.polleriaparedes.pruebas;

// Importa el DAO que consulta PostgreSQL.
import com.polleriaparedes.dao.postgres.PlatoPostgresDAO;

// Importa el modelo que representa cada plato.
import com.polleriaparedes.modelo.Plato;

// Permite utilizar listas de objetos.
import java.util.List;

/**
 * Prueba de lectura de platos desde PostgreSQL.
 */
public class PruebaPlatoPostgresDAO {

    public static void main(String[] args) {

        try {

            // Crea el DAO de PostgreSQL.
            PlatoPostgresDAO dao = new PlatoPostgresDAO();

            // Recupera los platos como objetos del modelo.
            List<Plato> platos = dao.listarPlatos();

            // Recorre los objetos obtenidos.
            for (Plato plato : platos) {

                // toString() define como se imprime cada plato.
                System.out.println(plato);
            }

            // Muestra la cantidad de registros.
            System.out.println("-----------------------------");
            System.out.println(
                    "Total de platos: " + platos.size()
            );

        } catch (Exception e) {

            // Muestra el error si falla la consulta.
            System.err.println(
                    "Error al consultar platos: " + e.getMessage()
            );
        }
    }
}
