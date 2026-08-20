package app;

import com.pharmacyfm.infrastructure.persistence.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fachada de compatibilidad hacia SqliteConnectionProvider.
 *
 * Antes de F2 esta clase gestionaba directamente la conexión SQLite.
 * Ahora delega toda la lógica a SqliteConnectionProvider (infrastructure),
 * que corrige la ruta frágil basada en user.dir y la reemplaza por user.home.
 *
 * Se mantiene en el paquete 'app' para no romper ninguna referencia existente
 * durante la transición. Se eliminará en F4 cuando la UI se refactorice
 * y ya no haya llamadas directas desde el paquete app.
 *
 * @deprecated Usar SqliteConnectionProvider directamente desde infrastructure.
 */
@Deprecated
public class DatabaseConnection {

    /**
     * Proporciona una conexión activa a la base de datos SQLite.
     * Delega completamente en SqliteConnectionProvider.
     *
     * @return Conexión JDBC abierta. El llamante debe cerrarla.
     * @throws SQLException Si no se puede establecer la conexión.
     */
    public static Connection getConnection() throws SQLException {
        return SqliteConnectionProvider.getConnection();
    }
}
