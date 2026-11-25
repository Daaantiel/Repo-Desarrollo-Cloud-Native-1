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

    // Exclusivo de gestion; carga items para mostrar pedidos completos.
    @Query("select distinct o from OrderEntity o left join fetch o.items order by o.id desc")
    List<OrderEntity> findAllWithItemsOrderByIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id")
    Optional<OrderEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id and o.userId = :userId")
    Optional<OrderEntity> findOwnedForUpdate(
            @Param("id") Long id,
            @Param("userId") String userId);

    // Incluye estados operativos: antes solo contemplaba PAGADO/COMPLETADO.
    @Query(value = """
        SELECT oi.product_id, SUM(oi.quantity), COUNT(DISTINCT o.id)
        FROM orders o
        JOIN order_items oi ON o.id = oi.order_id
        WHERE o.user_id = :userId
          AND o.status IN ('PAGADO', 'ACEPTADO', 'EN_PREPARACION',
                           'DESPACHADO', 'ENTREGADO', 'COMPLETADO')
        GROUP BY oi.product_id
        ORDER BY oi.product_id
        """, nativeQuery = true)
    List<Object[]> resumenProductos(@Param("userId") String userId);
}
