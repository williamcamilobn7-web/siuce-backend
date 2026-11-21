const fs = require('fs');

const CANTIDAD = 60;

const NOMBRES_M = ["Ricardo", "Darío", "Carlos", "Andrés", "Mateo", "Santiago", "Sebastián", "Juan", "Luis", "David", "Daniel", "Felipe", "Camilo", "Alejandro", "Nicolás"];
const NOMBRES_F = ["María", "Laura", "Camila", "Valentina", "Sofía", "Isabella", "Daniela", "Gabriela", "Paula", "Natalia", "Andrea", "Carolina", "Juliana", "Mariana", "Sara"];
const APELLIDOS = ["Suárez Moreno", "Delgado Rivera", "Pérez Gómez", "López Martínez", "Rodríguez Díaz", "González Castro", "Hernández Ruiz", "Ramírez Torres", "Vargas Flores", "Morales Rojas", "Ortiz Silva", "Castro Mendoza", "Jiménez Navarro", "Reyes Peña", "Molina Romero"];

const GRADOS = ["6A", "6B", "7A", "7B", "8A", "8B", "9A", "9B", "10A", "10B", "11A", "11B"];
const JORNADAS = ["MANANA", "TARDE"];
const SEDES = ["Sede Centro", "Sede Norte", "Sede Sur"];
const LUGARES = [
  ["Cundinamarca", "Bogotá"],
  ["Bolívar", "Cartagena"],
  ["Antioquia", "Medellín"],
  ["Valle del Cauca", "Cali"],
  ["Atlántico", "Barranquilla"],
  ["Santander", "Bucaramanga"]
];

const aleatorio = (arr) => arr[Math.floor(Math.random() * arr.length)];
const enteroAleatorio = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

function fechaNacimientoAleatoria() {
  const inicio = new Date('2004-01-01T05:00:00.000Z').getTime();
  const fin = new Date('2014-12-31T05:00:00.000Z').getTime();
  return new Date(enteroAleatorio(inicio, fin)).toISOString();
}

const estudiantes = [];
const acudientes = [];
const documentosUsados = new Set();

for (let i = 0; i < CANTIDAD; i++) {
  // --- Generar Estudiante ---
  const esMasculino = Math.random() < 0.5;
  const nombre = aleatorio(esMasculino ? NOMBRES_M : NOMBRES_F);
  const apellido = aleatorio(APELLIDOS);
  const genero = esMasculino ? "MASCULINO" : "FEMENINO";

  let doc = String(enteroAleatorio(1000000000, 1099999999));
  while (documentosUsados.has(doc)) {
    doc = String(enteroAleatorio(1000000000, 1099999999));
  }
  documentosUsados.add(doc);

  const [depto, municipio] = aleatorio(LUGARES);

  estudiantes.push({
    numeroDocumento: doc,
    tipoDocumento: aleatorio(["TI", "CC"]),
    nombres: nombre,
    apellidos: apellido,
    fechaNacimiento: fechaNacimientoAleatoria(),
    genero,
    grado: aleatorio(GRADOS),
    jornada: aleatorio(JORNADAS),
    sede: aleatorio(SEDES),
    paisOrigen: "Colombia",
    departamentoExpedicion: depto,
    municipioExpedicion: municipio,
    promedioAcademico: Number((Math.random() * (5.0 - 2.5) + 2.5).toFixed(1)),
    numeroInasistencias: enteroAleatorio(0, 15),
    _class: "com.colegio.entity.Estudiante"
  });

  // --- Generar Acudiente ---
  const nomAcu = aleatorio([...NOMBRES_M, ...NOMBRES_F]);
  const apeAcu = aleatorio(APELLIDOS).split(" ")[0];
  const nombreCompleto = `${nomAcu} ${apeAcu}`;
  const correoBase = `${nomAcu}.${apeAcu}${i + 1}`
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");

  const prefijoTel = aleatorio(["300", "301", "310", "311", "312", "315", "320", "321"]);
  const telefono = `${prefijoTel}${enteroAleatorio(1000000, 9999999)}`;

  acudientes.push({
    nombreCompleto,
    telefono,
    email: `${correoBase}@gmail.com`,
    notificado: Math.random() < 0.5,
    _class: "com.colegio.entity.Acudiente"
  });
}

fs.writeFileSync("estudiantes.json", JSON.stringify(estudiantes, null, 2), "utf-8");
fs.writeFileSync("acudientes.json", JSON.stringify(acudientes, null, 2), "utf-8");

console.log(`Generados ${estudiantes.length} estudiantes y ${acudientes.length} acudientes.`);