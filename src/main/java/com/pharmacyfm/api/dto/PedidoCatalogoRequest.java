package com.pharmacyfm.api.dto;

/**
 * DTO para crear un pedido a partir de una fórmula del catálogo.
 * Se usa como cuerpo de la petición POST /api/pedidos/catalogo.
 */
public record PedidoCatalogoRequest(
        int idPaciente,
        int idFormula,
        int cantidad,
        String unidad,
        String observaciones
) {}
