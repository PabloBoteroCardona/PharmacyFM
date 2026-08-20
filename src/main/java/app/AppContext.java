package app;

import app.service.AuthService;
import app.service.FormulaService;
import app.service.PacienteService;
import app.service.PedidoService;
import com.pharmacyfm.domain.port.FormulaRepository;
import com.pharmacyfm.domain.port.PacienteRepository;
import com.pharmacyfm.domain.port.PedidoRepository;
import com.pharmacyfm.domain.port.UserRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcFormulaRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcPacienteRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcPedidoRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcUserRepository;

/**
 * Raíz de composición (Composition Root) de PharmacyFM.
 *
 * Único lugar de toda la aplicación donde se crean las implementaciones
 * concretas de los repositorios y se inyectan en los servicios.
 * Ninguna otra clase debe instanciar repositorios ni servicios directamente.
 *
 * Implementa el patrón Singleton sencillo: una sola instancia compartida
 * para toda la vida de la aplicación (lazy-init seguro para un solo hilo
 * de arranque JavaFX).
 *
 * Flujo de dependencias:
 *   AppContext crea Jdbc*Repository → inyecta en *Service
 *   LoginScreen / AdminWindow / UserWindow obtienen servicios de AppContext
 *
 * En una futura migración a Spring Boot (F6), este archivo se eliminaría
 * y Spring gestionaría la inyección automáticamente con @Component y @Autowired.
 */
public class AppContext {

    /** Instancia única compartida por toda la aplicación. */
    private static final AppContext INSTANCE = new AppContext();

    // ---- Repositorios (adaptadores JDBC concretos) ----
    private final FormulaRepository  formulaRepository;
    private final PedidoRepository   pedidoRepository;
    private final UserRepository     userRepository;
    private final PacienteRepository pacienteRepository;

    // ---- Servicios de negocio (dependencias inyectadas por constructor) ----
    private final AuthService     authService;
    private final FormulaService  formulaService;
    private final PedidoService   pedidoService;
    private final PacienteService pacienteService;

    /**
     * Constructor privado: cablea todas las dependencias de la aplicación.
     * Se ejecuta una sola vez al cargar la clase.
     */
    private AppContext() {
        // 1. Instanciar adaptadores JDBC (capa de infraestructura)
        this.formulaRepository  = new JdbcFormulaRepository();
        this.pedidoRepository   = new JdbcPedidoRepository();
        this.userRepository     = new JdbcUserRepository();
        this.pacienteRepository = new JdbcPacienteRepository();

        // 2. Inyectar los repositorios en los servicios (capa de aplicación)
        //    Los servicios solo conocen los puertos del dominio, no las implementaciones
        this.authService     = new AuthService(userRepository, pacienteRepository);
        this.formulaService  = new FormulaService(formulaRepository);
        this.pedidoService   = new PedidoService(pedidoRepository);
        this.pacienteService = new PacienteService(pacienteRepository);
    }

    /**
     * Punto de acceso global a la raíz de composición.
     *
     * @return La única instancia de AppContext.
     */
    public static AppContext get() {
        return INSTANCE;
    }

    // ---- Accesores de servicios (la UI solo llama a estos métodos) ----

    /** @return Servicio de autenticación y gestión de cuentas. */
    public AuthService authService() {
        return authService;
    }

    /** @return Servicio de gestión del catálogo de fórmulas magistrales. */
    public FormulaService formulaService() {
        return formulaService;
    }

    /** @return Servicio de gestión de pedidos. */
    public PedidoService pedidoService() {
        return pedidoService;
    }

    /** @return Servicio de gestión de perfiles de paciente. */
    public PacienteService pacienteService() {
        return pacienteService;
    }
}
