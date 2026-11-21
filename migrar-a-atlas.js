/**
 * migrar-a-atlas.js
 * Envía los datos exportados de tu MongoDB local al backend en Render,
 * que los inserta directamente en Atlas.
 *
 * Uso: node migrar-a-atlas.js
 */

const fs   = require('fs');
const path = require('path');
const https = require('https');
const http  = require('http');

// ─── CONFIGURACIÓN ────────────────────────────────────────────────────────────
const RENDER_URL = 'https://siuce-backend.onrender.com'; // URL de tu backend en Render
const CLAVE      = 'migracion-siuce-2026';               // Debe coincidir con la variable de entorno en Render
const CARPETA    = path.join(__dirname, 'exportacion-atlas');
const LOTE       = 500; // documentos por petición (evita timeouts)
// ─────────────────────────────────────────────────────────────────────────────

function peticion(urlStr, metodo, body) {
  return new Promise((resolve, reject) => {
    const url     = new URL(urlStr);
    const datos   = JSON.stringify(body);
    const opciones = {
      hostname: url.hostname,
      port:     url.port || (url.protocol === 'https:' ? 443 : 80),
      path:     url.pathname + url.search,
      method:   metodo,
      headers:  {
        'Content-Type':   'application/json',
        'Content-Length': Buffer.byteLength(datos),
      },
    };

    const lib = url.protocol === 'https:' ? https : http;
    const req = lib.request(opciones, (res) => {
      let txt = '';
      res.on('data', c => txt += c);
      res.on('end', () => {
        try { resolve({ status: res.statusCode, body: JSON.parse(txt) }); }
        catch { resolve({ status: res.statusCode, body: txt }); }
      });
    });
    req.on('error', reject);
    req.write(datos);
    req.end();
  });
}

function get(urlStr) {
  return new Promise((resolve, reject) => {
    const url = new URL(urlStr);
    const lib = url.protocol === 'https:' ? https : http;
    lib.get(urlStr, (res) => {
      let txt = '';
      res.on('data', c => txt += c);
      res.on('end', () => {
        try { resolve({ status: res.statusCode, body: JSON.parse(txt) }); }
        catch { resolve({ status: res.statusCode, body: txt }); }
      });
    }).on('error', reject);
  });
}

function leer(archivo) {
  const ruta = path.join(CARPETA, archivo);
  if (!fs.existsSync(ruta)) return null;
  return JSON.parse(fs.readFileSync(ruta, 'utf-8'));
}

// Convierte { $date: "..." } → string de fecha que Spring entiende (LocalDate yyyy-MM-dd)
function limpiarFechas(docs) {
  return docs.map(doc => {
    const nuevo = { ...doc };
    // fechaNacimiento: Atlas exporta como { $date: "..." }, Spring espera "yyyy-MM-dd"
    if (nuevo.fechaNacimiento && nuevo.fechaNacimiento.$date) {
      nuevo.fechaNacimiento = nuevo.fechaNacimiento.$date.substring(0, 10);
    }
    // Eliminar _id exportado para que MongoDB genere uno nuevo en Atlas
    delete nuevo._id;
    return nuevo;
  });
}

function limpiarRaw(docs) {
  return docs.map(doc => {
    const nuevo = { ...doc };
    delete nuevo._id;
    // Convertir { $oid } → string
    for (const k of Object.keys(nuevo)) {
      if (nuevo[k] && typeof nuevo[k] === 'object' && nuevo[k].$oid) {
        nuevo[k] = nuevo[k].$oid;
      }
      if (nuevo[k] && typeof nuevo[k] === 'object' && nuevo[k].$date) {
        nuevo[k] = nuevo[k].$date;
      }
    }
    return nuevo;
  });
}

async function enviarEnLotes(endpoint, docs, etiqueta) {
  let total = 0;
  for (let i = 0; i < docs.length; i += LOTE) {
    const lote = docs.slice(i, i + LOTE);
    const url  = `${RENDER_URL}/api/migracion/${endpoint}?clave=${CLAVE}`;
    process.stdout.write(`  Lote ${Math.floor(i/LOTE)+1}/${Math.ceil(docs.length/LOTE)} (${lote.length} docs)... `);
    const r = await peticion(url, 'POST', lote);
    if (r.status === 200) {
      total += r.body.insertados || lote.length;
      console.log(`✅ ${r.body.insertados ?? 'ok'}`);
    } else {
      console.log(`❌ Error ${r.status}:`, JSON.stringify(r.body).substring(0, 200));
    }
  }
  console.log(`  → ${etiqueta}: ${total} insertados en total\n`);
}

