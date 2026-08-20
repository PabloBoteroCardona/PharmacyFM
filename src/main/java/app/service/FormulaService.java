package app.service;

import com.pharmacyfm.domain.port.FormulaRepository;
import com.pharmacyfm.domain.model.Formula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Todas las dependencias se inyectan por constructor desde AppContext.
 */
public class FormulaService {

    private static final Logger log = LoggerFactory.getLogger(FormulaService.class);

    /** Puerto de acceso a datos de fórmulas, inyectado por constructor. */
    private final FormulaRepository formulaRepository;

    /**
     * Constructor principal: recibe el repositorio inyectado desde AppContext.
     *
     * @param formulaRepository Implementación del puerto de fórmulas.
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
        log.debug("Cargando catálogo de fórmulas");
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
            log.warn("Intento de guardar fórmula con nombre vacío");
            return false;
        }

        // Validación de precio no negativo (0 es válido para fórmulas sin coste)
        if (f.getPrecio() < 0) {
            log.warn("Intento de guardar fórmula con precio negativo: {}", f.getPrecio());
            return false;
        }

        // Determinamos si es nueva (id == 0) o una actualización (id > 0)
        if (f.isNew()) {
            int id = formulaRepository.insert(f);
            boolean ok = id > 0;
            if (ok) log.info("Fórmula insertada con id={}", id);
            else    log.error("Error al insertar fórmula '{}'", f.getNombre());
            return ok;
        } else {
            boolean ok = formulaRepository.update(f);
            if (ok) log.info("Fórmula actualizada, id={}", f.getId());
            else    log.warn("No se encontró la fórmula a actualizar, id={}", f.getId());
            return ok;
        }
    }

    /**
     * Elimina del catálogo una fórmula por su identificador.
     *
     * @param id ID de la fórmula a eliminar (debe ser mayor que 0).
     * @return true si se eliminó correctamente; false si el ID no existía.
     */
    public boolean eliminarFormula(int id) {
        boolean ok = formulaRepository.delete(id);
        if (ok) log.info("Fórmula eliminada, id={}", id);
        else    log.warn("No se encontró la fórmula a eliminar, id={}", id);
        return ok;
    }
}
