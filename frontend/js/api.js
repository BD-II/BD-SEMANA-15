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

    // Consultar el catalogo integrado:
    // PostgreSQL proporciona los datos oficiales del plato.
    // MongoDB proporciona la informacion complementaria.
    const respuesta = await fetch(`${API_URL}/platos-integrados`);

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
function _guardarLocal(l) { try { localStorage.setItem("pedidos", JSON.stringify(l)); } catch (e) { } }

async function obtenerPedidos() {
  if (USAR_API_PEDIDOS) return (await fetch(`${API_URL}/pedidos`)).json();      // GET /api/pedidos
  return _leerLocal();
}
// Consultar una ruta de pedidos y convertir su respuesta JSON.
async function consultarPedidosEnAPI(ruta) {
  const respuesta = await fetch(`${API_URL}${ruta}`);
  const datos = await respuesta.json();

  if (!respuesta.ok) {
    throw new Error(datos.error || `Error HTTP ${respuesta.status}`);
  }

  return datos;
}

function listarPedidosEnAPI() {
  return consultarPedidosEnAPI("/pedidos");
}

// Enviar el método elegido. Java obtiene el importe desde PostgreSQL.
async function registrarPagoEnAPI(id, metodoPago) {
  const respuesta = await fetch(
    `${API_URL}/pedidos/${encodeURIComponent(id)}/pago`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ metodoPago })
    }
  );

  const datos = await respuesta.json();
  if (!respuesta.ok) {
    throw new Error(datos.error || `Error HTTP ${respuesta.status}`);
  }

  return datos;
}

// Enviar el estado solicitado para el pedido indicado en la URL.
async function cambiarEstadoPedidoEnAPI(id, estado) {
  const respuesta = await fetch(`${API_URL}/pedidos/${encodeURIComponent(id)}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ estado })
  });

  const datos = await respuesta.json();

  // Si PostgreSQL rechaza la transición, mostrar el mensaje que devuelve Java.
  if (!respuesta.ok) {
    throw new Error(datos.error || `Error HTTP ${respuesta.status}`);
  }

  return datos;
}

// El botón actual sigue usando esta función para enviar el estado 1.
function marcarPreparandoEnAPI(id) {
  return cambiarEstadoPedidoEnAPI(id, 1);
}
function obtenerDetallePedidoEnAPI(id) {
  // El código se envía como un segmento de la URL, por ejemplo PD057.
  return consultarPedidosEnAPI(`/pedidos/${encodeURIComponent(id)}`);
}

// Codificar el texto para que la URL acepte espacios, tildes y otros caracteres.
async function buscarClientesEnAPI(texto) {
  const parametros = new URLSearchParams({ buscar: texto });
  const respuesta = await fetch(`${API_URL}/clientes?${parametros}`);
  const datos = await respuesta.json();

  if (!respuesta.ok) {
    throw new Error(datos.error || `Error HTTP ${respuesta.status}`);
  }

  return datos;
}
async function registrarClienteEnAPI(cliente) {
  const respuesta = await fetch(`${API_URL}/clientes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(cliente)
  });

  const datos = await respuesta.json();

  if (!respuesta.ok) {
    throw new Error(datos.error || `Error HTTP ${respuesta.status}`);
  }

  return datos;
}

// Envía el pedido a Java y devuelve el código generado en PostgreSQL.
async function registrarPedidoEnAPI(datos) {
  const respuesta = await fetch(`${API_URL}/pedidos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(datos)
  });

  // La API devuelve JSON tanto para el pedido creado como para sus errores.
  const resultado = await respuesta.json();

  // fetch también recibe respuestas HTTP de error. Aquí comprobamos el estado.
  if (!respuesta.ok) {
    throw new Error(resultado.error || `Error HTTP ${respuesta.status}`);
  }

  return resultado;
}

async function crearPedido(datos) {
  if (USAR_API_PEDIDOS) {                                                        // POST /api/pedidos
    const r = await fetch(`${API_URL}/pedidos`, {
      method: "POST",
      headers: { "Content-Type": "application/json" }, body: JSON.stringify(datos)
    });
    return r.json();
  }
  const lista = _leerLocal();
  const pedido = {
    ...datos, id: lista.reduce((m, p) => Math.max(m, p.id), 0) + 1,
    fecha: new Date().toISOString(), estado: "Pendiente"
  };
  lista.push(pedido); _guardarLocal(lista);
  return pedido;
}
async function cambiarEstado(id, estado) {
  if (USAR_API_PEDIDOS) {                                                        // PATCH /api/pedidos/:id
    await fetch(`${API_URL}/pedidos/${id}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" }, body: JSON.stringify({ estado })
    });
    return;
  }
  const lista = _leerLocal(); const p = lista.find(x => x.id === id);
  if (p) { p.estado = estado; _guardarLocal(lista); }
}
