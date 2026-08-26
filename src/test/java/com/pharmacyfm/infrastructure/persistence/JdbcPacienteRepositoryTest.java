package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.Paciente;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para JdbcPacienteRepository.
 *
 * Los pacientes requieren un usuario previo (FK 'id_usuario'). @BeforeEach
 * inserta un usuario de apoyo directamente por JDBC antes de cada test.
 *
 * Cada test usa su propio archivo SQLite temporal para garantizar aislamiento.
 */
@DisplayName("JdbcPacienteRepository — integración con SQLite en archivo temporal")
class JdbcPacienteRepositoryTest {

    private File dbFile;
    private JdbcPacienteRepository repo;

    /** ID del usuario de apoyo reutilizado como FK en los tests. */
    private int idUsuarioPrueba;

    @BeforeEach
    void setUp() throws Exception {
        dbFile = InMemoryDb.crearArchivoTemporal();
        repo   = new JdbcPacienteRepository(InMemoryDb.factoriaPara(dbFile));

        idUsuarioPrueba = insertarUsuarioPrueba();
    }

    @AfterEach
    void tearDown() {
        if (dbFile != null) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("findByUserId devuelve null cuando el usuario no tiene perfil de paciente")
    void findByUserId_sinPerfil_devuelveNull() {
        assertNull(repo.findByUserId(idUsuarioPrueba));
    }

    @Test
    @DisplayName("findAll devuelve lista vacía cuando no hay pacientes")
    void findAll_sinDatos_devuelveListaVacia() {
        List<Paciente> resultado = repo.findAll();

        assertNotNull(resultado, "findAll nunca debe devolver null");
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("insert persiste el perfil de paciente vinculado al usuario")
    void insert_pacienteNuevo_devuelveTrue() throws Exception {
        boolean ok = insertarPacientePrueba("María García", "612345678", "maria@test.com");

        assertTrue(ok, "El insert debe devolver true");
    }

    @Test
    @DisplayName("findByUserId recupera el perfil insertado con sus datos correctos")
    void findByUserId_trasInsert_devuelvePacienteCorrecto() throws Exception {
        insertarPacientePrueba("Carlos Martín", "698765432", "carlos@test.com");

        Paciente paciente = repo.findByUserId(idUsuarioPrueba);

        assertNotNull(paciente);
        assertEquals(idUsuarioPrueba, paciente.idUsuario());
        assertEquals("Carlos Martín", paciente.nombre());
        assertEquals("698765432", paciente.telefono());
        assertEquals("carlos@test.com", paciente.email());
    }

    @Test
    @DisplayName("findAll devuelve todos los pacientes ordenados por nombre")
    void findAll_variosRegistros_devuelveTodosOrdenados() throws Exception {
        insertarPacientePrueba("Zoe Fernández", "600000001", "zoe@test.com");
        int idUsuario2 = insertarUsuarioPrueba();
        insertarPacientePrueba(idUsuario2, "Ana López", "600000002", "ana@test.com");

        List<Paciente> lista = repo.findAll();

        assertEquals(2, lista.size());
        assertEquals("Ana López", lista.get(0).nombre(), "Debe venir ordenado alfabéticamente");
    }

    @Test
    @DisplayName("update modifica los datos de contacto de un paciente existente")
    void update_pacienteExistente_actualizaDatos() throws Exception {
        insertarPacientePrueba("Nombre Original", "600000000", "original@test.com");
        Paciente existente = repo.findByUserId(idUsuarioPrueba);

        Paciente actualizado = existente.withNombre("Nombre Actualizado").withTelefono("611111111");
        boolean ok = repo.update(actualizado);

        assertTrue(ok, "La actualización debe reportar éxito");

        Paciente recuperado = repo.findByUserId(idUsuarioPrueba);
        assertEquals("Nombre Actualizado", recuperado.nombre());
        assertEquals("611111111", recuperado.telefono());
    }

    // =========================================================
    // UTILIDADES DE APOYO PARA TESTS
    // =========================================================

    /**
     * Inserta un usuario de apoyo directamente por JDBC (fuera del repositorio
     * bajo prueba), necesario porque 'pacientes' tiene FK NOT NULL hacia 'usuarios'.
     *
     * @return ID autogenerado del usuario insertado.
     */
    private int insertarUsuarioPrueba() throws Exception {
        try (Connection conn = InMemoryDb.factoriaPara(dbFile).get();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO usuarios (email, password, nombre, telefono, rol) " +
                     "VALUES (?, 'hash', 'Usuario Test', '600000000', 'paciente')",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, "usuario" + System.nanoTime() + "@test.com");
            stmt.executeUpdate();

            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Inserta un paciente vinculado a idUsuarioPrueba, usando el repo bajo prueba. */
    private boolean insertarPacientePrueba(String nombre, String telefono, String email) throws Exception {
        return insertarPacientePrueba(idUsuarioPrueba, nombre, telefono, email);
    }

    /** Inserta un paciente vinculado a un usuario concreto, usando el repo bajo prueba. */
    private boolean insertarPacientePrueba(int idUsuario, String nombre, String telefono, String email) throws Exception {
        try (Connection conn = InMemoryDb.factoriaPara(dbFile).get()) {
            return repo.insert(idUsuario, nombre, telefono, email, conn);
        }
    }
}
