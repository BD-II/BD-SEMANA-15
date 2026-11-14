
/*
 * Proyecto: Polleria Paredes
 * Archivo: 02_insertar_platos.js
 *
 * Objetivo:
 * Registrar los primeros ocho platos del
 * catalogo multimedia en MongoDB.
 *
 * Requisito:
 * Ejecutar primero 01_crear_coleccion.js
 */

const bd = db.getSiblingDB("PolleriaParedes_Catalogo");
const coleccion = bd.getCollection("Catalogo_Platos");

const fechaActual = new Date();

const platos = [
  {
    id_plato: "P001",
    nombre: "Papa a la Huancaina",
    categoria: "Entrada",
    descripcion: "Papas acompañadas de salsa huancaína.",
    imagenes: [
      {
        ruta: "imagenes/platos/P001/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Papa a la huancaína"
      }
    ],
    presentaciones: [],
    etiquetas: ["entrada", "papa", "tradicional"],
    estado: "borrador"
  },
  {
    id_plato: "P002",
    nombre: "Causa Limeña",
    categoria: "Entrada",
    descripcion: "Entrada peruana preparada con papa amarilla.",
    imagenes: [
      {
        ruta: "imagenes/platos/P002/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Causa limeña"
      }
    ],
    presentaciones: [],
    etiquetas: ["entrada", "papa", "peruano"],
    estado: "borrador"
  },
  {
    id_plato: "P017",
    nombre: "Pollo a la Brasa",
    categoria: "Fondo",
    descripcion: "Pollo a la brasa acompañado de papas fritas y ensalada fresca, preparado con una cocción tradicional.",
    imagenes: [
      {
        ruta: "imagenes/platos/P017/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Pollo a la brasa"
      }
    ],
    presentaciones: [],
    etiquetas: ["pollo", "brasa", "familiar"],
    estado: "borrador"
  },
  {
    id_plato: "P018",
    nombre: "Pollo Broaster",
    categoria: "Fondo",
    descripcion: "Pollo broaster de textura crujiente.",
    imagenes: [
      {
        ruta: "imagenes/platos/P018/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Pollo broaster"
      }
    ],
    presentaciones: [],
    etiquetas: ["pollo", "broaster", "crujiente"],
    estado: "borrador"
  },
  {
    id_plato: "P026",
    nombre: "Pollo a la Leña",
    categoria: "Fondo",
    descripcion: "Pollo preparado mediante cocción a la leña.",
    imagenes: [
      {
        ruta: "imagenes/platos/P026/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Pollo a la leña"
      }
    ],
    presentaciones: [],
    etiquetas: ["pollo", "leña", "tradicional"],
    estado: "borrador"
  },
  {
    id_plato: "P032",
    nombre: "Picarones",
    categoria: "Postre",
    descripcion: "Postre tradicional peruano acompañado de miel.",
    imagenes: [
      {
        ruta: "imagenes/platos/P032/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Picarones con miel"
      }
    ],
    presentaciones: [],
    etiquetas: ["postre", "dulce", "tradicional"],
    estado: "borrador"
  },
  {
    id_plato: "P033",
    nombre: "Torta de Chocolate",
    categoria: "Postre",
    descripcion: "Porción de torta de chocolate.",
    imagenes: [
      {
        ruta: "imagenes/platos/P033/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Torta de chocolate"
      }
    ],
    presentaciones: [],
    etiquetas: ["postre", "chocolate", "dulce"],
    estado: "borrador"
  },
  {
    id_plato: "P037",
    nombre: "Chicha Morada",
    categoria: "Bebida",
    descripcion: "Bebida tradicional peruana de maíz morado.",
    imagenes: [
      {
        ruta: "imagenes/platos/P037/principal.jpg",
        tipo: "principal",
        texto_alternativo: "Chicha morada"
      }
    ],
    presentaciones: [],
    etiquetas: ["bebida", "tradicional", "refrescante"],
    estado: "borrador"
  }
];

// Buscar identificadores ya registrados
const ids = platos.map(plato => plato.id_plato);

const existentes = coleccion.find(
  { id_plato: { $in: ids } },
  { _id: 0, id_plato: 1 }
).toArray();

const idsExistentes = new Set(
  existentes.map(plato => plato.id_plato)
);

// Filtrar platos que todavía no existen
const nuevos = platos
  .filter(plato => !idsExistentes.has(plato.id_plato))
  .map(plato => ({
    ...plato,
    fecha_creacion: fechaActual,
    fecha_actualizacion: fechaActual
  }));

if (nuevos.length > 0) {
  const resultado = coleccion.insertMany(nuevos);

  print(
    "Platos insertados: " +
    Object.keys(resultado.insertedIds).length
  );
} else {
  print("Los ocho platos ya estan registrados.");
}

print(
  "Total de documentos en la coleccion: " +
  coleccion.countDocuments({})
);
