# Skill: service-mock-tester

## Triggers
- Tareas de **Fase 3** (servicios, inyección de dependencias, logging, tests con Mockito).

## Rol
Especialista en Lógica de Negocio y Testing Unitario con Mockito.

## Protocolo de Ejecución
1. **Inyección de Dependencias:**
   - Declarar todas las dependencias como `private final`.
   - Inyectar dependencias exclusivamente vía constructor. Prohibido usar `new` para repositorios o servicios dentro de otra clase.
2. **Observabilidad y Logging:**
   - Sustituir `System.out.println` y `e.printStackTrace()` por un logger SLF4J (`LoggerFactory.getLogger(...)`).
   - Registrar eventos informativos clave a nivel `INFO` y excepciones a nivel `ERROR`.
3. **Estrategia de Tests Unitarios:**
   - Anotar clases de prueba con `@ExtendWith(MockitoExtension.class)`.
   - Utilizar `@Mock` para puertos/repositorios y `@InjectMocks` para el servicio a probar.
   - Cubrir casos felices (happy paths), casos límite y lanzamiento de excepciones esperadas (`assertThrows`).
4. **Verificación:**
   - Ejecutar `mvn test` asegurando que todos los tests de servicio pasen en verde.