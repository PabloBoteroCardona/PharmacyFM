package com.pharmacyfm.infrastructure.persistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utilidad de soporte para tests de integración con SQLite en archivo temporal.
 *
 * Usa un archivo SQLite en el directorio temporal del sistema en lugar de
 * :memory:, porque los adaptadores JDBC abren y cierran conexiones individualmente
 * (try-with-resources): una BD :memory: se destruye al cerrar su última conexión,
 * mientras que un archivo temporal persiste entre llamadas al repositorio.
 *
 * Cada test crea su propio archivo temporal (nombre único) y lo elimina en @AfterEach,
 * garantizando aislamiento completo entre tests.
 *
 * Uso típico:
 * <pre>
 *   File dbFile = InMemoryDb.crearArchivoTemporal();
 *   Supplier&lt;Connection&gt; factory = InMemoryDb.factoriaPara(dbFile);
 *   JdbcFormulaRepository repo = new JdbcFormulaRepository(factory);
 *   // ... test ...
 *   dbFile.delete(); // en @AfterEach
 * </pre>
 */
public class InMemoryDb {

    /**
     * Crea un archivo SQLite temporal con el esquema completo de PharmacyFM.
     *
     * @return Archivo creado con el esquema inicializado, listo para tests.
     * @throws Exception Si falla la creación del archivo o del esquema.
     */
    public static File crearArchivoTemporal() throws Exception {
        // Cargamos el driver explícitamente en el contexto de tests
        Class.forName("org.sqlite.JDBC");

        // Creamos un archivo único en el directorio temporal del SO
        File dbFile = File.createTempFile("pharmacyfm_test_", ".db");
        dbFile.deleteOnExit(); // Respaldo de limpieza si el test no elimina manualmente

        // Iniciamos el esquema con una conexión que se cierra inmediatamente
        try (Connection conn = DriverManager.getConnection(urlPara(dbFile))) {
            crearEsquema(conn);
        }
        return dbFile;
    }

    /**
     * Construye un Supplier de conexiones apuntando al archivo temporal dado.
     * Cada llamada al Supplier abre una nueva conexión al mismo archivo
     * (el archivo persiste entre conexiones, a diferencia de :memory:).
     *
     * @param dbFile Archivo SQLite temporal creado con crearArchivoTemporal().
     * @return Supplier listo para inyectar en los adaptadores JDBC.
     */
    public static java.util.function.Supplier<Connection> factoriaPara(File dbFile) {
        return () -> {
            try {
                return DriverManager.getConnection(urlPara(dbFile));
            } catch (SQLException e) {
                throw new RuntimeException("Error conectando a BD temporal: " + e.getMessage(), e);
            }
        };
    }

    /**
     * Construye la URL JDBC para un archivo SQLite dado.
     *
     * @param dbFile Archivo SQLite.
     * @return URL en formato "jdbc:sqlite:/ruta/al/archivo.db".
     */
    private static String urlPara(File dbFile) {
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    /**
     * Crea todas las tablas necesarias para los tests de integración.
     * El esquema replica la estructura de producción sin datos iniciales.
     *
     * @param conn Conexión abierta a la BD temporal.
     * @throws SQLException Si falla la creación de alguna tabla.
     */
    private static void crearEsquema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Tabla de usuarios del sistema (admin y pacientes)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS usuarios (" +
                "  id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  email    TEXT    NOT NULL UNIQUE," +
                "  password TEXT    NOT NULL," +
                "  nombre   TEXT," +
                "  telefono TEXT," +
                "  rol      TEXT    NOT NULL" +
                ")"
            );

            // Perfiles de paciente vinculados a usuarios por FK
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS pacientes (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  id_usuario INTEGER NOT NULL REFERENCES usuarios(id)," +
                "  nombre     TEXT," +
                "  telefono   TEXT," +
                "  email      TEXT" +
                ")"
            );

            // Catálogo de fórmulas magistrales
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS formulas (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  nombre      TEXT    NOT NULL," +
                "  descripcion TEXT," +
                "  precio      REAL    NOT NULL DEFAULT 0" +
                ")"
            );

            // Pedidos con soporte para fórmulas de catálogo y personalizadas
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS pedidos (" +
                "  id                    INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  id_paciente           INTEGER NOT NULL REFERENCES pacientes(id)," +
                "  id_formula            INTEGER REFERENCES formulas(id)," +
                "  formula_personalizada TEXT," +
                "  cantidad              INTEGER NOT NULL DEFAULT 1," +
                "  unidad                TEXT," +
                "  observaciones         TEXT," +
                "  fecha                 TEXT," +
                "  estado                TEXT    NOT NULL" +
                ")"
            );
        }
    }
}
