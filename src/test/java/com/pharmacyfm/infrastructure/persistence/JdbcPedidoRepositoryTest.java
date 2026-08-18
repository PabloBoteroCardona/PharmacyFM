package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.EstadoPedido;
import com.pharmacyfm.domain.model.Pedido;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para JdbcPedidoRepository.
 *
 * Los pedidos requieren registros previos en 'usuarios' y 'pacientes' (claves foráneas).
 * @BeforeEach inserta un usuario y un paciente de prueba antes de cada test.
 *
 * Cada test usa su propio archivo SQLite temporal para garantizar aislamiento.
 */
@DisplayName("JdbcPedidoRepository — integración con SQLite en archivo temporal")
class JdbcPedidoRepositoryTest {

    private File dbFile;
    private JdbcPedidoRepository repo;

    /** ID del paciente de prueba reutilizado en todos los tests del ciclo. */
    private int idPacientePrueba;

    @BeforeEach
    void setUp() throws Exception {
        dbFile = InMemoryDb.crearArchivoTemporal();
        repo   = new JdbcPedidoRepository(InMemoryDb.factoriaPara(dbFile));

        // Insertamos el usuario y paciente de apoyo que actúan como FK en 'pedidos'
        idPacientePrueba = insertarPacientePrueba();
    }

    @AfterEach
    void tearDown() {
        if (dbFile != null) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("findByPacienteId devuelve lista vacía si el paciente no tiene pedidos")
    void findByPacienteId_sinPedidos_devuelveListaVacia() {
        List<Pedido> resultado = repo.findByPacienteId(idPacientePrueba);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("insert con fórmula personalizada crea un pedido en estado PENDIENTE")
    void insert_formulaPersonalizada_creaEnEstadoPendiente() {
        boolean ok = repo.insert(idPacientePrueba, null, "Crema personalizada",
                2, "Gramos", "Sin fragancias");

        assertTrue(ok, "El insert debe devolver true");

        List<Pedido> lista = repo.findByPacienteId(idPacientePrueba);
        assertEquals(1, lista.size(), "Debe haberse creado 1 pedido");
        assertEquals(EstadoPedido.PENDIENTE, lista.get(0).getEstadoPedido(),
                "El estado inicial siempre es PENDIENTE");
    }

    @Test
    @DisplayName("updateEstado cambia el estado de un pedido existente")
    void updateEstado_pedidoExistente_actualizaEstado() {
        repo.insert(idPacientePrueba, null, "Solución oral", 1, "Mililitros", "");

        List<Pedido> lista = repo.findByPacienteId(idPacientePrueba);
        int idPedido = lista.get(0).getId();

        // Avanzamos el estado en el flujo de trabajo
        boolean ok = repo.updateEstado(idPedido, EstadoPedido.EN_PREPARACION.getLabel());

        assertTrue(ok, "La actualización de estado debe devolver true");

        List<Pedido> tras = repo.findByPacienteId(idPacientePrueba);
        assertEquals(EstadoPedido.EN_PREPARACION, tras.get(0).getEstadoPedido());
    }

    @Test
    @DisplayName("findAll devuelve todos los pedidos del sistema")
    void findAll_conPedidos_devuelveTodos() {
        repo.insert(idPacientePrueba, null, "Fórmula A", 1, "Cápsulas", "");
        repo.insert(idPacientePrueba, null, "Fórmula B", 3, "Gramos", "Urgente");

        List<Pedido> todos = repo.findAll();

        assertEquals(2, todos.size(), "Deben recuperarse los 2 pedidos insertados");
    }

    @Test
    @DisplayName("el nombre de la fórmula personalizada se recupera correctamente")
    void insert_nombreFormulaPersonalizada_seRecuperaCorrectamente() {
        repo.insert(idPacientePrueba, null, "Melatonina 1mg", 30, "Cápsulas",
                "Tomar antes de dormir");

        List<Pedido> lista = repo.findByPacienteId(idPacientePrueba);

        assertEquals("Melatonina 1mg", lista.get(0).getNombreFormula(),
                "El nombre personalizado debe recuperarse íntegro");
    }

    // =========================================================
    // UTILIDADES DE APOYO PARA TESTS
    // =========================================================

    /**
     * Inserta un usuario y un paciente de prueba en el archivo temporal.
     * Necesario porque la tabla 'pedidos' tiene FK hacia 'pacientes'.
     *
     * @return ID del paciente insertado.
     * @throws Exception Si falla alguna inserción JDBC.
     */
    private int insertarPacientePrueba() throws Exception {
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try (Connection conn = DriverManager.getConnection(url)) {
            // 1. Insertamos el usuario
            int idUsuario;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO usuarios (email, password, nombre, telefono, rol) " +
                    "VALUES ('test@test.com', 'hash', 'Paciente Test', '600000000', 'paciente')",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.executeUpdate();
                try (var rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    idUsuario = rs.getInt(1);
                }
            }

            // 2. Insertamos el perfil de paciente vinculado al usuario
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO pacientes (id_usuario, nombre, telefono, email) " +
                    "VALUES (?, 'Paciente Test', '600000000', 'test@test.com')",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, idUsuario);
                stmt.executeUpdate();
                try (var rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1); // ID del paciente
                }
            }
        }
    }
}
