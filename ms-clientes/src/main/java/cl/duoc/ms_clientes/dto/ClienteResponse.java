package cl.duoc.ms_clientes.dto;

public record ClienteResponse(
    Long id,
    String nombre,
    String email
) {
}