package com.pharmacyfm.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Formula.
 * Verifica la inmutabilidad, el centinela isNew() y el toString
 * que usa JavaFX para renderizar el ComboBox.
 */
@DisplayName("Formula — modelo de dominio inmutable")
class FormulaTest {

    @Test
    @DisplayName("Constructor completo inicializa todos los campos correctamente")
    void constructor_completo_inicializaCampos() {
        Formula f = new Formula(5, "Crema hidratante", "Urea al 10%", 18.50);

        assertEquals(5,                  f.getId());
        assertEquals("Crema hidratante", f.getNombre());
        assertEquals("Urea al 10%",      f.getDescripcion());
        assertEquals(18.50,              f.getPrecio(), 0.001);
    }

    @Test
    @DisplayName("Constructor sin id fija id = 0 e isNew() devuelve true")
    void constructor_sinId_esNueva() {
        Formula nueva = new Formula("Gel antiinflamatorio", "Ibuprofeno 5%", 15.75);

        assertEquals(0, nueva.getId());
        assertTrue(nueva.isNew());
    }

    @Test
    @DisplayName("Una fórmula con id > 0 no es nueva")
    void isNew_conIdPositivo_esFalse() {
        Formula existente = new Formula(3, "Colirio", "Ácido hialurónico", 22.00);

        assertFalse(existente.isNew());
    }

    @Test
    @DisplayName("toString() devuelve el nombre para los ComboBox de JavaFX")
    void toString_devuelveNombre() {
        Formula f = new Formula(1, "Suspensión pediátrica", "Paracetamol 250mg", 9.90);

        assertEquals("Suspensión pediátrica", f.toString());
    }

    @Test
    @DisplayName("toString() con nombre nulo devuelve texto de sustitución")
    void toString_nombreNulo_devuelvePlaceholder() {
        Formula f = new Formula(1, null, "Sin nombre", 0.0);

        assertEquals("(Sin nombre)", f.toString());
    }
}
