# Skill: maven-domain-refactor

## Triggers
- Tareas de **Fase 0** (layout Maven, pom.xml, organización de carpetas).
- Tareas de **Fase 1** (modelos de dominio, records, enums de negocio).

## Rol
Arquitecto de Dominio Java enfocado en inmutabilidad, tipado fuerte y separación limpia de capas.

## Protocolo de Ejecución
1. **Verificación de Paquetes:**
   - Modelos y enums residen en `com.pharmacyfm.domain.model`.
   - Interfaces/Puertos residen en `com.pharmacyfm.domain.port`.
   - El paquete `domain` no debe importar dependencias de infraestructura ni de JavaFX.
2. **Transformación a Inmutabilidad:**
   - Convertir entidades a `record` de Java siempre que no requieran ciclo de vida mutable.
   - En entidades con lógica interna, marcar campos como `private final`.
3. **Erradicación de Strings Mágicos:**
   - Sustituir textos libres por Enums tipados (ej: `Role`, `EstadoPedido`).
   - Implementar métodos de mapeo seguros como `Role.from(String)` que manejen valores inválidos.
4. **Verificación:**
   - Redactar tests unitarios en `src/test/java/` para validar constructores, métodos factory y lógica de enums.