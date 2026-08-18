package com.pharmacyfm.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el enum Role.
 * Verifica que el método factory Role.from() convierte correctamente
 * los Strings de la base de datos y lanza excepciones ante valores inválidos.
 */
@DisplayName("Role — conversión desde String de base de datos")
class RoleTest {

    @Test
    @DisplayName("'admin' (minúsculas) se convierte a Role.ADMIN")
    void from_admin_minusculas() {
        assertEquals(Role.ADMIN, Role.from("admin"));
    }

    @Test
    @DisplayName("'ADMIN' (mayúsculas) se convierte a Role.ADMIN")
    void from_admin_mayusculas() {
        assertEquals(Role.ADMIN, Role.from("ADMIN"));
    }

    @Test
    @DisplayName("'paciente' se convierte a Role.PACIENTE")
    void from_paciente() {
        assertEquals(Role.PACIENTE, Role.from("paciente"));
    }

    @Test
    @DisplayName("Valor desconocido lanza IllegalArgumentException")
    void from_valorDesconocido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> Role.from("gestor"));
    }

    @Test
    @DisplayName("Valor nulo lanza IllegalArgumentException")
    void from_nulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> Role.from(null));
    }
}
