package com.polleriaparedes.api;

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
import com.polleriaparedes.dao.mongo.PlatoDAO;
import com.polleriaparedes.config.ConexionMongoDB;

import com.polleriaparedes.dao.postgres.ClientePostgresDAO; // Consulta clientes en PostgreSQL.
import java.net.URLDecoder; // Convierte el texto codificado en la URL a texto normal.

// Servicio que combina los datos oficiales de PostgreSQL
// con la informacion complementaria de MongoDB.
import com.polleriaparedes.servicio.CatalogoServicio;

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

// DAO del proyecto que registra el pedido y sus detalles en PostgreSQL.
import com.polleriaparedes.dao.postgres.PedidoPostgresDAO;

// Excepción que JDBC comunica cuando falla una operación con PostgreSQL.
import java.sql.SQLException;

// Implementación de Map que conserva el orden en que agregamos los platos.
import java.util.LinkedHashMap;

// Representa la lista de líneas que llega en el JSON.
import java.util.List;

// Representa pares clave-valor: código del plato y cantidad.
import java.util.Map;

// Permite capturar errores del lector de JSON que usamos con Document.parse().
import org.bson.BSONException;

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

            // Crear el servicio que integra PostgreSQL y MongoDB.
            // Reutiliza la misma base MongoDB ya seleccionada.
            CatalogoServicio catalogoServicio
                    = new CatalogoServicio(bd);

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

            // ======================================================
            // ENDPOINT: CATALOGO INTEGRADO POSTGRESQL + MONGODB
            // ======================================================
            // Ruta de prueba independiente del endpoint original.
            servidor.createContext("/api/platos-integrados", intercambio -> {

                // Permitir solicitudes desde nuestro frontend local.
                intercambio.getResponseHeaders().set(
                        "Access-Control-Allow-Origin",
                        "http://127.0.0.1:5501"
                );

                // Indicar que la respuesta sera JSON con codificacion UTF-8.
                intercambio.getResponseHeaders().set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                // Comprobar que el cliente utilice el metodo GET.
                if (!intercambio.getRequestMethod().equals("GET")) {

                    // Indicar que solamente se acepta GET.
                    intercambio.getResponseHeaders().set(
                            "Allow", "GET"
                    );

                    // Responder con el codigo HTTP 405.
                    responder(
                            intercambio,
                            405,
                            "{\"error\":\"Metodo no permitido\"}"
                    );

                    return;
                }

                try {

                    // Consultar PostgreSQL y MongoDB mediante el servicio.
                    java.util.List<Document> catalogo
                            = catalogoServicio.listarCatalogoIntegrado();

                    // Preparar el arreglo JSON de respuesta.
                    StringBuilder json = new StringBuilder("[");

                    // Controlar la separacion de documentos con comas.
                    boolean primero = true;

                    // Recorrer los platos integrados.
                    for (Document plato : catalogo) {

                        if (!primero) {
                            json.append(",");
                        }

                        // Adaptar cada plato al formato que utiliza
                        // actualmente nuestro frontend.
                        json.append(adaptarPlatoIntegrado(plato).toJson());

                        primero = false;
                    }

                    // Cerrar el arreglo JSON.
                    json.append("]");

                    // Enviar los platos integrados al cliente.
                    responder(intercambio, 200, json.toString());

                } catch (Exception e) {

                    // Registrar el error en la consola de NetBeans.
                    System.err.println(
                            "Error al integrar el catalogo: "
                            + e.getMessage()
                    );

                    e.printStackTrace();

                    // Enviar una respuesta de error al cliente.
                    responder(
                            intercambio,
                            500,
                            "{\"error\":\"Error interno del servidor\"}"
                    );
                }
            });

            // Crear el objeto que sabe registrar pedidos en PostgreSQL.
            PedidoPostgresDAO pedidoDAO = new PedidoPostgresDAO();

            // Asociar /api/pedidos con el método que atenderá la solicitud.
            // "intercambio" contiene la solicitud recibida y permite enviar la respuesta.
            servidor.createContext("/api/pedidos",
                    intercambio -> atenderPedidos(intercambio, pedidoDAO));

            // Crear el DAO que consultará los clientes de PostgreSQL.
            ClientePostgresDAO clienteDAO = new ClientePostgresDAO();

