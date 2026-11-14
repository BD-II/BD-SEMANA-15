
/*
 * Proyecto: Polleria Paredes
 * Base de datos: PolleriaParedes_Catalogo
 * Coleccion: Catalogo_Platos
 *
 * Objetivo:
 * Crear la coleccion y configurar sus
 * validaciones e indice unico.
 *
 * Ejecucion: mongosh
 */

const bd = db.getSiblingDB("PolleriaParedes_Catalogo");

const nombreColeccion = "Catalogo_Platos";

// 1. Comprobar si existe la coleccion
const existe = bd.getCollectionNames()
  .includes(nombreColeccion);

if (!existe) {
  bd.createCollection(nombreColeccion);
  print("Coleccion creada correctamente.");
} else {
  print("La coleccion ya existe.");
}

// 2. Configurar el validador
const resultado = bd.runCommand({
  collMod: nombreColeccion,

  validator: {
    $jsonSchema: {
      bsonType: "object",

      required: [
        "id_plato",
        "nombre",
        "categoria",
        "descripcion",
        "imagenes",
        "presentaciones",
        "etiquetas",
        "estado",
        "fecha_creacion",
        "fecha_actualizacion"
      ],

      properties: {
        id_plato: {
          bsonType: "string",
          pattern: "^P[0-9]{3}$"
        },

        nombre: {
          bsonType: "string",
          minLength: 1,
          maxLength: 50
        },

        categoria: {
          enum: [
            "Entrada",
            "Fondo",
            "Postre",
            "Bebida"
          ]
        },

        descripcion: {
          bsonType: "string",
          minLength: 10,
          maxLength: 300
        },

        imagenes: {
          bsonType: "array",
          maxItems: 5,
          items: {
            bsonType: "object",
            required: [
              "ruta",
              "tipo",
              "texto_alternativo"
            ],
            properties: {
              ruta: {
                bsonType: "string",
                minLength: 1
              },
              tipo: {
                enum: [
                  "principal",
                  "secundaria"
                ]
              },
              texto_alternativo: {
                bsonType: "string"
              }
            }
          }
        },

        presentaciones: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: [
              "nombre",
              "porciones"
            ],
            properties: {
              nombre: {
                bsonType: "string"
              },
              porciones: {
                bsonType: "int",
                minimum: 1
              }
            }
          }
        },

        etiquetas: {
          bsonType: "array",
          maxItems: 8,
          items: {
            bsonType: "string"
          }
        },

        estado: {
          enum: [
            "borrador",
            "publicado"
          ]
        },

        fecha_creacion: {
          bsonType: "date"
        },

        fecha_actualizacion: {
          bsonType: "date"
        }
      }
    }
  },

  validationLevel: "strict",
  validationAction: "error"
});

if (resultado.ok !== 1) {
  throw new Error("No se pudo configurar el validador.");
}

print("Validador configurado correctamente.");

// 3. Crear indice unico para id_plato
bd.getCollection(nombreColeccion).createIndex(
  { id_plato: 1 },
  {
    unique: true,
    name: "idx_id_plato_unico"
  }
);

print("Indice unico verificado correctamente.");
