package com.pharmacyfm.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el record Pedido.
 *
 * Verifica los métodos cantidadConUnidad() y withEstado() que son los únicos
 * con lógica propia más allá de los accesores generados por el record.
 */
@DisplayName("Pedido — modelo de dominio inmutable (record)")
class PedidoTest {

    private static final Pedido BASE = new Pedido(
            1, 10, "Ana García", "2026-01-15 10:30:00",
            "Vitamina C", EstadoPedido.PENDIENTE, 2, "Cápsulas", "Sin gluten"
    );

    @Test
    @DisplayName("Constructor inicializa todos los campos correctamente")
    void constructor_inicializaCamposCorrectos() {
        assertEquals(1,                     BASE.id());
        assertEquals(10,                    BASE.idPaciente());
        assertEquals("Ana García",          BASE.nombrePaciente());
        assertEquals("2026-01-15 10:30:00", BASE.fecha());
        assertEquals("Vitamina C",          BASE.nombreFormula());
        assertEquals(EstadoPedido.PENDIENTE, BASE.estado());
        assertEquals(2,                     BASE.cantidad());
        assertEquals("Cápsulas",            BASE.unidad());
        assertEquals("Sin gluten",          BASE.observaciones());
    }

    @Test
    @DisplayName("cantidadConUnidad devuelve la cantidad y la unidad separadas por un espacio")
    void cantidadConUnidad_devuelveFormatoCorrecto() {
        assertEquals("2 Cápsulas", BASE.cantidadConUnidad());
    }

    @Test
    @DisplayName("cantidadConUnidad con unidad nula devuelve cantidad seguida de espacio vacío")
    void cantidadConUnidad_unidadNula_noLanzaExcepcion() {
        Pedido sinUnidad = new Pedido(
                2, 10, "Ana", "2026-01-15", "Vitamina C",
                EstadoPedido.PENDIENTE, 1, null, ""
        );

        // No debe lanzar NullPointerException
        assertDoesNotThrow(() -> sinUnidad.cantidadConUnidad());
        assertEquals("1 ", sinUnidad.cantidadConUnidad());
    }

    @Test
    @DisplayName("withEstado devuelve una nueva instancia con el estado actualizado")
    void withEstado_devuelveNuevoPedidoConEstadoCambiado() {
        Pedido actualizado = BASE.withEstado(EstadoPedido.EN_PREPARACION);

        // El estado debe haber cambiado
        assertEquals(EstadoPedido.EN_PREPARACION, actualizado.estado());

        // El resto de campos debe permanecer intacto
        assertEquals(BASE.id(),              actualizado.id());
        assertEquals(BASE.nombreFormula(),   actualizado.nombreFormula());
        assertEquals(BASE.cantidadConUnidad(), actualizado.cantidadConUnidad());

        // El objeto original no debe haber cambiado (inmutabilidad)
        assertEquals(EstadoPedido.PENDIENTE, BASE.estado());
    }

    @Test
    @DisplayName("withEstado con ENTREGADO refleja correctamente el estado final")
    void withEstado_estadoEntregado_seAlmacenaCorrectamente() {
        Pedido entregado = BASE.withEstado(EstadoPedido.ENTREGADO);

        assertEquals(EstadoPedido.ENTREGADO, entregado.estado());
        assertEquals("Entregado", entregado.estado().getLabel());
    }
}
