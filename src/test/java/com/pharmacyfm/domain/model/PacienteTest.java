package com.pharmacyfm.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el record Paciente.
 *
 * Verifica la inmutabilidad del modelo y los métodos with* que devuelven
 * copias parcialmente actualizadas manteniendo los demás campos intactos.
 */
@DisplayName("Paciente — modelo de dominio inmutable (record)")
class PacienteTest {

    private static final Paciente BASE =
            new Paciente(1, 10, "Ana García", "600111222", "ana@test.com");

    @Test
    @DisplayName("Constructor inicializa todos los campos correctamente")
    void constructor_inicializaCamposCorrectos() {
        assertEquals(1,              BASE.id());
        assertEquals(10,             BASE.idUsuario());
        assertEquals("Ana García",   BASE.nombre());
        assertEquals("600111222",    BASE.telefono());
        assertEquals("ana@test.com", BASE.email());
    }

    @Test
    @DisplayName("withNombre devuelve una nueva instancia con el nombre actualizado")
    void withNombre_devuelveNuevaPacienteConNombreCambiado() {
        Paciente actualizado = BASE.withNombre("Ana López");

        // El campo nombre debe cambiar
        assertEquals("Ana López", actualizado.nombre());

        // Los demás campos deben permanecer iguales
        assertEquals(BASE.id(),       actualizado.id());
        assertEquals(BASE.idUsuario(), actualizado.idUsuario());
        assertEquals(BASE.telefono(),  actualizado.telefono());
        assertEquals(BASE.email(),     actualizado.email());

        // El objeto original no debe haber cambiado (inmutabilidad)
        assertEquals("Ana García", BASE.nombre());
    }

    @Test
    @DisplayName("withTelefono devuelve una nueva instancia con el teléfono actualizado")
    void withTelefono_devuelveNuevaPacienteConTelefonoCambiado() {
        Paciente actualizado = BASE.withTelefono("611999888");

        assertEquals("611999888", actualizado.telefono());
        assertEquals(BASE.nombre(), actualizado.nombre());
        assertEquals(BASE.email(),  actualizado.email());
        assertEquals("600111222", BASE.telefono());
    }

    @Test
    @DisplayName("withEmail devuelve una nueva instancia con el email actualizado")
    void withEmail_devuelveNuevaPacienteConEmailCambiado() {
        Paciente actualizado = BASE.withEmail("nuevo@email.com");

        assertEquals("nuevo@email.com", actualizado.email());
        assertEquals(BASE.nombre(),    actualizado.nombre());
        assertEquals(BASE.telefono(),  actualizado.telefono());
        assertEquals("ana@test.com", BASE.email());
    }
}
