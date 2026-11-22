/**
 * vincular-acudiente.js
 * ─────────────────────────────────────────────
 * Busca un acudiente que tenga reportes y le pone tu correo.
 * Equivale al código de mongosh pero ejecutado vía la API de Render.
 *
 * Uso: node vincular-acudiente.js tucorreo@gmail.com
 * ─────────────────────────────────────────────
 */

const https = require('https');

const BASE  = 'https://siuce-backend.onrender.com';
const CLAVE = 'migracion-siuce-2026';

function request(method, urlStr, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const url  = new URL(urlStr);
    const data = body ? JSON.stringify(body) : '';
    const opts = {
      hostname: url.hostname, port: 443,
      path: url.pathname + url.search, method,
      headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data), ...headers },
    };
    const req = https.request(opts, res => {
      const cookies = res.headers['set-cookie'] || [];
      let txt = '';
      res.on('data', c => txt += c);
      res.on('end', () => {
        let parsed; try { parsed = JSON.parse(txt); } catch { parsed = txt; }
        resolve({ status: res.statusCode, body: parsed, cookies });
      });
    });
    req.on('error', reject);
    if (data) req.write(data);
    req.end();
  });
}

function extraerToken(res) {
  for (const c of (res.cookies || [])) {
    const m = c.match(/=(eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+)/);
    if (m) return m[1];
  }
  return res.body?.token || null;
}

async function main() {
  const correo = process.argv[2];
  if (!correo || !correo.includes('@')) {
    console.error('Uso: node vincular-acudiente.js tucorreo@gmail.com');
    process.exit(1);
  }

  console.log(`\n🔧 Vinculando "${correo}" a un acudiente con reportes...\n`);

  // 1. Login como DISCIPLINA
  const login = await request('POST', `${BASE}/api/auth/login`,
    { username: 'disciplina', password: 'Disciplina2026*' });
  const token = extraerToken(login);
  if (!token) { console.error('❌ No se pudo iniciar sesión como DISCIPLINA'); process.exit(1); }
  console.log('✅ Sesión DISCIPLINA iniciada');

  // 2. Buscar un reporte que tenga implicados con acudienteId
  const reps = await request('GET', `${BASE}/api/reportes`, null, { Authorization: `Bearer ${token}` });
  if (reps.status !== 200 || !reps.body?.length) { console.error('❌ No hay reportes'); process.exit(1); }

  let acudienteId = null;
  let estudianteNombre = '';
  for (const rep of reps.body.slice(0, 15)) {
    const imps = await request('GET', `${BASE}/api/reportes/${rep.id}/implicados`, null,
      { Authorization: `Bearer ${token}` });
    if (imps.status === 200 && Array.isArray(imps.body)) {
      for (const imp of imps.body) {
        if (imp.acudienteId) {
          acudienteId = imp.acudienteId;
          estudianteNombre = imp.estudianteNombre || imp.nombreEstudiante || 'N/A';
          break;
        }
      }
    }
    if (acudienteId) break;
  }

  if (!acudienteId) { console.error('❌ No se encontró ningún implicado con acudienteId'); process.exit(1); }

  // 3. Ver datos actuales del acudiente
  const acuActual = await request('GET', `${BASE}/api/acudientes/${acudienteId}`, null,
    { Authorization: `Bearer ${token}` });
  console.log(`\n📋 Acudiente encontrado:`);
  console.log(`   ID:     ${acudienteId}`);
  console.log(`   Nombre: ${acuActual.body?.nombreCompleto || 'desconocido'}`);
  console.log(`   Email:  ${acuActual.body?.email || '(ninguno)'}`);

  // 4. Actualizar el email usando el endpoint de migración
  const update = await request('PUT',
    `${BASE}/api/migracion/actualizar?clave=${CLAVE}&coleccion=acudientes&id=${acudienteId}`,
    { email: correo.toLowerCase() });

  if (update.status === 200 && update.body?.modified >= 1) {
    console.log(`\n✅ ¡Email actualizado a "${correo}"!`);
  } else if (update.status === 200 && update.body?.matched >= 1) {
    console.log(`\n✅ Documento encontrado (ya tenía ese email o se actualizó)`);
  } else {
    console.error(`\n❌ Error al actualizar:`, update.status, JSON.stringify(update.body));
    process.exit(1);
  }

  // 5. Verificar
  const acuDespues = await request('GET', `${BASE}/api/acudientes/${acudienteId}`, null,
    { Authorization: `Bearer ${token}` });
  console.log(`\n📋 Verificación:`);
  console.log(`   Nombre: ${acuDespues.body?.nombreCompleto}`);
  console.log(`   Email:  ${acuDespues.body?.email}`);
  console.log(`\n🎉 Listo. Ahora puedes entrar con Google usando "${correo}" y verás el dashboard de acudiente.`);
}

main().catch(err => { console.error('❌ Error:', err.message); process.exit(1); });
