package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.Formula;
import com.pharmacyfm.domain.port.FormulaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Adaptador JDBC para el puerto FormulaRepository.
 *
 * Implementa el contrato definido en domain.port.FormulaRepository utilizando
 * SQLite como motor de base de datos. Es el único punto del sistema que conoce
 * el nombre exacto de la tabla 'formulas' y sus columnas.
 *
 * Todas las consultas usan PreparedStatement para prevenir inyección SQL.
 * Ninguna colección retornada es null: en ausencia de datos se devuelve lista vacía.
 *
 * El proveedor de conexiones es inyectable para facilitar los tests de integración
 * con bases de datos en memoria (SQLite :memory:) sin tocar el archivo de producción.
 */
public class JdbcFormulaRepository implements FormulaRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcFormulaRepository.class);

    /**
     * Factoría de conexiones. En producción apunta a SqliteConnectionProvider;
     * en tests se inyecta una que devuelve la conexión en memoria compartida.
     */
    private final Supplier<Connection> connectionFactory;

    /** Constructor de producción: usa la BD real de SqliteConnectionProvider. */
    public JdbcFormulaRepository() {
        this(() -> {
            try { return SqliteConnectionProvider.getConnection(); }
            catch (SQLException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Constructor para tests: permite inyectar cualquier proveedor de conexión.
     *
     * @param connectionFactory Proveedor de conexiones alternativo (p.ej. BD en memoria).
     */
    public JdbcFormulaRepository(Supplier<Connection> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /** Obtiene una conexión del proveedor configurado. */
    private Connection getConn() throws SQLException {
        try {
            return connectionFactory.get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException) throw (SQLException) e.getCause();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     * Recupera el catálogo completo de fórmulas ordenado por nombre ascendente.
     */
    @Override
    public List<Formula> findAll() {
        List<Formula> lista = new ArrayList<>();
        String sql = "SELECT * FROM formulas ORDER BY nombre ASC";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Mapeamos cada fila al modelo de dominio inmutable
                lista.add(new Formula(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio")
                ));
            }

        } catch (SQLException e) {
            log.error("[JdbcFormulaRepository] Error en findAll: {}", e.getMessage());
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     * Inserta una nueva fórmula y devuelve el ID autogenerado.
     */
    @Override
    public int insert(Formula f) {
        String sql = "INSERT INTO formulas (nombre, descripcion, precio) VALUES (?, ?, ?)";

        // RETURN_GENERATED_KEYS permite recuperar el ID asignado por SQLite
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, f.getNombre());
            stmt.setString(2, f.getDescripcion());
            stmt.setDouble(3, f.getPrecio());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            log.error("[JdbcFormulaRepository] Error en insert: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     * Actualiza los datos de una fórmula existente por su ID.
     */
    @Override
    public boolean update(Formula f) {
        String sql = "UPDATE formulas SET nombre = ?, descripcion = ?, precio = ? WHERE id = ?";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, f.getNombre());
            stmt.setString(2, f.getDescripcion());
            stmt.setDouble(3, f.getPrecio());
            stmt.setInt(4, f.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("[JdbcFormulaRepository] Error en update: {}", e.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * Elimina permanentemente una fórmula por su ID.
     */
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM formulas WHERE id = ?";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("[JdbcFormulaRepository] Error en delete: {}", e.getMessage());
            return false;
        }
    }
}
