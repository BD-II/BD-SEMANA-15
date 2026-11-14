
/*
 * PROYECTO: POLLERIA PAREDES
 * Archivo: 06_actualizar_precios.js
 *
 * Objetivo:
 * Actualizar los precios de los 40 productos
 * oficiales del catalogo MongoDB.
 *
 * Los precios fueron contrastados con PostgreSQL.
 *
 * Este script no inserta ni elimina productos.
 * Solo actualiza el precio de los existentes.
 *
 * Base de datos por defecto:
 * PolleriaParedes_Catalogo_Pruebas
 */

// 1. Seleccionamos la base de datos de pruebas.
// Para trabajar con el catalogo principal,
// cambiar el nombre por "PolleriaParedes_Catalogo".
const baseDatos = db.getSiblingDB(
    "PolleriaParedes_Catalogo_Pruebas"
);

// 2. Seleccionamos la coleccion.
const coleccion = baseDatos.getCollection("Catalogo_Platos");

// 3. Precios oficiales de los 40 productos.
const precios = [
    // ENTRADAS
    { id: "P001", precio: 12.00 },
    { id: "P002", precio: 14.00 },
    { id: "P003", precio: 13.50 },
    { id: "P004", precio: 11.00 },
    { id: "P005", precio: 15.00 },
    { id: "P006", precio: 16.00 },
    { id: "P007", precio: 13.00 },
    { id: "P008", precio: 10.00 },
    { id: "P009", precio: 9.00 },
    { id: "P010", precio: 14.50 },

    // PLATOS DE FONDO
    { id: "P011", precio: 25.00 },
    { id: "P012", precio: 20.00 },
    { id: "P013", precio: 27.00 },
    { id: "P014", precio: 24.00 },
    { id: "P015", precio: 25.00 },
    { id: "P016", precio: 22.00 },
    { id: "P017", precio: 26.00 },
    { id: "P018", precio: 25.00 },
    { id: "P019", precio: 23.00 },
    { id: "P020", precio: 31.00 },
    { id: "P021", precio: 23.00 },
    { id: "P022", precio: 24.50 },
    { id: "P023", precio: 27.00 },
    { id: "P024", precio: 26.00 },
    { id: "P025", precio: 24.00 },
    { id: "P026", precio: 18.00 },
    { id: "P027", precio: 25.00 },
    { id: "P028", precio: 24.00 },

    // POSTRES
    { id: "P029", precio: 9.50 },
    { id: "P030", precio: 7.00 },
    { id: "P031", precio: 7.00 },
    { id: "P032", precio: 10.00 },
    { id: "P033", precio: 11.00 },
    { id: "P034", precio: 12.00 },
    { id: "P035", precio: 8.50 },

    // BEBIDAS
    { id: "P036", precio: 5.00 },
    { id: "P037", precio: 4.50 },
    { id: "P038", precio: 6.00 },
    { id: "P039", precio: 8.00 },
    { id: "P040", precio: 5.50 }
];

// 4. Verificamos que existan los 40 productos.
const ids = precios.map(plato => plato.id);

const existentes = coleccion.find(
    { id_plato: { $in: ids } },
    { _id: 0, id_plato: 1 }
).toArray();

const idsExistentes = new Set(
    existentes.map(plato => plato.id_plato)
);

const faltantes = ids.filter(id => !idsExistentes.has(id));

if (faltantes.length > 0) {
    print("No se actualizo el catalogo.");
    print("Productos faltantes: " + faltantes.join(", "));
} else {

    // 5. Preparamos las operaciones de actualizacion.
    const operaciones = precios.map(plato => ({
        updateOne: {
            filter: { id_plato: plato.id },
            update: {
                $set: { precio: plato.precio }
            },
            upsert: false
        }
    }));

    // 6. Ejecutamos todas las actualizaciones.
    const resultado = coleccion.bulkWrite(operaciones);

    // 7. Mostramos el resumen.
    print("\n=== ACTUALIZACION DE PRECIOS ===");
    print("Base de datos: " + baseDatos.getName());
    print("Productos esperados: " + precios.length);
    print("Documentos encontrados: " + resultado.matchedCount);
    print("Documentos modificados: " + resultado.modifiedCount);
    print("Proceso finalizado.");
}
