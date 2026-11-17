package com.polleriaparedes.pruebas;

import com.polleriaparedes.config.ConexionMongoDB; // Selecciona el catálogo de pruebas.
import com.polleriaparedes.dao.mongo.PlatoDAO;      // Ejecuta las operaciones CRUD.
import java.util.Date;                             // Representa las fechas del documento.
import java.util.List;                             // Construye los arreglos del catálogo.
import javax.swing.JOptionPane;                    // Pausa el ensayo para tomar capturas.
import org.bson.Document;                          // Representa un documento BSON en Java.

public class PruebaCrudCatalogoMongoDB {

    public static void main(String[] args) {
        String basePruebas = "PolleriaParedes_Catalogo_Pruebas";
        String codigo = "P999";

        // Elegir explícitamente el catálogo de pruebas.
        ConexionMongoDB conexion = new ConexionMongoDB(basePruebas);

        try {
            var base = conexion.getBaseDatos();
            System.out.println("Base MongoDB: " + base.getName());

            // Usar el mismo DAO que documentamos en el apartado 6.2.
            PlatoDAO dao = new PlatoDAO(base);

            if (dao.buscarPlato(codigo) != null) {
                System.out.println("P999 ya existe. La prueba se detuvo antes de modificarlo.");
                return;
            }

            // Incluir los campos que requiere el validador de la colección.
            Date ahora = new Date();
            Document temporal = new Document("id_plato", codigo)
                    .append("nombre", "Plato temporal de demostracion")
                    .append("categoria", "Fondo")
                    .append("descripcion", "Descripcion inicial para la prueba desde Java.")
                    .append("imagenes", List.of())
                    .append("presentaciones", List.of())
                    .append("etiquetas", List.of("prueba"))
                    .append("estado", "borrador")
                    .append("fecha_creacion", ahora)
                    .append("fecha_actualizacion", ahora);

            // CREATE: insertar el documento temporal.
            boolean creado = dao.registrarPlato(temporal);
            System.out.println("CREATE | P999 insertado: " + creado);
            if (!creado) return;

            try {
                // READ: volver a consultar el documento desde MongoDB.
                Document encontrado = dao.buscarPlato(codigo);
                if (encontrado == null) {
                    throw new IllegalStateException("La consulta no encontro P999.");
                }
                System.out.println("READ   | Descripcion: " + encontrado.getString("descripcion"));

                JOptionPane.showMessageDialog(null,
                        "Captura P999 en Compass. Pulsa Aceptar para actualizarlo.");

                // UPDATE: cambiar la descripción y verificar el valor guardado.
                String descripcionNueva = "Descripcion actualizada desde Java.";
                boolean actualizado = dao.actualizarDescripcion(codigo, descripcionNueva);
                Document verificado = dao.buscarPlato(codigo);

                if (!actualizado || verificado == null
                        || !descripcionNueva.equals(verificado.getString("descripcion"))) {
                    throw new IllegalStateException("No se pudo verificar la actualización.");
                }
                System.out.println("UPDATE | Descripcion: " + verificado.getString("descripcion"));

                JOptionPane.showMessageDialog(null,
                        "Actualiza la vista de Compass y captura la nueva descripcion. "
                        + "Pulsa Aceptar para eliminar P999.");
            } finally {
                // DELETE: intentar limpiar el documento creado por esta ejecución.
                boolean eliminado = dao.eliminarPlato(codigo);
                System.out.println("DELETE | P999 eliminado: " + eliminado);
                System.out.println("Verificacion | P999 ausente: " + (dao.buscarPlato(codigo) == null));
            }
        } finally {
            conexion.cerrarConexion();
        }
    }
}