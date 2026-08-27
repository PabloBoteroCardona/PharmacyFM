package com.pharmacyfm.api;

import com.pharmacyfm.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@SpringBootApplication
public class MainApiApp {

    private static final Logger log = LoggerFactory.getLogger(MainApiApp.class);

    public static void main(String[] args) {
        try {
            // Igual que en MainApp (JavaFX): crea las tablas si no existen y
            // asegura el usuario administrador. Imprescindible en Cloud Run,
            // donde el sistema de archivos es efímero y cada despliegue
            // arranca con una base de datos vacía.
            Database.initializeDatabase();
        } catch (Exception e) {
            log.error("Error crítico al inicializar la base de datos", e);
        }
        SpringApplication.run(MainApiApp.class, args);
    }
}
