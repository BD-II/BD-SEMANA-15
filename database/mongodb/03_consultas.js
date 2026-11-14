
/*
 * Proyecto: Polleria Paredes
 * Archivo: 03_consultas.js
 *
 * Objetivo:
 * Demostrar consultas y agregaciones
 * sobre el catalogo multimedia.
 *
 * Requisito:
 * Ejecutar primero los scripts 01 y 02.
 */

const bd = db.getSiblingDB("PolleriaParedes_Catalogo");
const coleccion = bd.getCollection("Catalogo_Platos");

// CONSULTA 1: Mostrar todos los platos
print("\n=== CONSULTA 1: CATALOGO ===");

printjson(
  coleccion.find(
    {},
    {
      _id: 0,
      id_plato: 1,
      nombre: 1,
      categoria: 1
    }
  ).sort({ id_plato: 1 }).toArray()
);

// CONSULTA 2: Filtrar platos de fondo
print("\n=== CONSULTA 2: PLATOS DE FONDO ===");

printjson(
  coleccion.find(
    { categoria: "Fondo" },
    {
      _id: 0,
      id_plato: 1,
      nombre: 1
    }
  ).toArray()
);

// CONSULTA 3: Buscar etiqueta tradicional
print("\n=== CONSULTA 3: ETIQUETA TRADICIONAL ===");

printjson(
  coleccion.find(
    { etiquetas: "tradicional" },
    {
      _id: 0,
      id_plato: 1,
      nombre: 1,
      etiquetas: 1
    }
  ).toArray()
);

// CONSULTA 4: Aplicar varias condiciones
print("\n=== CONSULTA 4: FONDO Y POLLO ===");

printjson(
  coleccion.find(
    {
      categoria: "Fondo",
      etiquetas: "pollo"
    },
    {
      _id: 0,
      nombre: 1,
      categoria: 1,
      etiquetas: 1
    }
  ).toArray()
);

// AGREGACION 1: Cantidad de platos por categoria
print("\n=== AGREGACION 1: PLATOS POR CATEGORIA ===");

printjson(
  coleccion.aggregate([
    {
      $group: {
        _id: "$categoria",
        total_platos: { $sum: 1 }
      }
    },
    {
      $sort: {
        total_platos: -1
      }
    }
  ]).toArray()
);

// AGREGACION 2: Cantidad de imagenes por plato
print("\n=== AGREGACION 2: IMAGENES POR PLATO ===");

printjson(
  coleccion.aggregate([
    {
      $project: {
        _id: 0,
        nombre: 1,
        cantidad_imagenes: {
          $size: "$imagenes"
        }
      }
    },
    {
      $sort: {
        nombre: 1
      }
    }
  ]).toArray()
);

// AGREGACION 3: Frecuencia de etiquetas
print("\n=== AGREGACION 3: ETIQUETAS MAS UTILIZADAS ===");

printjson(
  coleccion.aggregate([
    {
      $unwind: "$etiquetas"
    },
    {
      $group: {
        _id: "$etiquetas",
        cantidad: { $sum: 1 }
      }
    },
    {
      $sort: {
        cantidad: -1,
        _id: 1
      }
    }
  ]).toArray()
);

print("\nConsultas ejecutadas correctamente.");
