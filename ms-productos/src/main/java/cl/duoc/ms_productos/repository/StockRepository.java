package cl.duoc.ms_productos.repository;

import cl.duoc.ms_productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Producto, Long> {
    // UPDATE condicional: dos compras concurrentes no pueden llevar stock bajo cero.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Producto p set p.stock = p.stock - :cantidad " +
           "where p.id = :productoId and p.stock >= :cantidad")
    int descontarSiHayStock(@Param("productoId") Long productoId,
                           @Param("cantidad") Integer cantidad);
}
