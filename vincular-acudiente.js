/**
 * vincular-acudiente.js
 * ─────────────────────────────────────────────
 * Prepara el acudiente con reportes y le asigna tu correo en MongoDB Atlas.
 * Uso: node vincular-acudiente.js williamcamilobn7@gmail.com
 * ─────────────────────────────────────────────
 */

const https = require('https');

const BASE  = 'https://siuce-backend.onrender.com';
const CLAVE = 'migracion-siuce-2026';

function post(path) {
  return new Promise((resolve, reject) => {
    const url = new URL(BASE + path);
    const opts = {
      hostname: url.hostname,
      port: 443,
      path: url.pathname + url.search,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': 0
      }
    };
    const req = https.request(opts, res => {
      let txt = '';
      res.on('data', c => txt += c);
      res.on('end', () => {
        let parsed;
        try { parsed = JSON.parse(txt); } catch { parsed = txt; }
        resolve({ status: res.statusCode, body: parsed });
      });
    });
    req.on('error', reject);
    req.end();
  });
}

async function main() {
  const correo = process.argv[2] || 'williamcamilobn7@gmail.com';
  console.log('════════════════════════════════════════════════════════════');
  console.log(`  Vinculando correo "${correo}" a un acudiente con reportes`);
  console.log('════════════════════════════════════════════════════════════\n');

  console.log('🚀 Enviando solicitud a Render/Atlas...');
  const res = await post(`/api/migracion/preparar-acudiente?clave=${CLAVE}&email=${encodeURIComponent(correo)}`);

  if (res.status === 200) {
    console.log('✅ ¡ÉXITO TOTAL!');
    console.log('\n📋 DETALLES DE LA VINCULACIÓN EN ATLAS:');
    console.log('  • Acudiente ID:     ', res.body.acudienteId);
    console.log('  • Nombre Acudiente: ', res.body.acudienteNombre);
    console.log('  • Email asignado:   ', res.body.acudienteEmail);
    console.log('  • Estudiante a cargo:', res.body.estudianteNombre);
    console.log('  • Reporte enlazado: ', res.body.reporteId);
    console.log('  • Tipo de falta:    ', res.body.tipoFalta);
    console.log('  • Estado reporte:   ', res.body.estadoReporte);
    console.log('\n🎉 Ahora puedes ir al frontend, hacer login con Google usando:');
    console.log(`   👉 ${correo}`);
    console.log('   y verás en tu Dashboard los "Reportes de mis acudidos".\n');
  } else {
    console.error('❌ Error al preparar acudiente:', res.status, res.body);
  }
}

main().catch(console.error);
