const fs = require('fs');
const { MongoClient } = require('mongodb');

// Pega aquí la URI de MongoDB Atlas de tu compañero
const MONGO_URI = "mongodb+srv://USUARIO:PASSWORD@CLUSTER.mongodb.net/?retryWrites=true&w=majority";
const NOMBRE_BD = "colegio_disiplina";

async function importar() {
  const client = new MongoClient(MONGO_URI);

  try {
    await client.connect();
    const db = client.db(NOMBRE_BD);

    // 1. Importar Estudiantes (convirtiendo fechaNacimiento a Date/ISODate)
    const estudiantes = JSON.parse(fs.readFileSync("estudiantes.json", "utf-8")).map(est => ({
      ...est,
      fechaNacimiento: new Date(est.fechaNacimiento)
    }));

    if (estudiantes.length > 0) {
      const resEst = await db.collection("estudiantes").insertMany(estudiantes);
      console.log(`Insertados ${resEst.insertedCount} documentos en 'estudiantes'.`);
    }

    // 2. Importar Acudientes
    const acudientes = JSON.parse(fs.readFileSync("acudientes.json", "utf-8"));

    if (acudientes.length > 0) {
      const resAcu = await db.collection("acudientes").insertMany(acudientes);
      console.log(`Insertados ${resAcu.insertedCount} documentos en 'acudientes'.`);
    }
  } finally {
    await client.close();
    console.log("Importación a MongoDB Atlas finalizada.");
  }
}

importar().catch(console.error);