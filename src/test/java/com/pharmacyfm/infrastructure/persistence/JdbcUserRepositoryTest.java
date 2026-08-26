package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.Role;
import com.pharmacyfm.domain.model.User;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para JdbcUserRepository.
 *
 * Cada test trabaja sobre un archivo SQLite temporal único creado en @BeforeEach
 * y eliminado en @AfterEach, igual que el resto de repositorios JDBC.
 *
 * insert() recibe la Connection externamente (participa en la transacción de
 * registro de AuthService), así que los tests que insertan abren su propia
 * conexión temporal para pasársela al método.
 */
@DisplayName("JdbcUserRepository — integración con SQLite en archivo temporal")
class JdbcUserRepositoryTest {

    private File dbFile;
    private JdbcUserRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        dbFile = InMemoryDb.crearArchivoTemporal();
        repo   = new JdbcUserRepository(InMemoryDb.factoriaPara(dbFile));
    }

    @AfterEach
    void tearDown() {
        if (dbFile != null) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("findByEmail devuelve Optional vacío cuando el email no existe")
    void findByEmail_sinDatos_devuelveOptionalVacio() {
        Optional<User> resultado = repo.findByEmail("nadie@pharmacyfm.com");

        assertTrue(resultado.isEmpty(), "Debe devolver Optional.empty() si el email no existe");
    }

    @Test
    @DisplayName("existsByEmail devuelve false cuando el email no está registrado")
    void existsByEmail_sinDatos_devuelveFalse() {
        assertFalse(repo.existsByEmail("nadie@pharmacyfm.com"));
    }

    @Test
    @DisplayName("insert persiste un usuario y devuelve un ID positivo")
    void insert_usuarioNuevo_devuelveIdPositivo() throws Exception {
        int id = insertarUsuarioPrueba("nuevo@pharmacyfm.com", "hash123", "paciente");

        assertTrue(id > 0, "El ID autogenerado debe ser mayor que 0");
    }

    @Test
    @DisplayName("findByEmail recupera el usuario insertado con el rol correcto")
    void findByEmail_trasInsert_devuelveUsuarioConRolCorrecto() throws Exception {
        insertarUsuarioPrueba("admin.test@pharmacyfm.com", "hashAdmin", "admin");

        Optional<User> resultado = repo.findByEmail("admin.test@pharmacyfm.com");

        assertTrue(resultado.isPresent(), "El usuario insertado debe encontrarse");
        assertEquals("admin.test@pharmacyfm.com", resultado.get().email());
        assertEquals(Role.ADMIN, resultado.get().role());
    }

    @Test
    @DisplayName("getPasswordHashByEmail devuelve el hash almacenado tras el insert")
    void getPasswordHashByEmail_trasInsert_devuelveHash() throws Exception {
        insertarUsuarioPrueba("paciente.test@pharmacyfm.com", "hashPaciente", "paciente");

        Optional<String> hash = repo.getPasswordHashByEmail("paciente.test@pharmacyfm.com");

        assertTrue(hash.isPresent());
        assertEquals("hashPaciente", hash.get());
    }

    @Test
    @DisplayName("existsByEmail devuelve true tras insertar el usuario")
    void existsByEmail_trasInsert_devuelveTrue() throws Exception {
        insertarUsuarioPrueba("existe@pharmacyfm.com", "hash", "paciente");

        assertTrue(repo.existsByEmail("existe@pharmacyfm.com"));
    }

    @Test
    @DisplayName("updatePassword reemplaza el hash almacenado para un email existente")
    void updatePassword_usuarioExistente_actualizaHash() throws Exception {
        insertarUsuarioPrueba("cambia.pass@pharmacyfm.com", "hashViejo", "paciente");

        boolean ok = repo.updatePassword("cambia.pass@pharmacyfm.com", "hashNuevo");

        assertTrue(ok, "La actualización debe reportar éxito");
        assertEquals("hashNuevo", repo.getPasswordHashByEmail("cambia.pass@pharmacyfm.com").orElseThrow());
    }

    @Test
    @DisplayName("updatePassword con email inexistente devuelve false")
    void updatePassword_emailInexistente_devuelveFalse() {
        assertFalse(repo.updatePassword("nadie@pharmacyfm.com", "hash"));
    }

    // =========================================================
    // UTILIDADES DE APOYO PARA TESTS
    // =========================================================

    /**
     * Inserta un usuario de prueba usando el propio repositorio bajo prueba,
     * abriendo la conexión externa que insert() requiere para participar
     * en una transacción (igual que hace AuthService en producción).
     *
     * @return ID autogenerado del usuario insertado.
     */
    private int insertarUsuarioPrueba(String email, String passwordHash, String rol) throws Exception {
        try (Connection conn = InMemoryDb.factoriaPara(dbFile).get()) {
            return repo.insert(email, passwordHash, "Usuario Test", "600000000", rol, conn);
        }
    }
}
