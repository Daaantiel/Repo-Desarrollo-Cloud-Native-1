package cl.duoc.ms_productos.service;

import cl.duoc.ms_productos.repository.StockRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockReservationService {
    private final StockRepository repository;
    public StockReservationService(StockRepository repository) {
        this.repository = repository;
    }

    public record Item(Long productId, Integer cantidad) {}

    @Transactional
    public void reservar(List<Item> items) {
        if (items == null || items.isEmpty() || items.size() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrito invalido");
        }
        for (Item item : items) {
            if (item == null || item.productId() == null || item.productId() <= 0 ||
                item.cantidad() == null || item.cantidad() <= 0 || item.cantidad() > 99) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto/cantidad invalida");
            }
            if (repository.descontarSiHayStock(item.productId(), item.cantidad()) != 1) {
                // La transaccion completa se revierte: ninguna linea se descuenta.
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente o producto inexistente: " + item.productId());
            }
        }
    }
}
