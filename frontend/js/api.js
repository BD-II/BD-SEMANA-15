// CAPA DE DATOS DEL FRONTEND
// Las páginas solo usan estas funciones. Hoy trabajan con datos locales (localStorage);
// cuando tengas el backend con MongoDB, cambia USAR_API a true y ajusta API_URL.


// Activar la API Java para consultar el catalogo MongoDB.
const USAR_API_CATALOGO = true;

// Mantener temporalmente los pedidos en localStorage.
const USAR_API_PEDIDOS = false;

// Direccion del servidor HTTP desarrollado en Java.
const API_URL = "http://127.0.0.1:8080/api";


// ---- IMÁGENES ---------------------------------------------------------
// Carpeta donde pones las fotos. El campo "imagen" del plato guarda solo el nombre
// (ej. "pollo-cuarto.jpg"). Si en Mongo guardas una URL completa, se usa tal cual.
const IMG_BASE = "img/platos/";
function urlImagen(p) {
  if (!p.imagen) return "";
  return /^(https?:)?\/\//.test(p.imagen) ? p.imagen : IMG_BASE + p.imagen;
}
// Si la imagen no existe o falla, se quita y queda el emoji de respaldo.
function foto(p) {
  const src = urlImagen(p);
  return `<div class="foto"><span>${p.emoji || "🍽️"}</span>` +
         (src ? `<img src="${src}" alt="${p.nombre}" loading="lazy" onerror="this.remove()">` : "") + `</div>`;
}

// ---- CARTA ------------------------------------------------------------

async function cargarCarta() {

    // Consultar MongoDB mediante nuestra API Java.
    if (USAR_API_CATALOGO) {

        // Enviar una solicitud HTTP GET al servidor Java.
        const respuesta = await fetch(`${API_URL}/platos`);

        // Comprobar que la solicitud fue exitosa.
        if (!respuesta.ok) {
            throw new Error(
                "Error al consultar el catalogo: "
                + respuesta.status
            );
        }

        // Convertir el JSON recibido en un arreglo JavaScript.
        const productos = await respuesta.json();

        // Verificar que la respuesta contenga una lista.
        if (!Array.isArray(productos)) {
            throw new Error(
                "La API no devolvio un arreglo de productos."
            );
        }

        // Guardar los productos consultados desde MongoDB.
        CARTA = productos;

    } else {

        // Utilizar el catalogo local cuando la API este desactivada.
        CARTA = CARTA_LOCAL;
    }

    return CARTA;
}


// ---- PEDIDOS ----------------------------------------------------------
// Forma de un pedido: { id, cliente, tipo, nota, fecha, estado, lineas:[{platoId, cantidad}] }
function _leerLocal() {
  try { return JSON.parse(localStorage.getItem("pedidos") || "[]"); } catch (e) { return []; }
}
function _guardarLocal(l) { try { localStorage.setItem("pedidos", JSON.stringify(l)); } catch (e) {} }

async function obtenerPedidos() {
  if (USAR_API_PEDIDOS) return (await fetch(`${API_URL}/pedidos`)).json();      // GET /api/pedidos
  return _leerLocal();
}
async function crearPedido(datos) {
  if (USAR_API_PEDIDOS) {                                                        // POST /api/pedidos
    const r = await fetch(`${API_URL}/pedidos`, { method: "POST",
      headers: { "Content-Type": "application/json" }, body: JSON.stringify(datos) });
    return r.json();
  }
  const lista = _leerLocal();
  const pedido = { ...datos, id: lista.reduce((m, p) => Math.max(m, p.id), 0) + 1,
                   fecha: new Date().toISOString(), estado: "Pendiente" };
  lista.push(pedido); _guardarLocal(lista);
  return pedido;
}
async function cambiarEstado(id, estado) {
  if (USAR_API_PEDIDOS) {                                                        // PATCH /api/pedidos/:id
    await fetch(`${API_URL}/pedidos/${id}`, { method: "PATCH",
      headers: { "Content-Type": "application/json" }, body: JSON.stringify({ estado }) });
    return;
  }
  const lista = _leerLocal(); const p = lista.find(x => x.id === id);
  if (p) { p.estado = estado; _guardarLocal(lista); }
}
