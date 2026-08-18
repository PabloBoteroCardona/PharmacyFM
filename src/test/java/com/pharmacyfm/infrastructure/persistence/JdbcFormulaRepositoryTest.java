package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.Formula;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para JdbcFormulaRepository.
 *
 * Cada test trabaja sobre un archivo SQLite temporal único creado en @BeforeEach
 * y eliminado en @AfterEach. Esto garantiza:
 *   - Aislamiento total: el test A no ve datos del test B.
 *   - Realismo: se ejercita el mismo driver SQLite que en producción.
 *   - Rapidez: los archivos temporales son pequeños y se crean en milisegundos.
 */
@DisplayName("JdbcFormulaRepository — integración con SQLite en archivo temporal")
class JdbcFormulaRepositoryTest {

    /** Archivo SQLite temporal exclusivo para este test. */
    private File dbFile;

    /** Repositorio bajo prueba, configurado para apuntar al archivo temporal. */
    private JdbcFormulaRepository repo;

    /**
     * Antes de cada test: crea el archivo temporal con el esquema e inyecta
     * la factoría de conexiones al repositorio.
     */
    @BeforeEach
    void setUp() throws Exception {
        dbFile = InMemoryDb.crearArchivoTemporal();
        repo   = new JdbcFormulaRepository(InMemoryDb.factoriaPara(dbFile));
    }

    /**
     * Después de cada test: eliminamos el archivo temporal.
     * deleteOnExit() ya es un respaldo, pero lo hacemos explícitamente para limpiar inmediatamente.
     */
    @AfterEach
    void tearDown() {
        if (dbFile != null) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("findAll devuelve lista vacía cuando no hay fórmulas")
    void findAll_sinDatos_devuelveListaVacia() {
        List<Formula> resultado = repo.findAll();

        assertNotNull(resultado, "findAll nunca debe devolver null");
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía si no hay fórmulas");
    }

    @Test
    @DisplayName("insert persiste una fórmula y devuelve un ID positivo")
    void insert_formulaNueva_devuelveIdPositivo() {
        Formula nueva = new Formula("Crema hidratante", "Base acuosa con urea al 10%", 12.50);

        int id = repo.insert(nueva);

        assertTrue(id > 0, "El ID autogenerado debe ser mayor que 0");
    }

    @Test
    @DisplayName("findAll recupera la fórmula insertada con sus datos correctos")
    void findAll_trasInsert_devuelveLaFormula() {
        repo.insert(new Formula("Gel antiséptico", "Alcohol 70% con glicerina", 8.00));

        List<Formula> lista = repo.findAll();

        assertEquals(1, lista.size(), "Debe haber exactamente una fórmula");
        Formula recuperada = lista.get(0);
        assertEquals("Gel antiséptico", recuperada.getNombre());
        assertEquals("Alcohol 70% con glicerina", recuperada.getDescripcion());
        assertEquals(8.00, recuperada.getPrecio(), 0.001);
    }

    @Test
    @DisplayName("update modifica los datos de una fórmula existente")
    void update_formulaExistente_actualizaDatos() {
        // Insertamos la fórmula original y obtenemos su ID
        int id = repo.insert(new Formula("Pomada base", "Petrolato simple", 5.00));

        // Actualizamos usando el ID devuelto por insert
        Formula actualizada = new Formula(id, "Pomada base enriquecida", "Petrolato + lanolina", 7.50);
        boolean resultado = repo.update(actualizada);

        assertTrue(resultado, "La actualización debe reportar éxito");

        // Verificamos que los nuevos datos se almacenaron
        List<Formula> lista = repo.findAll();
        assertEquals(1, lista.size());
        assertEquals("Pomada base enriquecida", lista.get(0).getNombre());
        assertEquals(7.50, lista.get(0).getPrecio(), 0.001);
    }

    @Test
    @DisplayName("delete elimina la fórmula y findAll devuelve lista vacía")
    void delete_formulaExistente_eliminaCorrectamente() {
        int id = repo.insert(new Formula("Solución salina", "NaCl 0.9%", 3.00));

        boolean resultado = repo.delete(id);

        assertTrue(resultado, "La eliminación debe reportar éxito");
        assertTrue(repo.findAll().isEmpty(), "La lista debe quedar vacía tras eliminar");
    }

    @Test
    @DisplayName("delete con ID inexistente devuelve false")
    void delete_idInexistente_devuelveFalse() {
        boolean resultado = repo.delete(9999);

        assertFalse(resultado, "Eliminar un ID que no existe debe devolver false");
    }

    @Test
    @DisplayName("findAll devuelve el número correcto de fórmulas insertadas")
    void findAll_variosRegistros_devuelveTodos() {
        repo.insert(new Formula("Zinc óxido", "Polvo antiséptico", 4.00));
        repo.insert(new Formula("Acido salicilico", "Queratolítico al 5%", 6.00));
        repo.insert(new Formula("Mentol", "Refrescante tópico", 9.00));

        List<Formula> lista = repo.findAll();

        assertEquals(3, lista.size(), "Deben recuperarse las 3 fórmulas insertadas");
    }
}
