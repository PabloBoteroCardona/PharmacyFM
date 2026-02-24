package app.service;

import app.Formula;
import app.repository.FormulaRepository;

import java.util.List;

/**
 * Servicio de fórmulas magistrales.
 * Gestiona la lógica de negocio relacionada con el catálogo de fórmulas.
 */
public class FormulaService {

    private final FormulaRepository formulaRepository = new FormulaRepository();

    /**
     * Devuelve todas las fórmulas del catálogo.
     */
    public List<Formula> getAllFormulas() {
        return formulaRepository.findAll();
    }

    /**
     * Guarda una fórmula nueva o actualiza una existente.
     * Si la fórmula tiene id=0 es nueva, si tiene id>0 es una actualización.
     * Devuelve true si la operación fue exitosa.
     */
    public boolean guardarFormula(Formula f) {
        if (f.getNombre() == null || f.getNombre().trim().isEmpty()) {
            System.err.println("El nombre de la fórmula no puede estar vacío.");
            return false;
        }

        if (f.getPrecio() < 0) {
            System.err.println("El precio no puede ser negativo.");
            return false;
        }

        if (f.getId() == 0) {
            // Fórmula nueva
            int idGenerado = formulaRepository.insert(f);
            if (idGenerado > 0) {
                f.setId(idGenerado);
                return true;
            }
            return false;
        } else {
            // Fórmula existente
            return formulaRepository.update(f);
        }
    }

    /**
     * Elimina una fórmula del catálogo por su id.
     * Devuelve true si se eliminó correctamente.
     */
    public boolean eliminarFormula(int id) {
        return formulaRepository.delete(id);
    }
}