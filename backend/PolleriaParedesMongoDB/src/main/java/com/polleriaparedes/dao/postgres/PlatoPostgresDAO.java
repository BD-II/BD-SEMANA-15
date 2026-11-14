package com.polleriaparedes.dao.postgres;

// Importa nuestra clase encargada de abrir conexiones PostgreSQL.
import com.polleriaparedes.config.ConexionPostgreSQL;

// Importa el modelo que representa un plato.
import com.polleriaparedes.modelo.Plato;

// Representa una conexion activa con PostgreSQL.
import java.sql.Connection;

// Permite preparar consultas SQL con parametros.
import java.sql.PreparedStatement;

// Permite recorrer las filas devueltas por PostgreSQL.
import java.sql.ResultSet;

// Permite manejar errores de consultas y conexiones SQL.
import java.sql.SQLException;

// Representa una lista de elementos.
import java.util.List;

// Permite crear una lista dinamica.
import java.util.ArrayList;

// Permite manejar precios con precision decimal.
import java.math.BigDecimal;

/**
 * DAO encargado de consultar los platos de PostgreSQL.
 *
 * PostgreSQL es la fuente oficial del identificador, nombre, categoria y
 * precio.
 */
public class PlatoPostgresDAO {

    // Consulta SQL para obtener los platos operativos.
    // PLDEMO se excluye porque es un registro de demostracion.
    private static final String SQL_LISTAR
            = "SELECT id_plato, nombre_plato, categoria, precio "
            + "FROM public.plato "
            + "WHERE id_plato <> ? "
            + "ORDER BY id_plato";

    /**
     * Consulta los platos y los convierte en objetos Plato.
     *
     * @return Lista de platos recuperados.
     * @throws SQLException Si ocurre un error en PostgreSQL.
     */
    public List<Plato> listarPlatos() throws SQLException {

        // Lista que almacenara los objetos obtenidos.
        List<Plato> platos = new ArrayList<>();

        // Abre la conexion y prepara la consulta.
        try (
                Connection conexion = ConexionPostgreSQL.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(SQL_LISTAR)) {

            // Sustituye el parametro SQL por el ID excluido.
            sentencia.setString(1, "PLDEMO");

            // Ejecuta la consulta.
            try (ResultSet resultado = sentencia.executeQuery()) {

                // Recorre cada fila devuelta por PostgreSQL.
                while (resultado.next()) {

                    // Obtiene los valores de las columnas.
                    String id = resultado.getString("id_plato");

                    String nombre
                            = resultado.getString("nombre_plato");

                    String categoria
                            = resultado.getString("categoria");

                    BigDecimal precio
                            = resultado.getBigDecimal("precio");

                    // Construye un objeto Plato con los datos.
                    Plato plato = new Plato(
                            id,
                            nombre,
                            categoria,
                            precio
                    );

                    // Agrega el objeto a la lista.
                    platos.add(plato);
                }
            }
        }

        // Devuelve los platos consultados.
        return platos;
    }
}
