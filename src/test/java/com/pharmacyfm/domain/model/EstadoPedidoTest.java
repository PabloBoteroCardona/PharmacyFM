package com.pharmacyfm.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el enum EstadoPedido.
 * Verifica la conversión desde los textos almacenados en la BD,
 * los labels legibles y el comportamiento ante valores inválidos.
 */
@DisplayName("EstadoPedido — conversión y labels")
class EstadoPedidoTest {

    @Test
    @DisplayName("'Pendiente' se convierte a EstadoPedido.PENDIENTE")
    void from_pendiente() {
        assertEquals(EstadoPedido.PENDIENTE, EstadoPedido.from("Pendiente"));
    }

    @Test
    @DisplayName("'En preparación' se convierte a EN_PREPARACION")
    void from_enPreparacion() {
        assertEquals(EstadoPedido.EN_PREPARACION, EstadoPedido.from("En preparación"));
    }

    @Test
    @DisplayName("'Entregado' se convierte a ENTREGADO")
    void from_entregado() {
        assertEquals(EstadoPedido.ENTREGADO, EstadoPedido.from("Entregado"));
    }

    @Test
    @DisplayName("La comparación es insensible a mayúsculas")
    void from_insensibleMayusculas() {
        assertEquals(EstadoPedido.LISTO, EstadoPedido.from("LISTO"));
    }

    @Test
    @DisplayName("Valor desconocido lanza IllegalArgumentException")
    void from_valorDesconocido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> EstadoPedido.from("Archivado"));
    }

    @Test
    @DisplayName("Valor nulo lanza IllegalArgumentException")
    void from_nulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> EstadoPedido.from(null));
    }

    @Test
    @DisplayName("getLabel() devuelve el texto legible del estado")
    void getLabel_devuelveTextoLegible() {
        assertEquals("En preparación", EstadoPedido.EN_PREPARACION.getLabel());
        assertEquals("Pendiente",      EstadoPedido.PENDIENTE.getLabel());
    }

    @Test
    @DisplayName("toString() devuelve el mismo texto que getLabel()")
    void toString_igualAGetLabel() {
        for (EstadoPedido e : EstadoPedido.values()) {
            assertEquals(e.getLabel(), e.toString());
        }
    }
}
