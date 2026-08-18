package app.service;

import app.repository.FormulaRepository;
import com.pharmacyfm.domain.model.Formula;

import java.util.List;

public class FormulaService {

    private final FormulaRepository formulaRepository = new FormulaRepository();

    public List<Formula> getAllFormulas() {
        return formulaRepository.findAll();
    }

    public boolean guardarFormula(Formula f) {
        if (f.getNombre() == null || f.getNombre().trim().isEmpty()) {
            System.err.println("El nombre de la fórmula no puede estar vacío.");
            return false;
        }
        if (f.getPrecio() < 0) {
            System.err.println("El precio no puede ser negativo.");
            return false;
        }

        if (f.isNew()) {
            return formulaRepository.insert(f) > 0;
        } else {
            return formulaRepository.update(f);
        }
    }

    public boolean eliminarFormula(int id) {
        return formulaRepository.delete(id);
    }
}
