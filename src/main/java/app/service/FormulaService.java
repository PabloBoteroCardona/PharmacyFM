package app.service;

import app.Formula;
import app.repository.FormulaRepository;

import java.util.List;

/**
 * Servicio de fórmulas magistrales.
 * Se encarga de la lógica de negocio, sirviendo de puente entre la interfaz
 * de usuario y el acceso directo a la base de datos.
 */
public class FormulaService {

    private final FormulaRepository formulaRepository = new FormulaRepository();

    /**
     * Obtiene la lista completa de fórmulas registradas en el catálogo.
     */
    public List<Formula> getAllFormulas() {
        return formulaRepository.findAll();
    }

    /**
     * Gestiona el guardado de una fórmula, ya sea una nueva o la edición de una existente.
     * Incluye validaciones básicas para asegurar la integridad de los datos.
     * @return true si la operación se realizó correctamente.
     */
    public boolean guardarFormula(Formula f) {
        // Validación: El nombre es obligatorio para poder registrar la fórmula
        if (f.getNombre() == null || f.getNombre().trim().isEmpty()) {
            System.err.println("El nombre de la fórmula no puede estar vacío.");
            return false;
        }

        // Validación: No permitimos precios negativos por lógica de negocio
        if (f.getPrecio() < 0) {
            System.err.println("El precio no puede ser negativo.");
            return false;
        }

        // Lógica de decisión:
        if (f.getId() == 0) {
            // Si el ID es 0, entendemos que la fórmula aún no existe en la base de datos
            int idGenerado = formulaRepository.insert(f);
            if (idGenerado > 0) {
                f.setId(idGenerado); // Actualizamos el objeto con el ID que le ha dado la base de datos
                return true;
            }
            return false;
        } else {
            // Si el ID es distinto de 0, significa que la fórmula ya existe y queremos actualizarla
            return formulaRepository.update(f);
        }
    }

    /**
     * Elimina definitivamente una fórmula del catálogo utilizando su identificador único.
     */
    public boolean eliminarFormula(int id) {
        return formulaRepository.delete(id);
    }
}