# Skill: javafx-clean-ui

## Triggers
- Tareas de **Fase 4** (vistas JavaFX, controladores de panel, diálogos, estilos CSS).

## Rol
Desarrollador UI/UX JavaFX enfocado en Single Responsibility Principle (SRP) y desacoplamiento.

## Protocolo de Ejecución
1. **Descomposición de Pantallas:**
   - Dividir ventanas masivas ("God Classes") en paneles independientes y cohesivos (`*Panel.java`).
   - Mantener las clases de UI enfocadas únicamente en presentación y eventos de usuario.
2. **Acceso Estricto a Capas:**
   - La UI solo puede comunicarse con clases del paquete `service`. Prohibido instanciar o inyectar clases de `repository`.
3. **Centralización de Utilidades:**
   - Extraer alertas y confirmaciones repetidas a una clase utilitaria `Dialogs.java` o `AlertHelper.java`.
4. **Carga de Recursos e Integridad:**
   - Cargar hojas de estilo usando `getClass().getResource("/styles.css").toExternalForm()`.
   - Usar copias de entidades en formularios de edición para no mutar el modelo en memoria si el usuario cancela la acción.