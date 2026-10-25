package cl.duoc.ms_orders.repository;

import cl.duoc.ms_orders.model.OrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserIdOrderByIdDesc(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id and o.userId = :userId")
    Optional<OrderEntity> findOwnedForUpdate(
            @Param("id") Long id,
            @Param("userId") String userId);

    @Query(value = """
        SELECT oi.product_id, SUM(oi.quantity), COUNT(DISTINCT o.id)
        FROM orders o
        JOIN order_items oi ON o.id = oi.order_id
        WHERE o.user_id = :userId
          AND o.status IN ('PAGADO', 'COMPLETADO')
        GROUP BY oi.product_id
        ORDER BY oi.product_id
        """, nativeQuery = true)
    List<Object[]> resumenProductos(@Param("userId") String userId);
}
