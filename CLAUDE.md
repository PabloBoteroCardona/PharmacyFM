# Directrices de Desarrollo — PharmacyFM

## 1. Comandos Principales
- Compilar: `mvn clean compile`
- Ejecutar tests: `mvn test`
- Ejecutar test individual: `mvn test -Dtest=NombreClaseTest`
- Ejecutar app: `mvn javafx:run`
- Cobertura y verificación CI: `mvn clean verify`

## 2. Principios de Arquitectura y Reglas de Código
- **Clean Architecture & Regla de Dependencia:** 
  - `domain` (Modelos, Records, Enums, Interfaces/Puertos) NO depende de nadie.
  - `service` (Casos de uso) implementa lógica de negocio y usa inyección por constructor.
  - `infrastructure` (Persistencia JDBC, SQLite, config) implementa las interfaces de dominio.
  - `ui` (JavaFX Panels, Dialogs) consume **exclusivamente** servicios, nunca repositorios directos.
- **Inmutabilidad:** Entidades de dominio modeladas con `record` siempre que sea posible.
- **Tipado estricto:** Usar Enums (`Role`, `EstadoPedido`) en lugar de `String` mágicos.
- **Manejo de Errores:** Prohibido `e.printStackTrace()` o atrapar excepciones para devolver `null`. Usar excepciones de dominio tipadas o `Optional<T>`, junto con SLF4J para logging.
- **Seguridad:** Todas las consultas SQL deben usar `PreparedStatement`.
- **Recursos:** Cargar archivos mediante Classpath (`getClass().getResource(...)`), nunca rutas de archivo relativas directas.

## 3. Estilo y Convenciones de Git
- **Commits atómicos (Conventional Commits):** `feat:`, `refactor:`, `fix:`, `test:`, `ci:`.
- **Regla de Oro:** Antes de cada commit, `mvn test` debe compilar y pasar en verde.
- Usar el estándar Conventional Commits: `refactor(build): migrar a layout estándar de Maven`.