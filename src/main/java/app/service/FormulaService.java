package app.service;

import com.pharmacyfm.domain.port.FormulaRepository;
import com.pharmacyfm.domain.model.Formula;
import com.pharmacyfm.infrastructure.persistence.JdbcFormulaRepository;

import java.util.List;

/**
 * Servicio de gestión del catálogo de fórmulas magistrales.
 *
 * Contiene la lógica de negocio relacionada con las fórmulas:
 * validaciones de datos antes de persistir y decisión entre
 * insertar o actualizar según el estado del objeto recibido.
 *
 * La distinción insert/update se delega en el método Formula.isNew()
 * (que devuelve true cuando id == 0), siguiendo el principio
 * «el modelo conoce su propio estado».
 *
 * Depende del puerto domain.port.FormulaRepository; en F3 la implementación
 * concreta se inyectará desde fuera en lugar de instanciarse aquí.
 */
public class FormulaService {

    // Tipado como puerto de dominio — el servicio no conoce si es JDBC, JPA u otro
    private final FormulaRepository formulaRepository;

    /**
     * Constructor por defecto: cablea la implementación JDBC concreta.
     * En F3 se sustituirá por inyección de dependencias desde el llamante.
     */
    public FormulaService() {
        this.formulaRepository = new JdbcFormulaRepository();
    }

    /**
     * Constructor para tests: permite inyectar un doble de test (mock/stub).
     *
     * @param formulaRepository Implementación alternativa del puerto.
     */
    public FormulaService(FormulaRepository formulaRepository) {
        this.formulaRepository = formulaRepository;
    }

    /**
     * Recupera el catálogo completo de fórmulas ordenado alfabéticamente.
     * Se usa tanto en el panel de administración como en el formulario de pedido del paciente.
     *
     * @return Lista de todas las fórmulas; lista vacía si no hay ninguna.
     */
    public List<Formula> getAllFormulas() {
        return formulaRepository.findAll();
    }

    /**
     * Persiste una fórmula en la base de datos, insertando o actualizando según corresponda.
     *
     * Validaciones aplicadas antes de persistir:
     *   - El nombre no puede ser nulo ni vacío.
     *   - El precio no puede ser negativo.
     *
     * @param f Fórmula a guardar. Si f.isNew() == true se inserta; si no, se actualiza.
     * @return true si la operación se completó correctamente.
     */
    public boolean guardarFormula(Formula f) {
        // Validación de nombre obligatorio
        if (f.getNombre() == null || f.getNombre().trim().isEmpty()) {
            System.err.println("[FormulaService] El nombre de la fórmula no puede estar vacío.");
            return false;
        }

        // Validación de precio no negativo (0 es válido para fórmulas sin coste)
        if (f.getPrecio() < 0) {
            System.err.println("[FormulaService] El precio no puede ser negativo.");
            return false;
        }

        // Determinamos si es nueva (id == 0) o una actualización (id > 0)
        if (f.isNew()) {
            return formulaRepository.insert(f) > 0;
        } else {
            return formulaRepository.update(f);
        }
    }

    /**
     * Elimina del catálogo una fórmula por su identificador.
     *
     * @param id ID de la fórmula a eliminar (debe ser mayor que 0).
     * @return true si se eliminó correctamente; false si el ID no existía.
     */
    public boolean eliminarFormula(int id) {
        return formulaRepository.delete(id);
    }
}
