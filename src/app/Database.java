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

    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            // Tabla usuarios
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

            // Tabla pacientes
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

            // Tabla formulas
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS formulas (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " nombre TEXT NOT NULL," +
                " descripcion TEXT," +
                " precio REAL" +
                ");"
            );

            // Tabla pedidos — incluye columna 'unidad'
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

            // Admin por defecto — solo se inserta si no existe
            String adminHash = BCrypt.hashpw("admin", BCrypt.gensalt());
            stmt.execute(
                "INSERT OR IGNORE INTO usuarios (id, email, password, nombre, telefono, rol) " +
                "VALUES (1, 'admin', '" + adminHash + "', 'Administrador', '', 'admin');"
            );

            System.out.println("Base de datos inicializada correctamente.");
        }
    }
}