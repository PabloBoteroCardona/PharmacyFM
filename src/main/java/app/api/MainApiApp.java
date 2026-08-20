package app.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST de PharmacyFM.
 *
 * Demuestra la regla de dependencia de Clean Architecture:
 * los mismos servicios que alimentan la UI JavaFX funcionan
 * aquí sin modificación alguna, simplemente cableados como @Bean
 * en SpringApiConfig en lugar de en AppContext.
 *
 * Arranque:    mvn spring-boot:run
 * Swagger UI:  http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication(scanBasePackages = "app.api")
public class MainApiApp {

    public static void main(String[] args) {
        SpringApplication.run(MainApiApp.class, args);
    }
}
