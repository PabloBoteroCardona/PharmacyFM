package com.pharmacyfm.service;

import com.pharmacyfm.domain.model.Pedido;
import com.pharmacyfm.domain.port.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de PedidoService con Mockito.
 *
 * PedidoRepository se sustituye por un mock; las pruebas verifican
 * únicamente la lógica de negocio del servicio: validaciones de cantidad
 * y nombre de fórmula, bifurcación catálogo/personalizada, y cambio de estado.
 *
 * La inyección por constructor permite pasar el mock directamente sin reflexión.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService — tests unitarios con mock")
class PedidoServiceTest {

    /** Mock del repositorio de pedidos. */
    @Mock
    private PedidoRepository pedidoRepository;

    /** Servicio bajo prueba, recibe el mock por constructor. */
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoRepository);
    }

    // =========================================================
    // getPedidosByPaciente / getAllPedidos
    // =========================================================

    @Test
    @DisplayName("getPedidosByPaciente delega en el repositorio y devuelve la lista")
    void getPedidosByPaciente_delegaEnRepositorio() {
        when(pedidoRepository.findByPacienteId(1)).thenReturn(List.of());

        List<Pedido> resultado = pedidoService.getPedidosByPaciente(1);

        assertNotNull(resultado, "La lista nunca debe ser null");
        verify(pedidoRepository, times(1)).findByPacienteId(1);
    }

    @Test
    @DisplayName("getAllPedidos delega en el repositorio y devuelve la lista completa")
    void getAllPedidos_delegaEnRepositorio() {
        when(pedidoRepository.findAll()).thenReturn(List.of());

        List<Pedido> resultado = pedidoService.getAllPedidos();

        assertNotNull(resultado, "La lista nunca debe ser null");
        verify(pedidoRepository, times(1)).findAll();
    }

    // =========================================================
    // crearPedidoFormulaCatalogo — validaciones y delegación
    // =========================================================

    @Test
    @DisplayName("crearPedidoFormulaCatalogo con cantidad <= 0 devuelve false sin llamar al repositorio")
    void crearPedidoCatalogo_cantidadCero_devuelveFalse() {
        boolean resultado = pedidoService.crearPedidoFormulaCatalogo(1, 2, 0, "Cápsulas", "");

        assertFalse(resultado, "Cantidad 0 no es válida para un pedido");
        verify(pedidoRepository, never()).insert(anyInt(), any(), any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("crearPedidoFormulaCatalogo válido delega en insert con idFormula y sin nombre personalizado")
    void crearPedidoCatalogo_valido_delegaEnInsert() {
        // El servicio llama a insert con idFormula=2 y formulaPersonalizada=null
        when(pedidoRepository.insert(1, 2, null, 3, "Cápsulas", "Antes de dormir")).thenReturn(true);

        boolean resultado = pedidoService.crearPedidoFormulaCatalogo(1, 2, 3, "Cápsulas", "Antes de dormir");

        assertTrue(resultado, "Pedido de catálogo válido debe registrarse correctamente");
        verify(pedidoRepository, times(1)).insert(1, 2, null, 3, "Cápsulas", "Antes de dormir");
    }

    // =========================================================
    // crearPedidoFormulaPersonalizada — validaciones y delegación
    // =========================================================

    @Test
    @DisplayName("crearPedidoFormulaPersonalizada con cantidad <= 0 devuelve false sin llamar al repositorio")
    void crearPedidoPersonalizado_cantidadCero_devuelveFalse() {
        boolean resultado = pedidoService.crearPedidoFormulaPersonalizada(
                1, "Melatonina 1mg", 0, "Cápsulas", "");

        assertFalse(resultado, "Cantidad 0 no es válida");
        verify(pedidoRepository, never()).insert(anyInt(), any(), any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("crearPedidoFormulaPersonalizada con nombre vacío devuelve false sin llamar al repositorio")
    void crearPedidoPersonalizado_nombreVacio_devuelveFalse() {
        boolean resultado = pedidoService.crearPedidoFormulaPersonalizada(1, "  ", 2, "Gramos", "");

        assertFalse(resultado, "Nombre de fórmula vacío no es válido");
        verify(pedidoRepository, never()).insert(anyInt(), any(), any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("crearPedidoFormulaPersonalizada válido delega en insert con idFormula=null")
    void crearPedidoPersonalizado_valido_delegaEnInsert() {
        // El servicio llama a insert con idFormula=null y el nombre personalizado
        when(pedidoRepository.insert(1, null, "Melatonina 1mg", 30, "Cápsulas", "Con cena"))
                .thenReturn(true);

        boolean resultado = pedidoService.crearPedidoFormulaPersonalizada(
                1, "Melatonina 1mg", 30, "Cápsulas", "Con cena");

        assertTrue(resultado, "Pedido personalizado válido debe registrarse correctamente");
        verify(pedidoRepository, times(1))
                .insert(1, null, "Melatonina 1mg", 30, "Cápsulas", "Con cena");
    }

    // =========================================================
    // actualizarEstado — validaciones y delegación
    // =========================================================

    @Test
    @DisplayName("actualizarEstado con estado vacío devuelve false sin llamar al repositorio")
    void actualizarEstado_estadoVacio_devuelveFalse() {
        boolean resultado = pedidoService.actualizarEstado(5, "  ");

        assertFalse(resultado, "Estado vacío no es válido");
        verify(pedidoRepository, never()).updateEstado(anyInt(), any());
    }

    @Test
    @DisplayName("actualizarEstado válido delega en updateEstado y devuelve su resultado")
    void actualizarEstado_valido_delegaEnRepositorio() {
        when(pedidoRepository.updateEstado(5, "Listo")).thenReturn(true);

        boolean resultado = pedidoService.actualizarEstado(5, "Listo");

        assertTrue(resultado, "Actualización de estado válida debe devolver true");
        verify(pedidoRepository, times(1)).updateEstado(5, "Listo");
    }
}
