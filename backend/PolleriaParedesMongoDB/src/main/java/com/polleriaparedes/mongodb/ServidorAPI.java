package com.polleriaparedes.mongodb;

/*
Concepto                                        Significado
HttpServer              Servidor que recibe solicitudes HTTP desde un cliente, como un navegador.
HttpExchange            Objeto que contiene la solicitud recibida y permite construir su respuesta.
InetSocketAddress	Establece la dirección IP y el puerto donde escuchará el servidor.
createContext()         Asocia una ruta HTTP con el código encargado de atenderla.
intercambio -> {}	Expresión lambda que define qué ejecutar cuando llega una solicitud.
StringBuilder           Construye cadenas de texto de manera eficiente.
toJson()                Convierte un documento BSON de MongoDB a su representación JSON.
OutputStream            Envía los bytes de la respuesta al cliente.
throws IOException	Declara que un método puede producir un error de entrada o salida.
addShutdownHook()	Registra un hilo para liberar recursos cuando se apaga normalmente la JVM.
 */
// Representa la base de datos MongoDB seleccionada.
import com.mongodb.client.MongoDatabase;

// Permite crear un servidor HTTP desde Java.
import com.sun.net.httpserver.HttpServer;

// Representa una solicitud HTTP y su respuesta.
import com.sun.net.httpserver.HttpExchange;

// Permite representar documentos BSON de MongoDB.
import org.bson.Document;

// Representa errores de entrada y salida.
import java.io.IOException;

// Permite enviar datos al navegador mediante un flujo de salida.
import java.io.OutputStream;

// Define la direccion IP y el puerto del servidor.
import java.net.InetSocketAddress;

// Permite codificar textos utilizando UTF-8.
import java.nio.charset.StandardCharsets;

public class ServidorAPI {

    // Metodo principal: inicia la aplicacion HTTP.
    public static void main(String[] args) {

        // Referencia para administrar la conexion con MongoDB.
        ConexionMongoDB conexion = null;

        try {

            // Establecer conexion con la base de datos principal.
            conexion = new ConexionMongoDB(
                    "PolleriaParedes_Catalogo"
            );

            // Obtener la base de datos seleccionada.
            MongoDatabase bd = conexion.getBaseDatos();
         
            // Mostrar la base de datos utilizada por la API.
            System.out.println(
                    "Base de datos utilizada: " + bd.getName()
            );

            // Verificar que MongoDB responda.
            bd.runCommand(new Document("ping", 1));

            // Crear el objeto que administra los platos.
            PlatoDAO platoDAO = new PlatoDAO(bd);

            // Crear el servidor HTTP en la IP local y puerto 8080.
            HttpServer servidor = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 8080),
                    0
            );

            // Definir la ruta que atendera las consultas de platos.
            servidor.createContext("/api/platos", intercambio -> {

                // Permitir solicitudes desde nuestro frontend.
                intercambio.getResponseHeaders().set(
                        "Access-Control-Allow-Origin",
                        "http://127.0.0.1:5501"
                );

                // Indicar que la respuesta sera JSON codificado en UTF-8.
                intercambio.getResponseHeaders().set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                // Verificar que la solicitud utilice el metodo GET.
                if (!intercambio.getRequestMethod().equals("GET")) {

                    // Informar que solamente se permite GET.
                    intercambio.getResponseHeaders().set(
                            "Allow", "GET"
                    );

                    // Responder con el codigo HTTP 405.
                    responder(intercambio, 405,
                            "{\"error\":\"Metodo no permitido\"}");

                    return;
                }

                try {

                    // Crear una estructura para construir el arreglo JSON.
                    StringBuilder json = new StringBuilder("[");

                    // Controlar la separacion de documentos mediante comas.
                    boolean primero = true;

                    // Consultar los platos almacenados en MongoDB.
                    for (Document plato : platoDAO.listarPlatos()) {

                        // Agregar una coma entre documentos.
                        if (!primero) {
                            json.append(",");
                        }

                        // Adaptar el documento MongoDB al formato del frontend.
                        json.append(adaptarPlato(plato).toJson());

                        primero = false;
                    }

                    // Cerrar el arreglo JSON.
                    json.append("]");

                    // Enviar los documentos al navegador.
                    responder(intercambio, 200, json.toString());

                } catch (Exception e) {

                    // Mostrar el error en la consola de NetBeans.
                    System.err.println(
                            "Error al consultar platos: "
                            + e.getMessage()
                    );

                    // Enviar una respuesta de error HTTP 500.
                    responder(intercambio, 500,
                            "{\"error\":\"Error interno del servidor\"}");
                }
            });

