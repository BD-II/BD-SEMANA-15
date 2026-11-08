/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.polleriaparedes.mongodb;

import com.mongodb.client.MongoClient;      // Representa la conexion del cliente con MongoDB.
import com.mongodb.client.MongoClients;     // Permite crear el cliente para conectarnos a MongoDB.
import com.mongodb.client.MongoDatabase;    // Permite acceder a una base de datos de MongoDB.

public class ConexionMongoDB {
    
    //URI = Dirección del servidor MongoDB local.
    private static final String URI = "mongodb://127.0.0.1:27017";
    
    //NOMBRE_BD = Nombre de nuestra base de datos de pruebas
    private static final String NOMBRE_BD = "PolleriaParedes_Catalogo_Pruebas";
    
    //MongoClient = Cliente utilizado para comunicarse con el servidor.
    private final MongoClient cliente;
    private final MongoDatabase baseDatos;

    public ConexionMongoDB() {

        // Crear el cliente de MongoDB.
        cliente = MongoClients.create(URI);

        // Obtener la base de datos.
        baseDatos = cliente.getDatabase(NOMBRE_BD);
    }
    
    //getBaseDatos() = Devuelve la referencia a la base de datos.
    public MongoDatabase getBaseDatos() {
        return baseDatos;
    }
    
    //cerrarConexion() = Libera los recursos del cliente.
    public void cerrarConexion() {
        cliente.close();
    }
}
