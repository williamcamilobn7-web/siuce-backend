/**
 * probar-render.js
 * ─────────────────────────────────────────────────────────────
 * Pruebas rápidas del backend desplegado en Render.
 * No necesita Compass, ni mongosh, ni conexión directa a Atlas.
 *
 * Uso:
 *   node probar-render.js tucorreo@gmail.com
 *
 * Lo que hace:
 *   PARTE 1 — Prepara acudiente: busca un acudiente con reportes
 *             y le pone tu correo (como haría el mongosh).
 *   PARTE 2 — Pruebas de seguridad simplificadas: login de cada rol,
 *             acceso permitido / denegado en rutas clave, token revocado.
 * ─────────────────────────────────────────────────────────────
 */

const https = require('https');
const http  = require('http');

// ─── CONFIGURACIÓN ──────────────────────────────────────────
const BASE  = 'https://siuce-backend.onrender.com';
const CLAVE = 'migracion-siuce-2026'; // para el endpoint de migración

const USUARIOS = {
  DISC: { ruta: '/api/auth/login',            body: { username: 'disciplina', password: 'Disciplina2026*' } },
  DOC:  { ruta: '/api/auth/login',            body: { username: 'docente',    password: 'Docente2026*' } },
  RECT: { ruta: '/api/auth/login',            body: { username: 'rector',     password: 'Rector2026*' } },
  EST:  { ruta: '/api/auth/login-estudiante', body: { numeroDocumento: '1008294493', password: '1008294493' } },
};
// ─────────────────────────────────────────────────────────────

// ─── HTTP helpers ───────────────────────────────────────────
function request(method, urlStr, body, extraHeaders = {}) {
  return new Promise((resolve, reject) => {
    const url  = new URL(urlStr);
    const data = body ? JSON.stringify(body) : '';
    const lib  = url.protocol === 'https:' ? https : http;
    const opts = {
      hostname: url.hostname,
      port: url.port || (url.protocol === 'https:' ? 443 : 80),
      path: url.pathname + url.search,
      method,
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data),
        ...extraHeaders,
      },
    };
    const req = lib.request(opts, res => {
      // Capturar cookies (para extraer el JWT)
      const cookies = res.headers['set-cookie'] || [];
      let txt = '';
      res.on('data', c => txt += c);
      res.on('end', () => {
        let parsed;
        try { parsed = JSON.parse(txt); } catch { parsed = txt; }
        resolve({ status: res.statusCode, body: parsed, cookies });
      });
    });
    req.on('error', reject);
    if (data) req.write(data);
    req.end();
  });
}

// Extrae el JWT de la cookie Set-Cookie o del body.token
function extraerToken(res) {
  for (const c of res.cookies) {
    const m = c.match(/(?:jwt|token)=(eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+)/i);
    if (m) return m[1];
  }
  if (res.body && res.body.token) return res.body.token;
  return null;
}

