package cl.duoc.ms_productos.repository;

import cl.duoc.ms_productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}