# PharmacyFM

Aplicación de escritorio para la gestión de fórmulas magistrales en farmacias.  
La idea surgió de un vacío real detectado tras años de experiencia en el sector de la salud: las farmacias no disponen de una herramienta sencilla para gestionar fórmulas magistrales, controlar la trazabilidad de los pedidos y mantener el contacto con sus pacientes desde una misma aplicación.

El proyecto se desarrolló en dos etapas: una primera versión funcional como trabajo de fin de grado DAM, y una posterior **refactorización completa hacia Clean Architecture** con el objetivo de servir como demostración de diseño de software para portfolio.

![CI](https://github.com/PabloBoteroCardona/PharmacyFM/actions/workflows/ci.yml/badge.svg)
![Coverage](https://img.shields.io/badge/Cobertura-≥70%25-brightgreen?style=flat-square)
![Java](https://img.shields.io/badge/Java-23-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-23-blue?style=flat-square)
![SQLite](https://img.shields.io/badge/SQLite-3.45-lightgrey?style=flat-square&logo=sqlite)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square&logo=apachemaven)

---

## Capturas de pantalla

### Inicio de sesión
![Login](docs/screenshots/login.png)

### Panel de administrador
![Admin](docs/screenshots/admin.png)

### Panel de paciente
![Paciente](docs/screenshots/paciente.png)

---

## Funcionalidades

### Rol Paciente
- Registro con aceptación de política de privacidad (RGPD)
- Solicitud de fórmulas del catálogo o personalizadas
- Selección de cantidad y unidad (Cápsulas, Gramos, Mililitros, Comprimidos)
- Consulta del historial de pedidos y su estado en tiempo real
- Recuperación de contraseña desde la pantalla de login

### Rol Administrador
- Gestión completa del catálogo de fórmulas magistrales (CRUD)
- Seguimiento de todos los pedidos con cambio de estado
- Gestión de datos de contacto de pacientes

---

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 23 | Lenguaje principal |
| JavaFX | 23 | Interfaz gráfica de escritorio |
| AtlantaFX | 2.1.0 | Tema visual moderno (CupertinoLight) |
| SQLite | 3.45 | Base de datos local embebida |
| BCrypt | 0.4 | Hash seguro de contraseñas |
| SLF4J + Logback | 2.0 / 1.5 | Logging estructurado |
| JUnit 5 | 5.11 | Tests unitarios e integración |
| Mockito | 5.12 | Mocks para tests unitarios de servicios |
| JaCoCo | 0.8.12 | Cobertura de código (umbral ≥ 70 %) |
| Maven | 3.x | Gestión de dependencias y ciclo de vida |

---

## Arquitectura

El proyecto sigue **Clean Architecture** con regla de dependencia estricta.  
Las capas internas no conocen las externas: el dominio y los servicios son independientes de JavaFX, SQLite o cualquier otro framework.

```
┌─────────────────────────────────────────────┐
│  UI  (app.ui.panels.*, AdminWindow…)        │  JavaFX — solo consume servicios
└──────────────────┬──────────────────────────┘
                   │ usa
┌──────────────────▼──────────────────────────┐
│  Services  (app.service.*)                  │  Casos de uso, constructor injection
└──────────────────┬──────────────────────────┘
                   │ implementa
┌──────────────────▼──────────────────────────┐
│  Domain  (com.pharmacyfm.domain.*)          │  ← Sin dependencias externas
│    model/   records + enums inmutables      │
│    port/    interfaces tecnología-agnósticas│
└──────────────────▲──────────────────────────┘
                   │ adaptadores
┌──────────────────┴──────────────────────────┐
│  Infrastructure  (…infrastructure.*)        │  JDBC + SQLite
└─────────────────────────────────────────────┘
```

**Decisiones de diseño clave:**

- `domain` — Java records inmutables (`Formula`, `Paciente`, `Pedido`), enums tipados (`Role`, `EstadoPedido`), interfaces de puerto. Cero imports de terceros.
- `service` — lógica de negocio con validaciones. Inyección por constructor desde `AppContext` (composition root). Única dependencia externa: SLF4J API (facade, sin implementación).
- `infrastructure` — adaptadores JDBC con `Supplier<Connection>` inyectable para tests de integración con SQLite en archivo temporal.
- `ui` — paneles JavaFX desacoplados. Lambda cell value factories en lugar de `PropertyValueFactory`, compatible con records Java.

---

## Tests

**65 tests** organizados en tres categorías:

| Categoría | Tests | Herramienta |
|---|---|---|
| Dominio (records, enums) | 25 | JUnit 5 |
| Servicios (casos de uso) | 23 | JUnit 5 + Mockito |
| Integración JDBC (SQLite) | 12 + utilidades | JUnit 5 |

La cobertura de instrucciones sobre dominio y servicios se verifica automáticamente con JaCoCo en cada `mvn verify` (umbral mínimo 70 %).

```bash
mvn verify   # tests + informe de cobertura en target/site/jacoco/
mvn test     # solo tests, sin verificación de cobertura
```

---

## CI/CD

GitHub Actions ejecuta `mvn verify` en cada push y pull request hacia `master`.  
El informe de cobertura JaCoCo se publica como artefacto del workflow.

Configuración: [`.github/workflows/ci.yml`](.github/workflows/ci.yml)

---

## Instalación y ejecución

### Requisitos
- Java 23+
- Maven 3.x

### Pasos

```bash
# 1. Clona el repositorio
git clone https://github.com/PabloBoteroCardona/PharmacyFM.git

# 2. Entra en el directorio
cd PharmacyFM

# 3. Ejecuta con Maven
mvn javafx:run
```

La base de datos se crea automáticamente en `~/.pharmacyfm/farmacia.db` en el primer arranque.

**Credenciales por defecto:**
- Email: `admin` | Contraseña: `admin`

---

## Estructura del proyecto

```
PharmacyFM/
├── .github/workflows/ci.yml          ← Pipeline CI (GitHub Actions)
├── src/
│   ├── main/java/
│   │   ├── app/
│   │   │   ├── service/              ← Casos de uso (FormulaService, PedidoService…)
│   │   │   ├── ui/
│   │   │   │   ├── AlertHelper.java  ← Utilidad de diálogos compartida
│   │   │   │   └── panels/           ← Paneles JavaFX (PedidosPanel, FormulasPanel…)
│   │   │   ├── AppContext.java       ← Composition root (cableado de dependencias)
│   │   │   ├── AdminWindow.java
│   │   │   ├── UserWindow.java
│   │   │   └── LoginScreen.java
│   │   └── com/pharmacyfm/
│   │       ├── domain/
│   │       │   ├── model/            ← Records + enums (Formula, Pedido, EstadoPedido…)
│   │       │   └── port/             ← Interfaces de repositorio (FormulaRepository…)
│   │       └── infrastructure/
│   │           └── persistence/      ← Adaptadores JDBC (JdbcFormulaRepository…)
│   ├── main/resources/
│   │   ├── logback.xml
│   │   └── styles.css
│   └── test/java/                    ← 65 tests (unitarios + integración)
└── pom.xml
```

---

## Seguridad

- Contraseñas almacenadas con **BCrypt** (factor de coste 10) — nunca en texto plano.
- Todas las consultas SQL usan **`PreparedStatement`** — sin riesgo de inyección SQL.
- La base de datos se guarda en el directorio del usuario (`~/.pharmacyfm/`) en lugar de rutas relativas, evitando problemas según el contexto de ejecución.

---

## Autor

**Pablo Botero Cardona** — Técnico Superior en DAM  
[LinkedIn](https://www.linkedin.com/in/pablo-botero-cardona/) · [GitHub](https://github.com/PabloBoteroCardona)
