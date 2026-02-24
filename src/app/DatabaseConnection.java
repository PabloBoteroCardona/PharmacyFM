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

    // Definición de la ruta del archivo y la URL de conexión JDBC
    private static final String DB_FILE = initDbPath();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    // Bloque estático para asegurar que el driver se cargue al iniciar la clase
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
     * Gestiona la ubicación del archivo .db de forma dinámica.
     * Crea el directorio 'db' en la raíz del proyecto si no existe para asegurar la persistencia.
     */
    private static String initDbPath() {
        // Obtenemos la ruta del directorio de trabajo del usuario
        String projectDir = System.getProperty("user.dir");
        
        // Definimos una carpeta 'db' fuera del src para separar datos de código fuente
        File dbDir = new File(projectDir, ".." + File.separator + "db");

        // Verificación y creación del directorio en caso de ser necesario
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

        // Retornamos la ruta completa hacia el archivo de la base de datos
        File dbFile = new File(dbDir, "farmacia.db");
        System.out.println("Usando base de datos en: " + dbFile.getAbsolutePath());
        return dbFile.getPath();
    }

    /**
     * Proporciona una instancia de conexión activa mediante DriverManager.
     * @return Connection objeto de conexión SQL.
     * @throws SQLException si ocurre un error al establecer el enlace con el archivo .db.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}