            // Iniciar el servidor para recibir solicitudes HTTP.
            servidor.start();

            System.out.println("=== API POLLERIA PAREDES ===");
            System.out.println("Conexion con MongoDB establecida.");
            System.out.println("Servidor HTTP iniciado.");
            System.out.println(
                    "URL: http://127.0.0.1:8080/api/platos"
            );

            // Conservar la referencia para cerrar la conexion.
            ConexionMongoDB conexionFinal = conexion;

            // Liberar recursos cuando finalice la aplicacion.
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {

                        // Detener el servidor HTTP.
                        servidor.stop(0);

                        // Cerrar la conexion con MongoDB.
                        conexionFinal.cerrarConexion();
                    })
            );

        } catch (Exception e) {

            // Mostrar errores durante el inicio del servidor.
            System.err.println(
                    "Error al iniciar la API: " + e.getMessage()
            );

            // Cerrar la conexion si se produjo un error.
            if (conexion != null) {
                conexion.cerrarConexion();
            }
        }
    }

    // Convertir un documento MongoDB al formato utilizado
    // por el frontend HTML, CSS y JavaScript.
    private static Document adaptarPlato(Document plato) {

        // Obtener los datos originales de MongoDB.
        String codigo = plato.getString("id_plato");
        String nombre = plato.getString("nombre");
        String categoria = plato.getString("categoria");

        // Obtener el precio como valor numerico.
        Number precio = plato.get("precio", Number.class);

        // Adaptar las categorias al frontend.
        String categoriaWeb;

        switch (categoria == null ? "" : categoria) {
            case "Entrada":
                categoriaWeb = "Entradas";
                break;
            case "Fondo":
                categoriaWeb = "Platos";
                break;
            case "Postre":
                categoriaWeb = "Postres";
                break;
            case "Bebida":
                categoriaWeb = "Bebidas";
                break;
            default:
                categoriaWeb = categoria;
                break;
        }

        // Seleccionar el emoji segun la categoria.
        String emoji;

        switch (categoria == null ? "" : categoria) {
            case "Entrada":
                emoji = "🥗";
                break;
            case "Fondo":
                emoji = "🍽️";
                break;
            case "Postre":
                emoji = "🍰";
                break;
            case "Bebida":
                emoji = "🥤";
                break;
            default:
                emoji = "🍽️";
                break;
        }

        // Construir el documento compatible con JavaScript.
        return new Document("id", codigo)
                .append("nombre", nombre)
                .append("categoria", categoriaWeb)
                .append("precio", precio)
                .append("emoji", emoji)
                .append("imagen", codigo + ".jpg")
                .append("etiqueta", "")
                .append("descripcion",
                        plato.getString("descripcion") == null
                        ? "" : plato.getString("descripcion"))
                .append("ingredientes",
                        plato.getList(
                                "ingredientes",
                                String.class,
                                java.util.Collections.emptyList()
                        ));
    }

    // Metodo auxiliar para enviar respuestas HTTP.
    private static void responder(
            HttpExchange intercambio,
            int codigo,
            String contenido
    ) throws IOException {

        // Convertir la respuesta a bytes utilizando UTF-8.
        byte[] datos = contenido.getBytes(
                StandardCharsets.UTF_8
        );

        // Enviar el codigo HTTP y el tamaño de la respuesta.
        intercambio.sendResponseHeaders(
                codigo, datos.length
        );

        // Abrir el flujo de salida y cerrarlo automaticamente.
        try (OutputStream salida
                = intercambio.getResponseBody()) {

            // Enviar los datos al navegador.
            salida.write(datos);
        }
    }
}
