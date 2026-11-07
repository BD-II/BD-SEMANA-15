
/*
 * Proyecto: Polleria Paredes
 * Archivo: 04_pruebas_crud.js
 *
 * Objetivo:
 * Comprobar las operaciones CRUD utilizando
 * un documento temporal.
 *
 * Requisito:
 * Ejecutar primero los scripts 01 y 02.
 */

const bd = db.getSiblingDB("PolleriaParedes_Catalogo");
const coleccion = bd.getCollection("Catalogo_Platos");

const idTemporal = "P999";

// Verificar que el identificador no exista
if (coleccion.findOne({ id_plato: idTemporal })) {
  throw new Error(
    "P999 ya existe. No se ejecutaran las pruebas."
  );
}

const totalInicial = coleccion.countDocuments({});

try {

  // CREATE: Insertar documento temporal
  print("\n=== CREATE: INSERTAR ===");

  const insercion = coleccion.insertOne({
    id_plato: idTemporal,
    nombre: "Plato Temporal",
    categoria: "Fondo",
    descripcion: "Documento temporal para probar operaciones CRUD.",
    imagenes: [],
    presentaciones: [],
    etiquetas: ["prueba"],
    estado: "borrador",
    fecha_creacion: new Date(),
    fecha_actualizacion: new Date()
  });

  const idMongo = insercion.insertedId;

  print("Documento temporal insertado:");
  printjson(idMongo);

  // READ: Consultar documento temporal
  print("\n=== READ: CONSULTAR ===");

  printjson(
    coleccion.findOne({ _id: idMongo })
  );

  // UPDATE: Modificar descripcion
  print("\n=== UPDATE: MODIFICAR DESCRIPCION ===");

  const actualizacion = coleccion.updateOne(
    { _id: idMongo },
    {
      $set: {
        descripcion: "Descripcion actualizada mediante updateOne.",
        fecha_actualizacion: new Date()
      }
    }
  );

  printjson(actualizacion);

  // UPDATE: Agregar fotografia secundaria
  print("\n=== UPDATE: AGREGAR IMAGEN ===");

  printjson(
    coleccion.updateOne(
      { _id: idMongo },
      {
        $push: {
          imagenes: {
            ruta: "imagenes/pruebas/P999/detalle.jpg",
            tipo: "secundaria",
            texto_alternativo: "Imagen temporal de prueba"
          }
        },
        $set: {
          fecha_actualizacion: new Date()
        }
      }
    )
  );

  // UPDATE: Retirar fotografia
  print("\n=== UPDATE: RETIRAR IMAGEN ===");

  printjson(
    coleccion.updateOne(
      { _id: idMongo },
      {
        $pull: {
          imagenes: {
            ruta: "imagenes/pruebas/P999/detalle.jpg"
          }
        },
        $set: {
          fecha_actualizacion: new Date()
        }
      }
    )
  );

  // READ: Comprobar estado final
  print("\n=== READ: VERIFICACION ===");

  printjson(
    coleccion.findOne({ _id: idMongo })
  );

} finally {

  // DELETE: Limpiar documento temporal
  print("\n=== DELETE: LIMPIEZA ===");

  const temporal = coleccion.findOne({
    id_plato: idTemporal,
    nombre: "Plato Temporal",
    etiquetas: "prueba"
  });

  if (temporal) {
    printjson(
      coleccion.deleteOne({ _id: temporal._id })
    );
  }

  print(
    "Total inicial: " + totalInicial
  );

  print(
    "Total final: " +
    coleccion.countDocuments({})
  );
}

print("\nPruebas CRUD finalizadas.");
