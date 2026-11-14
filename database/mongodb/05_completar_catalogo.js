
/*
 * POLLERIA PAREDES
 * Script 05: Completar catalogo MongoDB
 *
 * Objetivo:
 * Insertar los 32 platos pendientes del catalogo.
 *
 * Base de datos:
 * PolleriaParedes_Catalogo_Pruebas
 *
 * Los datos de identificacion provienen de PostgreSQL.
 * Las descripciones y rutas de imagen son provisionales.
 */

// Seleccionamos nuestra base de datos de pruebas.
const baseDatos = db.getSiblingDB("PolleriaParedes_Catalogo_Pruebas");

// Seleccionamos la coleccion donde guardamos los platos.
const coleccion = baseDatos.getCollection("Catalogo_Platos");

// Lista de los 32 platos que faltan registrar.
const platosPendientes = [
    // ENTRADAS
    { id: "P003", nombre: "Ocopa Arequipeña", categoria: "Entrada" },
    { id: "P004", nombre: "Tequeños de Queso", categoria: "Entrada" },
    { id: "P005", nombre: "Ensalada Cesar", categoria: "Entrada" },
    { id: "P006", nombre: "Anticuchos", categoria: "Entrada" },
    { id: "P007", nombre: "Tequeños de Pollo", categoria: "Entrada" },
    { id: "P008", nombre: "Tamal Criollo", categoria: "Entrada" },
    { id: "P009", nombre: "Wantan Frito", categoria: "Entrada" },
    { id: "P010", nombre: "Sopa Criolla", categoria: "Entrada" },

    // FONDOS
    { id: "P011", nombre: "Lomo Saltado", categoria: "Fondo" },
    { id: "P012", nombre: "Arroz Chaufa", categoria: "Fondo" },
    { id: "P013", nombre: "Arroz con Pollo Especial", categoria: "Fondo" },
    { id: "P014", nombre: "Aji de Gallina", categoria: "Fondo" },
    { id: "P015", nombre: "Pollo Saltado", categoria: "Fondo" },
    { id: "P016", nombre: "Tallarines Verdes", categoria: "Fondo" },
    { id: "P019", nombre: "Alitas Broaster", categoria: "Fondo" },
    { id: "P020", nombre: "Tacu Tacu con Lomo", categoria: "Fondo" },
    { id: "P021", nombre: "Carapulcra", categoria: "Fondo" },
    { id: "P022", nombre: "Arroz con Pollo", categoria: "Fondo" },
    { id: "P023", nombre: "Milanesa con Papas", categoria: "Fondo" },
    { id: "P024", nombre: "Pollo al Horno", categoria: "Fondo" },
    { id: "P025", nombre: "Pollo Guisado", categoria: "Fondo" },
    { id: "P027", nombre: "Pollo a la Plancha", categoria: "Fondo" },
    { id: "P028", nombre: "Pollo Frito con Arroz", categoria: "Fondo" },

    // POSTRES
    { id: "P029", nombre: "Suspiro a la Limeña", categoria: "Postre" },
    { id: "P030", nombre: "Arroz con Leche", categoria: "Postre" },
    { id: "P031", nombre: "Mazamorra Morada", categoria: "Postre" },
    { id: "P034", nombre: "Cheesecake de Fresa", categoria: "Postre" },
    { id: "P035", nombre: "Helado Artesanal", categoria: "Postre" },

    // BEBIDAS
    { id: "P036", nombre: "Inca Kola", categoria: "Bebida" },
    { id: "P038", nombre: "Limonada", categoria: "Bebida" },
    { id: "P039", nombre: "Maracuya Frozen", categoria: "Bebida" },
    { id: "P040", nombre: "Cafe Americano", categoria: "Bebida" }
];

// Contadores para verificar el resultado.
let insertados = 0;
let existentes = 0;
let errores = 0;

// Recorremos cada plato pendiente.
platosPendientes.forEach(plato => {

    try {
        // Comprobamos si el codigo ya esta registrado.
        const encontrado = coleccion.findOne({
            id_plato: plato.id
        });

        if (encontrado !== null) {
            print("Ya existe: " + plato.id);
            existentes++;
            return;
        }

        // Creamos una ruta de imagen provisional.
        const rutaImagen =
            "imagenes/platos/" + plato.id + "/principal.jpg";

        // Construimos el documento completo.
        const nuevoPlato = {
            id_plato: plato.id,
            nombre: plato.nombre,
            categoria: plato.categoria,

            descripcion:
                "Descripcion provisional del plato " +
                plato.nombre + ".",

            imagenes: [
                {
                    ruta: rutaImagen,
                    tipo: "principal",
                    texto_alternativo: plato.nombre
                }
            ],

            // Se completaran cuando se definan las presentaciones.
            presentaciones: [],

            etiquetas: [
                plato.categoria.toLowerCase()
            ],

            estado: "borrador",

            fecha_creacion: new Date(),
            fecha_actualizacion: new Date()
        };

        // Insertamos el documento en MongoDB.
        coleccion.insertOne(nuevoPlato);

        insertados++;

        print("Insertado: " + plato.id + " - " + plato.nombre);

    } catch (error) {
        errores++;
        print("Error en " + plato.id + ": " + error.message);
    }
});

// Consultamos la cantidad total de documentos.
const total = coleccion.countDocuments({});

// Verificamos cuantos codigos oficiales existen.
const codigosOficiales = coleccion.countDocuments({
    id_plato: {
        $regex: "^P(00[1-9]|0[1-3][0-9]|040)$"
    }
});

// Mostramos los resultados.
print("\n=== RESUMEN ===");
print("Insertados: " + insertados);
print("Ya existentes: " + existentes);
print("Errores: " + errores);
print("Total en Catalogo_Platos: " + total);
print("Codigos oficiales P001-P040 presentes: " + codigosOficiales);