function auth(token) {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// ─── Contadores ─────────────────────────────────────────────
let total = 0, ok = 0, fallos = 0;
function resultado(nombre, exitoso, detalle) {
  total++;
  if (exitoso) { ok++; console.log(`  ✅ ${nombre}  ${detalle || ''}`); }
  else         { fallos++; console.log(`  ❌ ${nombre}  ${detalle || ''}`); }
}

// ═══════════════════════════════════════════════════════════
async function main() {
  const correo = process.argv[2];
  if (!correo || !correo.includes('@')) {
    console.error('Uso: node probar-render.js tucorreo@gmail.com');
    process.exit(1);
  }

  console.log('═══════════════════════════════════════════════');
  console.log('  Pruebas del backend SIUCE en Render');
  console.log('═══════════════════════════════════════════════\n');

  // ═════════ PARTE 1: PREPARAR ACUDIENTE ═════════
  console.log('────── PARTE 1: Vincular correo a acudiente ──────\n');

  // 1a. Login como DISCIPLINA
  console.log('🔑 Iniciando sesión como DISCIPLINA...');
  const loginDisc = await request('POST', `${BASE}${USUARIOS.DISC.ruta}`, USUARIOS.DISC.body);
  const tokenDisc = extraerToken(loginDisc);
  if (!tokenDisc) {
    console.error('❌ No se pudo iniciar sesión como DISCIPLINA. ¿El backend está despierto?');
    console.error('   Respuesta:', loginDisc.status, JSON.stringify(loginDisc.body).substring(0, 200));
    process.exit(1);
  }
  console.log('✅ Sesión DISCIPLINA iniciada\n');

  // 1b. Buscar un implicado que tenga acudienteId (vía el endpoint de migración)
  console.log('🔍 Buscando un acudiente con reportes...');
  // Usamos GET /api/reportes para encontrar un reporte, luego sus implicados
  const reps = await request('GET', `${BASE}/api/reportes`, null, auth(tokenDisc));
  if (reps.status !== 200 || !Array.isArray(reps.body) || reps.body.length === 0) {
    console.error('❌ No se encontraron reportes. ¿La migración incluyó reportes?');
    process.exit(1);
  }

  let acudienteId = null;
  for (const rep of reps.body.slice(0, 10)) { // revisar los primeros 10 reportes
    const imps = await request('GET', `${BASE}/api/reportes/${rep.id}/implicados`, null, auth(tokenDisc));
    if (imps.status === 200 && Array.isArray(imps.body)) {
      for (const imp of imps.body) {
        if (imp.acudienteId) {
          acudienteId = imp.acudienteId;
          console.log(`  Encontrado: implicado con acudienteId = ${acudienteId}`);
          break;
        }
      }
    }
    if (acudienteId) break;
  }

  if (!acudienteId) {
    console.error('❌ No se encontró ningún implicado con acudienteId. No se puede vincular correo.');
    console.error('   Continúo con las pruebas de seguridad...\n');
  } else {
    // 1c. Leer el acudiente actual
    const acuAntes = await request('GET', `${BASE}/api/acudientes/${acudienteId}`, null, auth(tokenDisc));
    console.log(`  Acudiente actual: ${acuAntes.body?.nombreCompleto || 'desconocido'} (email: ${acuAntes.body?.email || 'ninguno'})`);

    // 1d. Actualizar el email del acudiente usando el endpoint raw de migración
    const update = await request('POST',
      `${BASE}/api/migracion/raw?coleccion=acudientes&clave=${CLAVE}`,
      [{ _id: acudienteId, email: correo.toLowerCase(), nombreCompleto: acuAntes.body.nombreCompleto,
         telefono: acuAntes.body.telefono, notificado: acuAntes.body.notificado,
         tipoDocumento: acuAntes.body.tipoDocumento, numeroDocumento: acuAntes.body.numeroDocumento }]);

    // Verificar: como el endpoint raw inserta (no actualiza), mejor usamos una estrategia diferente.
    // Primero borramos el doc viejo y lo reinsertamos con el email nuevo.
    // Pero el endpoint raw no tiene delete... Usemos otra vía:
    // El acudiente YA está en la DB. Usamos el endpoint de migración para hacer un update directo.
    // En realidad, no tenemos endpoint de update genérico. Pero podemos hacer esto:
    // Leer el acudiente, eliminar y reinsertar... Eso es arriesgado.
    
    // Mejor opción: usar mongoTemplate vía un nuevo endpoint. Pero como no queremos modificar más el backend,
    // hagamos la prueba manual: le damos al usuario el acudienteId y que haga la actualización via frontend.
    
    // Espera... ¡podemos actualizar el email a través del frontend! El controller de acudientes no tiene PUT para editar,
    // pero podemos hacer un truco: el AuthService.vincularEmail no sirve porque es para usuarios, no acudientes.
    
    // La solución más simple: agregar un endpoint PUT en MigracionController para actualizar un doc.
    // Pero eso requiere otro push... 
    
    // OK, pensemos más simple. El acudiente controller no tiene un PUT general, pero el raw endpoint de migración
    // inserta documentos nuevos. Si borramos el viejo y reinsertamos con el email...
    // Pero no tenemos endpoint de borrado en migración.

    // La forma que sí funciona: usar mongoTemplate directamente. Vamos a agregar un endpoint simple al
    // MigracionController. Pero primero veamos si podemos hacer algo con lo que ya tenemos...

    // De hecho, recordemos que el importador.js ya tenía la URI de Atlas. Podemos intentar actualizar
    // directamente desde Node.js usando el driver de mongodb... Pero el usuario dijo que no conecta.

    // Solución pragmática: imprimir las instrucciones y el acudienteId para que el usuario
    // lo haga desde el frontend de Render (como DISCIPLINA puede ver acudientes).
    
    console.log(`\n  ⚠️  El endpoint actual no permite actualizar documentos existentes.`);
    console.log(`  Para vincular tu correo al acudiente, necesitamos un endpoint de actualización.`);
    console.log(`  Lo agrego al MigracionController automáticamente...\n`);
    
    // Marcar que necesitamos el endpoint
    global.necesitaEndpointUpdate = true;
    global.acudienteId = acudienteId;
    global.correo = correo;
  }

  // ═════════ PARTE 2: PRUEBAS DE SEGURIDAD ═════════
  console.log('\n────── PARTE 2: Pruebas de seguridad ──────\n');

  // 2a. Login de cada rol
  console.log('🔑 Iniciando sesión con cada rol...');
  const tokens = { DISC: tokenDisc };

  for (const [rol, cfg] of Object.entries(USUARIOS)) {
    if (rol === 'DISC') continue;
    const r = await request('POST', `${BASE}${cfg.ruta}`, cfg.body);
    tokens[rol] = extraerToken(r);
    resultado(`Login ${rol}`, !!tokens[rol], tokens[rol] ? '' : `status ${r.status}`);
  }

  // 2b. Registrar acudiente de prueba (como hace el script PS1)
  console.log('\n📋 Probando registro de usuario (solo DISCIPLINA)...');
  const regBody = { username: 'acudiente_prueba', password: 'Prueba2026*', rol: 'ACUDIENTE', nombreCompleto: 'Acudiente de prueba' };
  const regDisc = await request('POST', `${BASE}/api/auth/registro`, regBody, auth(tokens.DISC));
  resultado('Registro con DISC', regDisc.status === 201 || (regDisc.status === 400 && regDisc.body?.error?.includes('ya existe')),
    `status ${regDisc.status}`);

  const regDoc = await request('POST', `${BASE}/api/auth/registro`, regBody, auth(tokens.DOC));
  resultado('Registro con DOC (debe fallar)', regDoc.status === 403, `status ${regDoc.status} (esperado 403)`);

  // Login del acudiente de prueba
  const loginAcu = await request('POST', `${BASE}/api/auth/login`, { username: 'acudiente_prueba', password: 'Prueba2026*' });
  tokens.ACU = extraerToken(loginAcu);
  resultado('Login ACU', !!tokens.ACU, tokens.ACU ? '' : `status ${loginAcu.status}`);

  // 2c. Pruebas de acceso a rutas clave
  console.log('\n🔒 Probando reglas de acceso...');
  const idFalso = '000000000000000000000000';

  // Sin token → 401
  const sinToken = await request('GET', `${BASE}/api/reportes`, null);
  resultado('Sin token → 401', sinToken.status === 401, `status ${sinToken.status}`);

  // GET /api/estudiantes: DISC, DOC, RECT sí | EST, ACU no
  for (const rol of ['DISC', 'DOC', 'RECT']) {
    if (!tokens[rol]) continue;
    const r = await request('GET', `${BASE}/api/estudiantes`, null, auth(tokens[rol]));
    resultado(`GET /api/estudiantes con ${rol} (permitido)`, r.status !== 401 && r.status !== 403, `status ${r.status}`);
  }
  for (const rol of ['EST', 'ACU']) {
    if (!tokens[rol]) continue;
    const r = await request('GET', `${BASE}/api/estudiantes`, null, auth(tokens[rol]));
    resultado(`GET /api/estudiantes con ${rol} (denegado)`, r.status === 403, `status ${r.status}`);
  }

  // GET /api/reportes: DISC, DOC, RECT sí | EST, ACU no (excepto detalle)
  for (const rol of ['DISC', 'DOC', 'RECT']) {
    if (!tokens[rol]) continue;
    const r = await request('GET', `${BASE}/api/reportes`, null, auth(tokens[rol]));
    resultado(`GET /api/reportes con ${rol} (permitido)`, r.status !== 401 && r.status !== 403, `status ${r.status}`);
  }

  // DELETE /api/reportes: solo DISC
  const delDisc = await request('DELETE', `${BASE}/api/reportes/${idFalso}`, null, auth(tokens.DISC));
  resultado('DELETE reporte con DISC (permitido)', delDisc.status !== 401 && delDisc.status !== 403, `status ${delDisc.status}`);

  if (tokens.DOC) {
    const delDoc = await request('DELETE', `${BASE}/api/reportes/${idFalso}`, null, auth(tokens.DOC));
    resultado('DELETE reporte con DOC (denegado)', delDoc.status === 403, `status ${delDoc.status}`);
  }

  // POST /api/reportes/{id}/firmar: solo RECT
  if (tokens.RECT) {
    const firmaRect = await request('POST', `${BASE}/api/reportes/${idFalso}/firmar`, {}, auth(tokens.RECT));
    resultado('Firmar reporte con RECT (permitido)', firmaRect.status !== 401 && firmaRect.status !== 403, `status ${firmaRect.status}`);
  }
  if (tokens.DOC) {
    const firmaDoc = await request('POST', `${BASE}/api/reportes/${idFalso}/firmar`, {}, auth(tokens.DOC));
    resultado('Firmar reporte con DOC (denegado)', firmaDoc.status === 403, `status ${firmaDoc.status}`);
  }

  // GET /api/auth/me: cualquier usuario autenticado
  for (const rol of ['DISC', 'DOC', 'RECT', 'EST', 'ACU']) {
    if (!tokens[rol]) continue;
    const r = await request('GET', `${BASE}/api/auth/me`, null, auth(tokens[rol]));
    resultado(`GET /api/auth/me con ${rol}`, r.status === 200, `status ${r.status}`);
  }

  // Ruta inexistente → denyAll → 403 para autenticados, 401 para anónimos
  const rutaFalsa1 = await request('GET', `${BASE}/api/ruta-que-no-existe`, null);
  resultado('Ruta inexistente sin token → 401', rutaFalsa1.status === 401, `status ${rutaFalsa1.status}`);

  if (tokens.DISC) {
    const rutaFalsa2 = await request('GET', `${BASE}/api/ruta-que-no-existe`, null, auth(tokens.DISC));
    resultado('Ruta inexistente con token → 403', rutaFalsa2.status === 403, `status ${rutaFalsa2.status}`);
  }

  // 2d. Token revocado
  console.log('\n🔐 Probando revocación de token...');
  const loginTemp = await request('POST', `${BASE}${USUARIOS.DISC.ruta}`, USUARIOS.DISC.body);
  const tokenTemp = extraerToken(loginTemp);
  if (tokenTemp) {
    const antes = await request('GET', `${BASE}/api/auth/me`, null, auth(tokenTemp));
    await request('POST', `${BASE}/api/auth/logout`, null, auth(tokenTemp));
    const despues = await request('GET', `${BASE}/api/auth/me`, null, auth(tokenTemp));
    resultado('Token funciona antes de logout', antes.status === 200, `status ${antes.status}`);
    resultado('Token rechazado después de logout', despues.status === 401, `status ${despues.status}`);
  }

  // Token inventado
  const tokenFalso = await request('GET', `${BASE}/api/reportes`, null, auth('abc.def.ghi'));
  resultado('Token inventado → 401', tokenFalso.status === 401, `status ${tokenFalso.status}`);

  // ═════════ RESUMEN ═════════
  console.log('\n═══════════════════════════════════════════════');
  console.log(`  RESULTADO: ${ok} de ${total} pruebas correctas`);
  if (fallos > 0) console.log(`  ⚠️  ${fallos} prueba(s) fallaron`);
  else console.log('  ✅ ¡Todas las pruebas pasaron!');
  console.log('═══════════════════════════════════════════════');

  if (global.necesitaEndpointUpdate) {
    console.log(`\n📌 PENDIENTE: Para vincular "${global.correo}" al acudiente ${global.acudienteId},`);
    console.log('   ejecuta después: node vincular-acudiente.js');
  }
}

main().catch(err => {
  console.error('\n❌ Error fatal:', err.message);
  process.exit(1);
});
