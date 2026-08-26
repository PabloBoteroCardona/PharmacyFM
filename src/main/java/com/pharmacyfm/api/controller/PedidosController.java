package com.pharmacyfm.api.controller;

import com.pharmacyfm.api.dto.EstadoRequest;
import com.pharmacyfm.api.dto.PedidoCatalogoRequest;
import com.pharmacyfm.api.dto.PedidoPersonalizadoRequest;
import com.pharmacyfm.service.PedidoService;
import com.pharmacyfm.domain.model.Pedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para la gestión de pedidos.
 */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestión del ciclo de vida de los pedidos de fórmulas")
public class PedidosController {

    private final PedidoService pedidoService;

    public PedidosController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los pedidos del sistema (vista administrador)")
    public List<Pedido> getAll() {
        return pedidoService.getAllPedidos();
    }

    @GetMapping("/paciente/{idPaciente}")
    @Operation(summary = "Obtener el historial de pedidos de un paciente")
    public List<Pedido> getPorPaciente(@PathVariable int idPaciente) {
        return pedidoService.getPedidosByPaciente(idPaciente);
    }

    @PostMapping("/catalogo")
    @Operation(summary = "Crear un pedido de una fórmula del catálogo")
    public ResponseEntity<Void> crearDesdeCatalogo(@RequestBody PedidoCatalogoRequest req) {
        boolean ok = pedidoService.crearPedidoFormulaCatalogo(
                req.idPaciente(), req.idFormula(), req.cantidad(), req.unidad(), req.observaciones());
        return ok ? ResponseEntity.status(201).build()
                  : ResponseEntity.badRequest().build();
    }

    @PostMapping("/personalizado")
    @Operation(summary = "Crear un pedido de fórmula personalizada (no está en el catálogo)")
    public ResponseEntity<Void> crearPersonalizado(@RequestBody PedidoPersonalizadoRequest req) {
        boolean ok = pedidoService.crearPedidoFormulaPersonalizada(
                req.idPaciente(), req.nombreFormula(), req.cantidad(), req.unidad(), req.observaciones());
        return ok ? ResponseEntity.status(201).build()
                  : ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de un pedido",
               description = "Valores válidos: Pendiente, En preparación, Listo, Entregado, Cancelado")
    public ResponseEntity<Void> actualizarEstado(@PathVariable int id,
                                                  @RequestBody EstadoRequest req) {
        boolean ok = pedidoService.actualizarEstado(id, req.estado());
        return ok ? ResponseEntity.ok().build()
                  : ResponseEntity.notFound().build();
    }
}
