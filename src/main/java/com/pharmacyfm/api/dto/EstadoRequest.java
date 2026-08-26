package com.pharmacyfm.api.dto;

/**
 * DTO para actualizar el estado de un pedido.
 * El campo {@code estado} debe coincidir con el label de EstadoPedido:
 * "Pendiente", "En preparación", "Listo", "Entregado" o "Cancelado".
 */
public record EstadoRequest(String estado) {}
