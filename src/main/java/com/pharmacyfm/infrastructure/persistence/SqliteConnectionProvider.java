package com.pharmacyfm.infrastructure.persistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Proveedor de conexiones a la base de datos SQLite de PharmacyFM.
 *
 * Responsabilidades:
 *   - Localizar o crear la carpeta de datos de la aplicación.
 *   - Construir la URL JDBC correcta para el archivo farmacia.db.
 *   - Exponer una factoría de conexiones (getConnection).
 *
 * El archivo de base de datos se almacena en:
 *   {user.home}/.pharmacyfm/farmacia.db
 *
 * Esta ruta es estable independientemente del directorio de trabajo actual
 * (user.dir cambia al lanzar la app desde distintos lugares; user.home no).
 *
 * No implementa Singleton: SQLite en modo WAL soporta múltiples conexiones
 * concurrentes sin problema. Cada llamada a getConnection() entrega una
 * conexión nueva que el llamante es responsable de cerrar.
 */
public class SqliteConnectionProvider {

    /** URL JDBC calculada una sola vez al cargar la clase. */
    private static final String DB_URL = buildDbUrl();

    static {
        // Cargamos explícitamente el driver para garantizar su disponibilidad
        // antes de la primera llamada a getConnection()
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "No se encontró el driver SQLite JDBC en el classpath: " + e.getMessage());
        }
    }

    /**
     * Proporciona una conexión activa a la base de datos.
     * El llamante DEBE cerrarla (idealmente con try-with-resources).
     *
     * @return Conexión JDBC abierta.
     * @throws SQLException Si no se puede establecer la conexión.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Devuelve la URL JDBC que se está usando, para trazabilidad en logs.
     *
     * @return Cadena "jdbc:sqlite:/ruta/al/farmacia.db".
     */
    public static String getDbUrl() {
        return DB_URL;
    }

    /**
     * Construye la URL JDBC, creando el directorio de datos si es necesario.
     *
     * La carpeta elegida ({user.home}/.pharmacyfm/) sigue la convención de
     * directorios de configuración de usuario en Windows, macOS y Linux.
     *
     * @return URL JDBC lista para usar con DriverManager.
     */
    private static String buildDbUrl() {
        // user.home apunta al directorio personal del usuario del SO:
        // Windows: C:\Users\<nombre>   macOS/Linux: /home/<nombre>
        File dirDatos = new File(System.getProperty("user.home"), ".pharmacyfm");

        if (!dirDatos.exists()) {
            boolean creado = dirDatos.mkdirs();
            if (!creado) {
                // Si no se puede crear el directorio, usamos el directorio de trabajo
                // como fallback para no bloquear el arranque de la aplicación
                System.err.println("[SqliteConnectionProvider] No se pudo crear: "
                        + dirDatos.getAbsolutePath() + " — usando directorio de trabajo como fallback.");
                dirDatos = new File(System.getProperty("user.dir"));
            }
        }

        File archivoDb = new File(dirDatos, "farmacia.db");
        System.out.println("[SqliteConnectionProvider] Base de datos: " + archivoDb.getAbsolutePath());
        return "jdbc:sqlite:" + archivoDb.getAbsolutePath();
    }
}
