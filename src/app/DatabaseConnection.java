package app;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase responsable de gestionar la conexión a la base de datos SQLite.
 * Sigue el patrón Singleton para garantizar una única fuente de conexión.
 */
public class DatabaseConnection {

    private static final String DB_FILE = initDbPath();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    // Carga del driver SQLite
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Driver SQLite JDBC cargado correctamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("No se pudo cargar el driver de SQLite JDBC:");
            e.printStackTrace();
        }
    }

    /**
     * Construye la ruta al archivo de base de datos.
     */
    private static String initDbPath() {
        String projectDir = System.getProperty("user.dir");
        File dbDir = new File(projectDir, ".." + File.separator + "db");

        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (!created) {
                System.err.println("No se pudo crear la carpeta de base de datos: " + dbDir.getAbsolutePath());
            } else {
                System.out.println("Carpeta de base de datos creada en: " + dbDir.getAbsolutePath());
            }
        } else {
            System.out.println("Carpeta de base de datos encontrada en: " + dbDir.getAbsolutePath());
        }

        File dbFile = new File(dbDir, "farmacia.db");
        System.out.println("Usando base de datos en: " + dbFile.getAbsolutePath());
        return dbFile.getPath();
    }

    /**
     * Devuelve una nueva conexión a la base de datos.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}