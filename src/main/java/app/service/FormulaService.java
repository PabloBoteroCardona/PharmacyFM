package app.service;

import app.repository.FormulaRepository;
import com.pharmacyfm.domain.model.Formula;

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
 */
public class FormulaService {

    // Acceso a datos de fórmulas; se instancia aquí hasta que F3 aplique inyección de constructor
    private final FormulaRepository formulaRepository = new FormulaRepository();

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
     * @param f Fórmula a guardar. Si f.isNew() == true se inserta; en caso contrario se actualiza.
     * @return true si la operación se completó correctamente.
     */
    public boolean guardarFormula(Formula f) {
        // Validación de nombre obligatorio
        if (f.getNombre() == null || f.getNombre().trim().isEmpty()) {
            System.err.println("El nombre de la fórmula no puede estar vacío.");
            return false;
        }

        // Validación de precio no negativo (0 es válido para fórmulas sin coste)
        if (f.getPrecio() < 0) {
            System.err.println("El precio no puede ser negativo.");
            return false;
        }

        // Determinamos si es una nueva fórmula (id == 0) o una actualización (id > 0)
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