async function enviarRaw(coleccion, docs) {
  const limpios = limpiarRaw(docs);
  await enviarEnLotes(`raw?coleccion=${coleccion}&`, limpios.map(d => d), coleccion);
}

async function main() {
  console.log('═══════════════════════════════════════════════');
  console.log('  Migración local → Render → Atlas');
  console.log('═══════════════════════════════════════════════\n');

  // 1. Verificar que el backend responde
  console.log('🔗 Verificando conexión con Render...');
  try {
    const estado = await get(`${RENDER_URL}/api/migracion/estado?clave=${CLAVE}`);
    if (estado.status === 503) {
      console.error('❌ La variable MIGRACION_CLAVE no está configurada en Render todavía.');
      console.error('   Configúrala primero (ver instrucciones abajo) y vuelve a ejecutar.');
      process.exit(1);
    }
    if (estado.status === 403) {
      console.error('❌ Clave incorrecta. Verifica que MIGRACION_CLAVE en Render coincida con CLAVE en este script.');
      process.exit(1);
    }
    if (estado.status !== 200) {
      console.error('❌ Error inesperado:', estado.status, estado.body);
      process.exit(1);
    }
    console.log('✅ Conectado. Estado actual en Atlas:', JSON.stringify(estado.body));
  } catch (e) {
    console.error('❌ No se pudo conectar con Render:', e.message);
    console.error('   Verifica que el backend esté desplegado y la URL sea correcta.');
    process.exit(1);
  }

  // 2. Limpiar Atlas antes de importar
  console.log('\n🗑️  Limpiando colecciones en Atlas...');
  const limpieza = await peticion(`${RENDER_URL}/api/migracion/limpiar?clave=${CLAVE}`, 'POST', {});
  console.log('  →', limpieza.body.mensaje || limpieza.body);

  // 3. Importar colecciones pequeñas (raw: usuarios, reportes, implicados, colegios, etc.)
  const coleccionesRaw = [
    'usuarios', 'reportes', 'implicados', 'colegios',
    'rectores', 'entidades_salud', 'policia', 'intentos_login'
  ];

  for (const col of coleccionesRaw) {
    const docs = leer(`${col}.json`);
    if (!docs || docs.length === 0) { console.log(`⏭️  ${col}: vacío, saltando`); continue; }
    console.log(`\n📦 Importando ${col} (${docs.length} docs)...`);
    const limpios = limpiarRaw(docs);
    await enviarEnLotes(`raw?coleccion=${col}&`, limpios, col);
  }

  // 4. Importar estudiantes (endpoint tipado con conversión de fechas)
  const estudiantes = leer('estudiantes.json');
  if (estudiantes && estudiantes.length > 0) {
    console.log(`\n🎓 Importando estudiantes (${estudiantes.length} docs)...`);
    await enviarEnLotes('estudiantes', limpiarFechas(estudiantes), 'estudiantes');
  }

  // 5. Importar acudientes
  const acudientes = leer('acudientes.json');
  if (acudientes && acudientes.length > 0) {
    console.log(`\n👨‍👩‍👦 Importando acudientes (${acudientes.length} docs)...`);
    await enviarEnLotes('acudientes', limpiarRaw(acudientes), 'acudientes');
  }

  // 6. Verificar resultado final
  console.log('\n📊 Verificando resultado final en Atlas...');
  const final = await get(`${RENDER_URL}/api/migracion/estado?clave=${CLAVE}`);
  console.log('  Estado final:', JSON.stringify(final.body));

  console.log('\n✅ ¡Migración completada!');
  console.log('\n⚠️  IMPORTANTE: elimina la variable MIGRACION_CLAVE de Render para deshabilitar el endpoint.');
}

main().catch(err => {
  console.error('\n❌ Error fatal:', err.message);
  process.exit(1);
});
