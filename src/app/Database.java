package app;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase responsable de inicializar la base de datos.
 * Crea las tablas si no existen e inserta el usuario administrador por defecto.
 *
 * La gestión de conexiones está en DatabaseConnection.
 * Las operaciones CRUD están en los repositorios (app.repository).
 */
public class Database {

    /**
     * Configura la estructura inicial de la base de datos SQLite.
     * Crea las tablas necesarias y asegura la existencia de un usuario administrador.
     */
    public static void initializeDatabase() throws SQLException {
        // Uso de try-with-resources para asegurar el cierre automático de la conexión y el statement
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Activación del soporte para claves foráneas en SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // ---- Estructura de la tabla Usuarios ----
            // Almacena credenciales y datos básicos para el control de acceso y roles
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS usuarios (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " email TEXT NOT NULL UNIQUE," +
                " password TEXT NOT NULL," +
                " nombre TEXT NOT NULL," +
                " telefono TEXT," +
                " rol TEXT NOT NULL" +
                ");"
            );

            // ---- Estructura de la tabla Pacientes ----
            // Relacionada con un usuario para permitir el acceso al perfil personal
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS pacientes (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " id_usuario INTEGER NOT NULL," +
                " nombre TEXT NOT NULL," +
                " telefono TEXT," +
                " email TEXT," +
                " FOREIGN KEY (id_usuario) REFERENCES usuarios(id)" +
                ");"
            );

            // ---- Estructura de la tabla Formulas ----
            // Catálogo de fórmulas magistrales disponibles con su descripción y precio
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS formulas (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " nombre TEXT NOT NULL," +
                " descripción TEXT," +
                " precio REAL" +
                ");"
            );

            // ---- Estructura de la tabla Pedidos ----
            // Registra las solicitudes, vinculando pacientes con fórmulas y gestionando el estado del pedido
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS pedidos (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " id_paciente INTEGER NOT NULL," +
                " id_formula INTEGER," +
                " formula_personalizada TEXT," +
                " cantidad INTEGER NOT NULL," +
                " unidad TEXT," +
                " observaciones TEXT," +
                " fecha TEXT NOT NULL," +
                " estado TEXT NOT NULL," +
                " FOREIGN KEY (id_paciente) REFERENCES pacientes(id)," +
                " FOREIGN KEY (id_formula) REFERENCES formulas(id)" +
                ");"
            );

            // ---- Inserción del Administrador por defecto ----
            // Se utiliza BCrypt para el hashing de la contraseña por seguridad
            String adminHash = BCrypt.hashpw("admin", BCrypt.gensalt());
            
            // Se usa INSERT OR IGNORE para evitar duplicados si la base de datos ya está inicializada
            stmt.execute(
                "INSERT OR IGNORE INTO usuarios (id, email, password, nombre, telefono, rol) " +
                "VALUES (1, 'admin', '" + adminHash + "', 'Administrador', '', 'admin');"
            );

            System.out.println("Base de datos inicializada correctamente.");
        }
    }
}