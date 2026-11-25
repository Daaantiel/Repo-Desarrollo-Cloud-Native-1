package cl.duoc.ms_orders.service;

import cl.duoc.ms_orders.model.OrderEntity;
import cl.duoc.ms_orders.model.OrderItemEntity;
import cl.duoc.ms_orders.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderPersistenceService {
    private final OrderRepository repository;
    private final InventoryClient inventoryClient;

    public OrderPersistenceService(OrderRepository repository,
            InventoryClient inventoryClient) {
        this.repository = repository;
        this.inventoryClient = inventoryClient;
    }

    // Solo recibimos identidad desde el BFF autenticado, nunca desde JSON.
    @Transactional
    public Map<String, Object> registrar(String userId, Map<String, Object> datos) {
        OrderEntity pedido = new OrderEntity(userId);
        Object lineas = datos.get("items");
        if (lineas instanceof List<?> lista) {
            if (lista.isEmpty() || lista.size() > 30) {
                throw badRequest("El carrito debe tener entre 1 y 30 lineas");
            }
            for (Object linea : lista) {
                if (!(linea instanceof Map<?, ?> item)) {
                    throw badRequest("Formato de producto invalido");
                }
                agregarItem(pedido, item);
            }
        } else {
            // Mantiene compatibilidad con el formato anterior de un producto.
            agregarItem(pedido, datos);
        }
        OrderEntity guardado = repository.saveAndFlush(pedido);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", guardado.getId());
        respuesta.put("estado", guardado.getStatus());
        respuesta.put("fecha", guardado.getCreatedAt().toLocalDate().toString());
        respuesta.put("totalProductos", guardado.getItems().size());
        return respuesta;
    }

    private void agregarItem(OrderEntity pedido, Map<?, ?> datos) {
        Object rawId = valor(datos, "productoId", "productId", "product_id");
        if (rawId == null && datos.get("producto") instanceof Map<?, ?> p) {
            rawId = p.get("id");
        }
        long id = numero(rawId, "productoId");
        Object rawCantidad = valor(datos, "cantidad", "quantity");
        long cantidad = rawCantidad == null ? 1 : numero(rawCantidad, "cantidad");
        if (id < 1 || cantidad < 1 || cantidad > 99) {
            throw badRequest("Producto o cantidad fuera de rango");
        }
        Object rawPrecio = valor(datos, "precioUnitario", "unitPrice", "precio");
        BigDecimal precio = null;
        if (rawPrecio != null) {
            try {
                precio = new BigDecimal(rawPrecio.toString());
            } catch (NumberFormatException e) {
                throw badRequest("Precio invalido");
            }
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                throw badRequest("Precio negativo");
            }
        }
        // El precio es solo informativo en esta demo; no es un cobro real.
        pedido.addItem(new OrderItemEntity(id, Math.toIntExact(cantidad), precio));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(String userId) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (OrderEntity pedido : repository.findByUserIdOrderByIdDesc(userId)) {
            for (OrderItemEntity item : pedido.getItems()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("id", pedido.getId());
                fila.put("productoId", item.getProductId());
                fila.put("productId", item.getProductId());
                fila.put("cantidad", item.getQuantity());
                fila.put("precioUnitario", item.getUnitPrice());
                fila.put("estado", pedido.getStatus());
                fila.put("fecha", pedido.getCreatedAt().toLocalDate().toString());
                resultado.add(fila);
            }
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> resumen(String userId) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] fila : repository.resumenProductos(userId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId", ((Number) fila[0]).longValue());
            item.put("cantidadTotal", ((Number) fila[1]).longValue());
            item.put("totalPedidos", ((Number) fila[2]).longValue());
            resultado.add(item);
        }
        return resultado;
    }

    public record ResultadoPago(Map<String, Object> respuesta, boolean publicar) {}

    @Transactional
    public ResultadoPago simularPago(Long id, String userId, boolean aprobado) {
        OrderEntity pedido = repository.findOwnedForUpdate(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        if ("PAGADO".equals(pedido.getStatus()) || "COMPLETADO".equals(pedido.getStatus())) {
            return new ResultadoPago(datosPago(pedido), false);
        }
        if (!"PENDIENTE_PAGO".equals(pedido.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este pedido ya fue resuelto");
        }
        // Descontar stock ANTES de marcar pagado. Un 409 deja pendiente el pedido.
        if (aprobado) {
            inventoryClient.reservar(pedido.getItems());
        }
        pedido.resolverPagoSimulado(aprobado);
        repository.saveAndFlush(pedido);
        return new ResultadoPago(datosPago(pedido), aprobado);
    }

    private Map<String, Object> datosPago(OrderEntity pedido) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("id", pedido.getId());
        datos.put("estado", pedido.getStatus());
        datos.put("proveedor", "SIMULADO");
        datos.put("referencia", pedido.getPaymentReference());
        return datos;
    }

    // Panel administrativo: una fila por pedido, con sus productos asociados.
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarTodosAdmin() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (OrderEntity pedido : repository.findAllWithItemsOrderByIdDesc()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("id", pedido.getId());
            fila.put("clienteId", pedido.getUserId());
            fila.put("estado", pedido.getStatus());
            fila.put("fecha", pedido.getCreatedAt().toString());
            fila.put("fechaPago", pedido.getPaidAt() == null ? null : pedido.getPaidAt().toString());
            List<Map<String, Object>> productos = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            boolean preciosCompletos = true;
            for (OrderItemEntity item : pedido.getItems()) {
                Map<String, Object> producto = new LinkedHashMap<>();
                producto.put("productoId", item.getProductId());
                producto.put("cantidad", item.getQuantity());
                producto.put("precioUnitario", item.getUnitPrice());
                productos.add(producto);
                if (item.getUnitPrice() != null) {
                    total = total.add(item.getUnitPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity())));
                } else {
                    preciosCompletos = false;
                }
            }
            fila.put("items", productos);
            fila.put("total", preciosCompletos ? total : null);
            resultado.add(fila);
        }
        return resultado;
    }

    public record ResultadoCambioEstado(
            Map<String, Object> respuesta, boolean publicar,
            String evento, String propietarioId, String estado) {}

    @Transactional
    public ResultadoCambioEstado cambiarEstadoAdmin(Long id, String nuevoEstado) {
        String destino = nuevoEstado.trim().toUpperCase(Locale.ROOT);
        OrderEntity pedido = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        String actual = pedido.getStatus();
        if (actual.equals(destino)) {
            return new ResultadoCambioEstado(datosCambio(pedido, actual),
                    false, null, pedido.getUserId(), destino);
        }
        String evento = eventoPermitido(actual, destino);
        if (evento == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transicion no permitida: " + actual + " -> " + destino);
        }
        pedido.actualizarEstadoGestion(destino);
        repository.saveAndFlush(pedido);
        return new ResultadoCambioEstado(datosCambio(pedido, actual),
                true, evento, pedido.getUserId(), destino);
    }

    private String eventoPermitido(String actual, String destino) {
        // Se cancela solo antes del pago porque la compra pagada ya descuento
        // inventario y aun no existe un flujo transaccional de reposicion.
        if ("PENDIENTE_PAGO".equals(actual) && "CANCELADO".equals(destino)) {
            return "OrderCancelled";
        }
        if ("PAGADO".equals(actual) && "ACEPTADO".equals(destino)) {
            return "OrderAccepted";
        }
        if ("ACEPTADO".equals(actual) && "EN_PREPARACION".equals(destino)) {
            return "OrderPreparing";
        }
        if ("EN_PREPARACION".equals(actual) && "DESPACHADO".equals(destino)) {
            return "OrderDispatched";
        }
        if ("DESPACHADO".equals(actual) && "ENTREGADO".equals(destino)) {
            return "OrderDelivered";
        }
        return null;
    }

    private Map<String, Object> datosCambio(OrderEntity pedido, String anterior) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("id", pedido.getId());
        datos.put("clienteId", pedido.getUserId());
        datos.put("estadoAnterior", anterior);
        datos.put("estado", pedido.getStatus());
        return datos;
    }

    private Object valor(Map<?, ?> datos, String... nombres) {
        for (String nombre : nombres) {
            if (datos.get(nombre) != null) return datos.get(nombre);
        }
        return null;
    }

    private long numero(Object raw, String campo) {
        try {
            if (raw == null) throw new IllegalArgumentException();
            return Long.parseLong(raw.toString());
        } catch (IllegalArgumentException e) {
            throw badRequest("Campo numerico invalido: " + campo);
        }
    }

    private ResponseStatusException badRequest(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
