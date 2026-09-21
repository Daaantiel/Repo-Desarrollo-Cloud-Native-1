package cl.duoc.ms_productos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import cl.duoc.ms_productos.model.Producto;
import cl.duoc.ms_productos.repository.ProductoRepository;

@SpringBootApplication
public class MsProductosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProductosApplication.class, args);
    }

    @Bean
    CommandLineRunner cargarDatos(ProductoRepository repository) {
        return args -> {
            repository.save(new Producto("Charizard Holo", "Ultra Rara", 150.00, 5, "https://m.media-amazon.com/images/I/71nbfl-JklS._AC_SY741_.jpg"));
            repository.save(new Producto("Pikachu Illustrator", "Secreta", 300.00, 2, "https://images.wikidexcdn.net/mwuploads/wikidex/thumb/a/a9/latest/20210927191950/Pokémon_Illustrator_%28CoroCoro_Promo_JTCG%29.png/640px-Pokémon_Illustrator_%28CoroCoro_Promo_JTCG%29.png"));
            repository.save(new Producto("Mewtwo GX", "Rara", 85.00, 10, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTeVnDJPO7pp_nXG8LgDEXuoIJAk-hYQKbk3sgDOFyKjahCTFc-nNUA80s&s=10"));
            repository.save(new Producto("Blastoise Holo", "Ultra Rara", 120.00, 4, "https://tcgplayer-cdn.tcgplayer.com/product/42464_in_1000x1000.jpg"));
            repository.save(new Producto("Gengar VMAX", "Rara", 95.00, 7, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSLipXcNKjxw1inYTBipyms4BKT63f47mlL2-Ytlbt-PKmufkCmcwbFX07V&s=10"));
            repository.save(new Producto("Lugia Legend", "Secreta", 210.00, 3, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSD2g2utUFTBrPrKWQXWRdiHTwuYkfY7btcg_R7fBTVRw&s=10"));
        };
    }
}