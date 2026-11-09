
// CATÁLOGO LOCAL DE RESPALDO
// Datos basados en los 40 productos del catálogo MongoDB.
// Cuando se active la API, los productos vendrán del backend Java.

const PRODUCTOS = [
  // ENTRADAS
  ["P001", "Papa a la Huancaina", "Entrada", 12.00],
  ["P002", "Causa Limeña", "Entrada", 14.00],
  ["P003", "Ocopa Arequipeña", "Entrada", 13.50],
  ["P004", "Tequeños de Queso", "Entrada", 11.00],
  ["P005", "Ensalada Cesar", "Entrada", 15.00],
  ["P006", "Anticuchos", "Entrada", 16.00],
  ["P007", "Tequeños de Pollo", "Entrada", 13.00],
  ["P008", "Tamal Criollo", "Entrada", 10.00],
  ["P009", "Wantan Frito", "Entrada", 9.00],
  ["P010", "Sopa Criolla", "Entrada", 14.50],

  // FONDOS
  ["P011", "Lomo Saltado", "Fondo", 25.00],
  ["P012", "Arroz Chaufa", "Fondo", 20.00],
  ["P013", "Arroz con Pollo Especial", "Fondo", 27.00],
  ["P014", "Aji de Gallina", "Fondo", 24.00],
  ["P015", "Pollo Saltado", "Fondo", 25.00],
  ["P016", "Tallarines Verdes", "Fondo", 22.00],
  ["P017", "Pollo a la Brasa", "Fondo", 26.00],
  ["P018", "Pollo Broaster", "Fondo", 25.00],
  ["P019", "Alitas Broaster", "Fondo", 23.00],
  ["P020", "Tacu Tacu con Lomo", "Fondo", 31.00],
  ["P021", "Carapulcra", "Fondo", 23.00],
  ["P022", "Arroz con Pollo", "Fondo", 24.50],
  ["P023", "Milanesa con Papas", "Fondo", 27.00],
  ["P024", "Pollo al Horno", "Fondo", 26.00],
  ["P025", "Pollo Guisado", "Fondo", 24.00],
  ["P026", "Pollo a la Leña", "Fondo", 18.00],
  ["P027", "Pollo a la Plancha", "Fondo", 25.00],
  ["P028", "Pollo Frito con Arroz", "Fondo", 24.00],

  // POSTRES
  ["P029", "Suspiro a la Limeña", "Postre", 9.50],
  ["P030", "Arroz con Leche", "Postre", 7.00],
  ["P031", "Mazamorra Morada", "Postre", 7.00],
  ["P032", "Picarones", "Postre", 10.00],
  ["P033", "Torta de Chocolate", "Postre", 11.00],
  ["P034", "Cheesecake de Fresa", "Postre", 12.00],
  ["P035", "Helado Artesanal", "Postre", 8.50],

  // BEBIDAS
  ["P036", "Inca Kola", "Bebida", 5.00],
  ["P037", "Chicha Morada", "Bebida", 4.50],
  ["P038", "Limonada", "Bebida", 6.00],
  ["P039", "Maracuya Frozen", "Bebida", 8.00],
  ["P040", "Cafe Americano", "Bebida", 5.50]
];

// Adaptación de las categorías MongoDB al frontend.
const CATEGORIAS = {
  Entrada: "Entradas",
  Fondo: "Platos",
  Postre: "Postres",
  Bebida: "Bebidas"
};

const EMOJIS = {
  Entrada: "🥗",
  Fondo: "🍽️",
  Postre: "🍰",
  Bebida: "🥤"
};

// Generación del catálogo compatible con la interfaz.
const CARTA_LOCAL = PRODUCTOS.map(
  ([codigo, nombre, categoria, precio]) => ({
    id: codigo,
    categoria: CATEGORIAS[categoria],
    nombre: nombre,
    precio: precio,
    emoji: EMOJIS[categoria],
    //Se reemplazó esta parte: imagen: "", por esta
    //JavaScript utiliza el código de cada producto para generar automáticamente el nombre de su imagen.
    imagen: `${codigo}.jpg`,
    etiqueta: "",
    descripcion: "",
    ingredientes: []
  })
);

// Formato monetario peruano.
const soles = n => "S/ " + Number(n).toFixed(2);
