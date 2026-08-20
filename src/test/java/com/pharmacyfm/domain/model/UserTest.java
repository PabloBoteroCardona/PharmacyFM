package com.pharmacyfm.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el record User.
 * Verifica que el record genera correctamente sus accesores
 * y que dos instancias con los mismos datos son iguales.
 */
@DisplayName("User — record inmutable de sesión")
class UserTest {

    @Test
    @DisplayName("Los accesores del record devuelven los valores del constructor")
    void accesores_devuelvenValoresConstructor() {
        User user = new User(1, "admin@test.com", "Administrador", "600000000", Role.ADMIN);

        assertEquals(1,               user.id());
        assertEquals("admin@test.com", user.email());
        assertEquals("Administrador", user.nombre());
        assertEquals("600000000",     user.telefono());
        assertEquals(Role.ADMIN,      user.role());
    }

    @Test
    @DisplayName("Dos records con los mismos datos son iguales (equals por valor)")
    void equals_dosInstanciasIguales() {
        User u1 = new User(2, "p@test.com", "Pablo", "611000000", Role.PACIENTE);
        User u2 = new User(2, "p@test.com", "Pablo", "611000000", Role.PACIENTE);

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    @DisplayName("Un admin no es igual a un paciente")
    void equals_rolesDistintosNoSonIguales() {
        User admin    = new User(1, "a@test.com", "Admin", "", Role.ADMIN);
        User paciente = new User(1, "a@test.com", "Admin", "", Role.PACIENTE);

        assertNotEquals(admin, paciente);
    }
}
