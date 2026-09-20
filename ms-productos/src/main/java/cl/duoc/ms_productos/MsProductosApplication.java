package cl.duoc.ms_productos;

import cl.duoc.ms_productos.model.Producto;
import cl.duoc.ms_productos.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MsProductosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProductosApplication.class, args);
    }

    @Bean
    CommandLineRunner cargarDatos(ProductoRepository repository) {
        return args -> {
            repository.save(new Producto("Charizard Holo", "Ultra Rara", 150.00, 5, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png"));
            repository.save(new Producto("Pikachu Illustrator", "Secreta", 300.00, 2, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png"));
            repository.save(new Producto("Mewtwo GX", "Rara", 85.00, 10, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/150.png"));
        };
    }
}