/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * Esta clase estará orientada a las cuatro operaciones CRUD, no solamente a consultar o modificar.
 *
 * Operación	Método que implementaremos              Finalidad
 *
 * Create	registrarPlato()                        Insertar un nuevo plato.
 * Read         listarPlatos() y buscarPlato()          Consultar todos los platos o buscar uno por su código.
 * Update	actualizarDescripcion()                 Modificar la descripción de un plato.
 * Delete	eliminarPlato()                         Eliminar un documento de la colección.
 */
package com.polleriaparedes.mongodb;

import com.mongodb.client.MongoCollection;           // Representa una coleccion de MongoDB.
import com.mongodb.client.MongoDatabase;             // Permite acceder a una base de datos MongoDB.
import com.mongodb.client.FindIterable;              // Permite recorrer los resultados de una consulta.
import org.bson.Document;                            // Representa un documento BSON desde Java.
import com.mongodb.client.result.InsertOneResult;    // Contiene el resultado de una insercion.
import com.mongodb.MongoWriteException;              // Representa errores de escritura en MongoDB.
import com.mongodb.client.result.UpdateResult;       // Permite conocer el resultado de una actualizacion.
import com.mongodb.client.result.DeleteResult;       // Permite conocer el resultado de una eliminacion.
import java.util.Date;                               // Permite trabajar con fechas en Java.
import java.util.List;                               // Representa listas de elementos.

public class PlatoDAO {

    // Coleccion donde se almacenan los platos.
    private final MongoCollection<Document> coleccion;

    // Constructor: recibe la base de datos.
    public PlatoDAO(MongoDatabase bd) {
        coleccion = bd.getCollection("Catalogo_Platos");
    }

    // READ: consultar todos los platos.
    public FindIterable<Document> listarPlatos() {
        return coleccion.find()
                .sort(new Document("id_plato", 1));
    }

    // READ: buscar un plato por su codigo.
    public Document buscarPlato(String idPlato) {

        return coleccion.find(
                new Document("id_plato", idPlato)
        ).first();
    }

    // CREATE: registrar un nuevo plato en MongoDB.
    public boolean registrarPlato(Document nuevoPlato) {

        // Obtener el codigo del plato.
        String codigo = nuevoPlato.getString("id_plato");

        // Verificar que el codigo sea valido.
        // isBlank() = Comprueba si el código contiene únicamente espacios o está vacío.
        if (codigo == null || codigo.isBlank()) {
            System.out.println("El codigo del plato es obligatorio.");
            return false;
        }

        // Verificar que el plato no exista previamente.
        if (buscarPlato(codigo) != null) {
            System.out.println("Ya existe un plato con ese codigo.");
            return false;
        }

        try {

            // Insertar el documento en MongoDB.
            InsertOneResult resultado
                    = coleccion.insertOne(nuevoPlato);

            // Comprobar si MongoDB confirmo la insercion.
            return resultado.wasAcknowledged();

        } catch (MongoWriteException e) {

            System.out.println("Error al registrar el plato: "
                    + e.getMessage());

            return false;
        }
    }

    // UPDATE: modificar la descripcion de un plato.
    public boolean actualizarDescripcion(String idPlato,
            String nuevaDescripcion) {

        // Verificar que la descripcion sea valida.
        if (nuevaDescripcion == null
                || nuevaDescripcion.isBlank()) {

            System.out.println("La descripcion no puede estar vacia.");
            return false;
        }

        // Buscar el plato mediante su codigo.
        Document filtro = new Document("id_plato", idPlato);

        // Definir los campos que se actualizaran.
        // $set = Modifica los campos especificados sin reemplazar todo el documento.
        Document cambios = new Document("$set",
                new Document("descripcion", nuevaDescripcion)
                        .append("fecha_actualizacion", new java.util.Date()));

        // Ejecutar la actualizacion en MongoDB.
        UpdateResult resultado = coleccion.updateOne(filtro, cambios);

        // Comprobar si se encontro y modifico el documento.
        // getModifiedCount() = Indica cuántos documentos realmente cambiaron.
        return resultado.getModifiedCount() > 0;
    }

    // DELETE: eliminar un plato mediante su codigo.
    public boolean eliminarPlato(String idPlato) {

        // Verificar que el codigo sea valido.
        if (idPlato == null || idPlato.isBlank()) {

            System.out.println("El codigo del plato es obligatorio.");
            return false;
        }

        // Crear el filtro para identificar el plato.
        Document filtro = new Document("id_plato", idPlato);

        // Ejecutar la eliminacion en MongoDB.
        DeleteResult resultado = coleccion.deleteOne(filtro);

        // Comprobar si se elimino un documento.
        return resultado.getDeletedCount() > 0;
    }
}
