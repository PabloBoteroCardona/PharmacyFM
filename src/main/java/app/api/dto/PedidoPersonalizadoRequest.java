package app.api.dto;

/**
 * DTO para crear un pedido de fórmula personalizada (no existe en el catálogo).
 * Se usa como cuerpo de la petición POST /api/pedidos/personalizado.
 */
public record PedidoPersonalizadoRequest(
        int idPaciente,
        String nombreFormula,
        int cantidad,
        String unidad,
        String observaciones
) {}