// Asociar /api/clientes con el método que atenderá la búsqueda.
            servidor.createContext("/api/clientes",
                    intercambio -> buscarClientes(intercambio, clienteDAO));

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

    // ======================================================
    // ADAPTADOR DEL CATALOGO INTEGRADO
    // ======================================================
    /**
     * Convierte un plato integrado al formato del frontend.
     *
     * El precio, nombre, categoria e ID proceden de PostgreSQL. La descripcion
     * y otros datos proceden de MongoDB.
     */
    private static Document adaptarPlatoIntegrado(Document plato) {

        // Recuperar los datos oficiales de PostgreSQL.
        String codigo = plato.getString("id");
        String nombre = plato.getString("nombre");
        String categoria = plato.getString("categoria");

        // Recuperar el precio como BigDecimal.
        java.math.BigDecimal precio
                = plato.get("precio", java.math.BigDecimal.class);

        // Convertir la categoria al nombre utilizado en JavaScript.
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

        // Seleccionar el emoji correspondiente a la categoria.
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

        // Construir el documento que recibira el frontend.
        return new Document("id", codigo)
                .append("nombre", nombre)
                .append("categoria", categoriaWeb)
                // Precio oficial recuperado desde PostgreSQL.
                // doubleValue() produce un numero JSON compatible
                // con el frontend para estos importes del catalogo.
                // Los calculos monetarios seguiran usando BigDecimal.
                .append("precio", precio.doubleValue())
                .append("emoji", emoji)
                // Conservar la convencion de imagenes del frontend.
                .append("imagen", codigo + ".jpg")
                // Conservar los campos utilizados por JavaScript.
                .append("etiqueta", "")
                // Recuperar la descripcion complementaria.
                .append(
                        "descripcion",
                        plato.getString("descripcion") == null
                        ? "" : plato.getString("descripcion")
                )
                // El documento MongoDB actual no contiene
                // un campo ingredientes en su estructura principal.
                .append(
                        "ingredientes",
                        java.util.Collections.emptyList()
                );
    }

    private static void atenderPedidos(
            HttpExchange intercambio, PedidoPostgresDAO pedidoDAO
    ) throws IOException {

        // Permitir que el navegador envíe solicitudes desde Live Server.
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "http://127.0.0.1:5501");
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type");
        intercambio.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        // Reconocer la consulta de detalle, por ejemplo /api/pedidos/PD057.
        String ruta = intercambio.getRequestURI().getPath();
        String prefijoDetalle = "/api/pedidos/";

        if (ruta.startsWith(prefijoDetalle)) {
            String idPedido = ruta.substring(prefijoDetalle.length());

            // La ruta de pago incluye un segundo segmento después del código.
            if (idPedido.endsWith("/pago")) {
                String codigoPedido = idPedido.substring(0, idPedido.length() - "/pago".length());

                if (!codigoPedido.isBlank() && !codigoPedido.contains("/")) {
                    atenderPago(intercambio, pedidoDAO, codigoPedido);
                    return;
                }
            }

            // El código debe ocupar un solo segmento de la ruta.
            if (idPedido.isBlank() || idPedido.contains("/")) {
                responder(intercambio, 404,
                        new Document("error", "Ruta no encontrada.").toJson());
                return;
            }

            // El navegador consulta los permisos antes de enviar PATCH desde Live Server.
            if ("OPTIONS".equals(intercambio.getRequestMethod())) {
                intercambio.sendResponseHeaders(204, -1);
                intercambio.close();
                return;
            }

            // PATCH actualiza el estado del pedido identificado en la URL.
            // PATCH solicita un cambio de estado para el pedido indicado en la URL.
            if ("PATCH".equals(intercambio.getRequestMethod())) {
                actualizarEstadoPedido(intercambio, pedidoDAO, idPedido);
                return;
            }

            if (!"GET".equals(intercambio.getRequestMethod())) {
                intercambio.getResponseHeaders().set("Allow", "GET, PATCH, OPTIONS");
                responder(intercambio, 405,
                        new Document("error", "Método no permitido.").toJson());
                return;
            }

            consultarDetallePedido(intercambio, pedidoDAO, idPedido);
            return;
        }

        // HttpServer también puede enviar aquí rutas con el mismo prefijo.
        // Comprobar que se solicitó exactamente /api/pedidos.
        if (!"/api/pedidos".equals(intercambio.getRequestURI().getPath())) {
            responder(intercambio, 404,
                    new Document("error", "Ruta no encontrada.").toJson());
            return;
        }

        // OPTIONS es la consulta previa que puede hacer el navegador
        // antes de enviar un POST con JSON desde Live Server.
        if ("OPTIONS".equals(intercambio.getRequestMethod())) {
            intercambio.sendResponseHeaders(204, -1);
            intercambio.close();
            return;
        }

        // Consultar las cabeceras cuando se solicita exactamente /api/pedidos.
        if ("GET".equals(intercambio.getRequestMethod())) {
            listarPedidos(intercambio, pedidoDAO);
            return;
        }

        // El registro de pedidos se realiza mediante POST.
        if (!"POST".equals(intercambio.getRequestMethod())) {
            intercambio.getResponseHeaders().set("Allow", "GET, POST, OPTIONS");
            responder(intercambio, 405,
                    new Document("error", "Método no permitido.").toJson());
            return;
        }

        String idPedido;
        try {
            // Leer el cuerpo como texto UTF-8 y cerrar el flujo al terminar.
            String cuerpo;
            try (var entrada = intercambio.getRequestBody()) {
                cuerpo = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Convertir el texto JSON en un objeto del que podemos leer campos.
            Document datos = Document.parse(cuerpo);

            // Comprobar los tipos y guardar los valores en variables Java.
            // List<?> representa una lista cuyos elementos validaremos uno a uno.
            if (!(datos.get("idCliente") instanceof String idCliente)
                    || !(datos.get("idEmpleado") instanceof String idEmpleado)
                    || !(datos.get("tipoPedido") instanceof String tipoPedido)
                    || !(datos.get("lineas") instanceof List<?> lineas)) {
                throw new IllegalArgumentException(
                        "Envía idCliente, idEmpleado, tipoPedido y una lista de líneas.");
            }

            // Usar los tipos de pedido que ya aparecen en los datos del proyecto.
            if (!List.of("Presencial", "Recojo", "Delivery").contains(tipoPedido)) {
                throw new IllegalArgumentException("Tipo de pedido inválido.");
            }

            // Convertir las líneas del JSON al mapa que espera el DAO.
            Map<String, Integer> platos = new LinkedHashMap<>();
            for (Object elemento : lineas) {
                if (!(elemento instanceof Document linea)
                        || !(linea.get("platoId") instanceof String platoId)
                        || !(linea.get("cantidad") instanceof Integer cantidad)
                        || platoId.isBlank() || cantidad <= 0) {
                    throw new IllegalArgumentException(
                            "Cada línea requiere un platoId y una cantidad entera positiva.");
                }

                // putIfAbsent agrega el plato si su código aún no está en el mapa.
                // Si ya estaba, devuelve la cantidad anterior y detectamos el duplicado.
                if (platos.putIfAbsent(platoId, cantidad) != null) {
                    throw new IllegalArgumentException("Envía una sola línea por plato.");
                }
            }

            // El DAO valida los campos obligatorios y que el mapa tenga platos.
            // Luego guarda la cabecera y los detalles en una sola transacción.
            idPedido = pedidoDAO.registrarPedido(
                    idCliente, idEmpleado, tipoPedido, platos);

        } catch (BSONException e) {
            // El cuerpo recibido no se pudo interpretar como un objeto JSON.
            responder(intercambio, 400,
                    new Document("error", "El cuerpo debe ser un objeto JSON válido.").toJson());
            return;

        } catch (IllegalArgumentException e) {
            // El JSON contiene datos que no cumplen las validaciones.
            responder(intercambio, 400,
                    new Document("error", e.getMessage()).toJson());
            return;

        } catch (SQLException e) {
            // El DAO gestiona el rollback si falla la transacción.
            // El detalle técnico queda en la consola de NetBeans.
            System.err.println("Error al registrar el pedido: " + e.getMessage());
            responder(intercambio, 500,
                    new Document("error", "No se pudo registrar el pedido.").toJson());
            return;
        }

        // El DAO devuelve el código después del commit.
        // 201 indica que se creó el pedido y toJson() prepara la respuesta.
        responder(intercambio, 201, new Document("id", idPedido).toJson());
    }

    private static void listarPedidos(
            HttpExchange intercambio, PedidoPostgresDAO pedidoDAO
    ) throws IOException {
        List<Map<String, Object>> pedidos;

        try {
            // El DAO ya devuelve las cabeceras de la más reciente a la más antigua.
            pedidos = pedidoDAO.listarPedidos();
        } catch (SQLException e) {
            System.err.println("Error al listar pedidos: " + e.getMessage());
            responder(intercambio, 500,
                    new Document("error", "No se pudieron consultar los pedidos.").toJson());
            return;
        }

        // Adaptar los nombres de PostgreSQL al formato que leerá la página.
        List<Document> lista = pedidos.stream()
                .map(pedido -> new Document("id", pedido.get("id_pedido"))
                .append("idCliente", pedido.get("id_cliente"))
                .append("idEmpleado", pedido.get("id_empleado"))
                .append("tipo", pedido.get("tipo_pedido"))
                .append("estado", pedido.get("estado"))
                .append("legado", pedido.get("legado"))
                .append("fecha", ((java.sql.Timestamp) pedido.get("fecha"))
                        .toLocalDateTime().toString()))
                .toList();

        // Serializar cada cabecera y reunirlas en un arreglo JSON.
        String json = lista.stream()
                .map(Document::toJson)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        responder(intercambio, 200, json);
    }

    private static void consultarDetallePedido(
            HttpExchange intercambio, PedidoPostgresDAO pedidoDAO, String idPedido
    ) throws IOException {
        List<Map<String, Object>> detalles;

        try {
            // Cada mapa representa una fila del detalle consultado en PostgreSQL.
            detalles = pedidoDAO.listarDetalles(idPedido);
        } catch (SQLException e) {
            System.err.println("Error al consultar el detalle: " + e.getMessage());
            responder(intercambio, 500,
                    new Document("error", "No se pudo consultar el detalle.").toJson());
            return;
        }

        // Convertir las filas a JSON. Los importes viajan como texto decimal,
        // por ejemplo "13.50", para conservar el valor devuelto por PostgreSQL.
        List<Document> lineas = detalles.stream()
                .map(detalle -> new Document("idDetalle", detalle.get("id_detalle"))
                .append("platoId", detalle.get("id_plato"))
                .append("nombre", detalle.get("nombre_plato"))
                .append("cantidad", detalle.get("cantidad"))
                .append("precioUnitario", detalle.get("precio_unitario").toString())
                .append("subtotal", detalle.get("subtotal").toString()))
                .toList();

        responder(intercambio, 200,
                new Document("id", idPedido).append("lineas", lineas).toJson());
    }

    private static void actualizarEstadoPedido(
            HttpExchange intercambio, PedidoPostgresDAO pedidoDAO, String idPedido
    ) throws IOException {
        Document datos;

        try (var entrada = intercambio.getRequestBody()) {
            // Leer el JSON enviado por la página, por ejemplo {"estado":3}.
            datos = Document.parse(
                    new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        } catch (BSONException | IllegalArgumentException e) {
            responder(intercambio, 400,
                    new Document("error", "Envía un objeto JSON válido.").toJson());
            return;
        }

        // Extraer un entero y aceptar las dos acciones disponibles por PATCH.
        if (!(datos.get("estado") instanceof Integer estado)
                || (estado != 1 && estado != 3)) {
            responder(intercambio, 400,
                    new Document("error",
                            "Envía el estado 1 para preparar o 3 para cancelar.").toJson());
            return;
        }

        try {
            // El DAO ya llama al procedimiento. PostgreSQL valida si la transición
            // es válida y aplica las reglas de inventario cuando corresponde.
            pedidoDAO.cambiarEstado(idPedido, estado);
        } catch (SQLException e) {
            System.err.println("Error al cambiar el estado: " + e.getMessage());

            if ("P0001".equals(e.getSQLState())) {
                responder(intercambio, 409,
                        new Document("error",
                                "PostgreSQL rechazó el cambio. Revisa el estado actual del pedido.")
                                .toJson());
            } else {
                responder(intercambio, 500,
                        new Document("error", "No se pudo actualizar el pedido.").toJson());
            }
            return;
        }

        // Devolver el estado solicitado después de que PostgreSQL confirme el cambio.
        responder(intercambio, 200,
                new Document("id", idPedido).append("estado", estado).toJson());
    }

    private static void atenderPago(
            HttpExchange intercambio, PedidoPostgresDAO pedidoDAO, String idPedido
    ) throws IOException {
        String metodo = intercambio.getRequestMethod();

        // Responder a la consulta previa del navegador.
        if ("OPTIONS".equals(metodo)) {
            intercambio.sendResponseHeaders(204, -1);
            intercambio.close();
            return;
        }

        if (!"POST".equals(metodo)) {
            intercambio.getResponseHeaders().set("Allow", "POST, OPTIONS");
            responder(intercambio, 405,
                    new Document("error", "Método no permitido.").toJson());
            return;
        }

        Document datos;
        try (var entrada = intercambio.getRequestBody()) {
            datos = Document.parse(
                    new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        } catch (BSONException | IllegalArgumentException e) {
            responder(intercambio, 400,
                    new Document("error", "Envía un objeto JSON válido.").toJson());
            return;
        }

        // Aceptar los métodos que ofrecerá el selector de la página.
        if (!(datos.get("metodoPago") instanceof String metodoPago)
                || !List.of("Efectivo", "Tarjeta", "Yape", "Plin").contains(metodoPago)) {
            responder(intercambio, 400,
                    new Document("error", "Selecciona un método de pago válido.").toJson());
            return;
        }

        try {
            pedidoDAO.registrarPago(idPedido, metodoPago);
        } catch (SQLException e) {
            System.err.println("Error al registrar el pago: " + e.getMessage());
            if ("P0001".equals(e.getSQLState())) {
                responder(intercambio, 409,
                        new Document("error",
                                "PostgreSQL rechazó el pago. Revisa el estado del pedido.")
                                .toJson());
            } else {
                responder(intercambio, 500,
                        new Document("error", "No se pudo registrar el pago.").toJson());
            }
            return;
        }

        // La factura se creó y el trigger dejó el pedido en estado 2.
        responder(intercambio, 201,
                new Document("id", idPedido).append("estado", 2).toJson());
    }

    private static void buscarClientes(
            HttpExchange intercambio, ClientePostgresDAO clienteDAO
    ) throws IOException {
        // Permitir que Nuevo pedido consulte desde Live Server.
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "http://127.0.0.1:5501");
        intercambio.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        // HttpServer atiende por prefijo. Comprobar la ruta exacta.
        if (!"/api/clientes".equals(intercambio.getRequestURI().getPath())) {
            responder(intercambio, 404,
                    new Document("error", "Ruta no encontrada.").toJson());
            return;
        }

        // Permitir el envío de JSON desde Live Server.
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type");

        String metodo = intercambio.getRequestMethod();

        if ("OPTIONS".equals(metodo)) {
            intercambio.sendResponseHeaders(204, -1);
            intercambio.close();
            return;
        }

        if ("POST".equals(metodo)) {
            registrarCliente(intercambio, clienteDAO);
            return;
        }

        if (!"GET".equals(intercambio.getRequestMethod())) {
            intercambio.getResponseHeaders().set("Allow", "GET");
            responder(intercambio, 405,
                    new Document("error", "Método no permitido.").toJson());
            return;
        }

        // Leer lo que viene después de ?, por ejemplo buscar=Mar%C3%ADa.
        String consulta = intercambio.getRequestURI().getRawQuery();
        String texto = "";

        try {
            if (consulta != null) {
                for (String parametro : consulta.split("&")) {
                    if (parametro.startsWith("buscar=")) {
                        texto = URLDecoder.decode(
                                parametro.substring("buscar=".length()),
                                StandardCharsets.UTF_8);
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            responder(intercambio, 400,
                    new Document("error", "El texto de búsqueda no es válido.").toJson());
            return;
        }

        try {
            // El DAO consulta PostgreSQL y devuelve hasta 20 coincidencias.
            List<ClientePostgresDAO.ClienteEncontrado> clientes
                    = clienteDAO.buscarPorNombre(texto);

            // Convertir cada cliente en un objeto JSON para el formulario.
            String json = clientes.stream()
                    .map(cliente -> new Document("idCliente", cliente.idCliente())
                    .append("nombre", cliente.nombre())
                    .append("apellido", cliente.apellido())
                    .append("telefonoFinal", cliente.telefonoFinal()))
                    .map(Document::toJson)
                    .collect(java.util.stream.Collectors.joining(",", "[", "]"));

            responder(intercambio, 200, json);
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
            responder(intercambio, 500,
                    new Document("error", "No se pudieron consultar los clientes.").toJson());
        }
    }

    private static void registrarCliente(
            HttpExchange intercambio, ClientePostgresDAO clienteDAO
    ) throws IOException {
        Document datos;

        try (var entrada = intercambio.getRequestBody()) {
            datos = Document.parse(
                    new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        } catch (BSONException | IllegalArgumentException e) {
            responder(intercambio, 400,
                    new Document("error", "Envía un objeto JSON válido.").toJson());
            return;
        }

        String nombre = leerCampoCliente(datos, "nombre");
        String apellido = leerCampoCliente(datos, "apellido");
        String telefono = leerCampoCliente(datos, "telefono");
        String direccion = leerCampoCliente(datos, "direccion");

        if (nombre.isEmpty() || apellido.isEmpty()
                || telefono.isEmpty() || direccion.isEmpty()) {
            responder(intercambio, 400,
                    new Document("error",
                            "Completa nombre, apellido, teléfono y dirección.").toJson());
            return;
        }

        ClientePostgresDAO.ResultadoAlta resultado;
        try {
            resultado = clienteDAO.registrarCliente(nombre, apellido, telefono, direccion);
        } catch (SQLException e) {
            System.err.println("Error al registrar el cliente: " + e.getMessage());

            int codigo = switch (e.getSQLState() == null ? "" : e.getSQLState()) {
                case "22001" ->
                    400; // Un valor supera la longitud de su columna.
                case "23505" ->
                    409; // PostgreSQL detectó un valor duplicado.
                default ->
                    500;
            };

            String mensaje = switch (codigo) {
                case 400 ->
                    "Uno de los campos supera la longitud permitida.";
                case 409 ->
                    "Hay un dato duplicado. Revisa si el cliente ya está registrado.";
                default ->
                    "No se pudo registrar el cliente.";
            };

            responder(intercambio, codigo, new Document("error", mensaje).toJson());
            return;
        }
        ClientePostgresDAO.ClienteEncontrado cliente = resultado.cliente();

        responder(intercambio, resultado.creado() ? 201 : 200,
                new Document("idCliente", cliente.idCliente())
                        .append("nombre", cliente.nombre())
                        .append("apellido", cliente.apellido())
                        .append("telefonoFinal", cliente.telefonoFinal())
                        .append("creado", resultado.creado())
                        .toJson());
    }

    private static String leerCampoCliente(Document datos, String campo) {
        // Quitar espacios exteriores y tratar campos ausentes como vacíos.
        return datos.get(campo) instanceof String texto ? texto.strip() : "";
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
