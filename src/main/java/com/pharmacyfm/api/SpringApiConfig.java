package com.pharmacyfm.api;

import com.pharmacyfm.service.FormulaService;
import com.pharmacyfm.service.PacienteService;
import com.pharmacyfm.service.PedidoService;
import com.pharmacyfm.domain.port.FormulaRepository;
import com.pharmacyfm.domain.port.PacienteRepository;
import com.pharmacyfm.domain.port.PedidoRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcFormulaRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcPacienteRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcPedidoRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Composition root de la API REST: cablea la infraestructura con los servicios.
 *
 * Ninguna clase de dominio ni de servicio tiene anotaciones de Spring.
 * La inyección ocurre aquí, igual que en AppContext para la UI JavaFX.
 * Esto prueba que el dominio es independiente del framework de presentación.
 */
@Configuration
public class SpringApiConfig {

    // — Adaptadores JDBC (infraestructura) —

    @Bean
    public FormulaRepository formulaRepository() {
        return new JdbcFormulaRepository();
    }

    @Bean
    public PedidoRepository pedidoRepository() {
        return new JdbcPedidoRepository();
    }

    @Bean
    public PacienteRepository pacienteRepository() {
        return new JdbcPacienteRepository();
    }

    // — Casos de uso (servicio) — mismas clases que usa la UI JavaFX —

    @Bean
    public FormulaService formulaService(FormulaRepository formulaRepository) {
        return new FormulaService(formulaRepository);
    }

    @Bean
    public PedidoService pedidoService(PedidoRepository pedidoRepository) {
        return new PedidoService(pedidoRepository);
    }

    @Bean
    public PacienteService pacienteService(PacienteRepository pacienteRepository) {
        return new PacienteService(pacienteRepository);
    }

    // — Jackson: serializar enums usando toString() para devolver el label legible —

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    }

    // — Descripción de la API en Swagger UI —

    @Bean
    public OpenAPI openApiInfo() {
        return new OpenAPI().info(new Info()
                .title("PharmacyFM API")
                .version("1.0.0")
                .description("API REST para gestión de fórmulas magistrales. " +
                        "Demuestra que los servicios de dominio son reutilizables " +
                        "independientemente del framework de presentación (JavaFX ↔ Spring Boot)."));
    }
}
