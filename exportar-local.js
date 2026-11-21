/**
 * exportar-local.js
 * Exporta TODAS las colecciones de tu MongoDB local a archivos JSON
 * listos para importar en MongoDB Atlas desde la interfaz web.
 *
 * Uso: node exportar-local.js
 */

const { MongoClient } = require('mongodb');
const fs = require('fs');
const path = require('path');

const MONGO_LOCAL = 'mongodb://localhost:27017';
const NOMBRE_BD   = 'colegio_disciplina';
const CARPETA     = path.join(__dirname, 'exportacion-atlas');

async function exportar() {
  if (!fs.existsSync(CARPETA)) fs.mkdirSync(CARPETA);

  const client = new MongoClient(MONGO_LOCAL);
  try {
    await client.connect();
    console.log('✅ Conectado a MongoDB local');

    const db = client.db(NOMBRE_BD);
    const colecciones = await db.listCollections().toArray();

    if (colecciones.length === 0) {
      console.log('⚠️  No se encontraron colecciones en la base de datos.');
      return;
    }

    for (const col of colecciones) {
      const nombre = col.name;
      const docs   = await db.collection(nombre).find({}).toArray();

      // Convertir ObjectId y Date a strings para que Atlas los acepte
      const serializado = docs.map(doc => convertir(doc));

      const archivo = path.join(CARPETA, `${nombre}.json`);
      fs.writeFileSync(archivo, JSON.stringify(serializado, null, 2), 'utf-8');
      console.log(`📦 ${nombre}: ${docs.length} documentos → ${archivo}`);
    }

    console.log('\n✅ Exportación completa. Archivos en:', CARPETA);
    console.log('\n📌 PASOS PARA IMPORTAR EN ATLAS:');
    console.log('   1. Ve a cloud.mongodb.com → tu cluster → "Browse Collections"');
    console.log('   2. Haz clic en "Add My Own Data" o selecciona tu base de datos');
    console.log('   3. En la colección, usa el botón "INSERT DOCUMENT" o...');
    console.log('   4. Mejor: en el cluster, haz clic en los 3 puntos "..." → "Load Sample Data"');
    console.log('      O usa: Cluster → Command Line Tools → mongoimport (si lo tienes)');
    console.log('\n   👉 La forma MÁS FÁCIL: sube cada .json en Atlas UI:');
    console.log('      Collections → tu_coleccion → "INSERT DOCUMENT" → pega el JSON array');
  } finally {
    await client.close();
  }
}

// Convierte ObjectId → string con formato $oid para que Atlas lo reconozca
function convertir(obj) {
  if (obj === null || obj === undefined) return obj;
  if (Array.isArray(obj)) return obj.map(convertir);

  if (typeof obj === 'object' && obj.constructor && obj.constructor.name === 'ObjectId') {
    return { $oid: obj.toString() };
  }
  if (obj instanceof Date) {
    return { $date: obj.toISOString() };
  }
  if (typeof obj === 'object') {
    const nuevo = {};
    for (const [k, v] of Object.entries(obj)) {
      nuevo[k] = convertir(v);
    }
    return nuevo;
  }
  return obj;
}

exportar().catch(err => {
  console.error('❌ Error:', err.message);
  process.exit(1);
});
