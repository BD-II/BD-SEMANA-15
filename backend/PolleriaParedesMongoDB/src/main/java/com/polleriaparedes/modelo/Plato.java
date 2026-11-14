package com.polleriaparedes.modelo;

// BigDecimal permite representar valores decimales con precision.
// Es especialmente recomendable para precios y otros importes.
import java.math.BigDecimal;

/**
 * Modelo que representa un plato registrado en PostgreSQL.
 *
 * Contiene los datos oficiales del catalogo: identificador, nombre, categoria y
 * precio.
 */
public class Plato {

    // Identificador unico del plato. Ejemplo: P001.
    private String idPlato;

    // Nombre oficial registrado en PostgreSQL.
    private String nombrePlato;

    // Categoria del plato: Entrada, Fondo, Postre o Bebida.
    private String categoria;

    // Precio oficial del plato.
    private BigDecimal precio;

    /**
     * Constructor que recibe los datos del plato.
     *
     * Se utiliza para crear un objeto Plato con sus atributos.
     */
    public Plato(String idPlato, String nombrePlato,
            String categoria, BigDecimal precio) {

        this.idPlato = idPlato;
        this.nombrePlato = nombrePlato;
        this.categoria = categoria;
        this.precio = precio;
    }

    // GETTERS: permiten consultar los valores del objeto.
    // Devuelve el identificador del plato.
    public String getIdPlato() {
        return idPlato;
    }

    // Devuelve el nombre del plato.
    public String getNombrePlato() {
        return nombrePlato;
    }

    // Devuelve la categoria del plato.
    public String getCategoria() {
        return categoria;
    }

    // Devuelve el precio oficial del plato.
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * Representacion textual del plato.
     *
     * Facilita mostrar los datos durante las pruebas.
     */
    @Override
    public String toString() {

        return idPlato + " | "
                + nombrePlato + " | "
                + categoria + " | S/ "
                + precio;
    }
}
