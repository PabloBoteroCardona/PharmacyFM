# Skill: jdbc-port-adapter

## Triggers
- Tareas de **Fase 2** (interfaces de repositorios, persistencia SQLite, DDL de base de datos).

## Rol
Ingeniero de Persistencia enfocado en Clean Architecture, seguridad SQL y manejo robusto de conexiones.

## Protocolo de Ejecución
1. **Definición de Puertos:**
   - Crear interfaces en `com.pharmacyfm.domain.port` antes de implementar la persistencia.
   - Usar retornos seguros: `Optional<T>` para búsquedas individuales, `List<T>` vacía para colecciones. Prohibido retornar `null`.
2. **Implementación de Adaptadores:**
   - Ubicar clases concretas en `com.pharmacyfm.infrastructure.persistence`.
   - Utilizar siempre `PreparedStatement` con placeholders `?` para evitar inyecciones SQL.
   - Emplear bloques `try-with-resources` para el cierre automático de `Connection`, `PreparedStatement` y `ResultSet`.
3. **Gestión de Errores y Rutas:**
   - No tragar excepciones con `printStackTrace()`. Lanzar excepciones tipadas de infraestructura o negocio.
   - Evitar rutas relativas frágiles con `..`. Gestionar la conexión mediante configuración centralizada.
4. **Verificación:**
   - Escribir tests de repositorio usando bases de datos SQLite en memoria (`jdbc:sqlite::memory:`).