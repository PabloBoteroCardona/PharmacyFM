package com.pharmacyfm.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacyfm.domain.model.Formula;
import com.pharmacyfm.service.FormulaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoints REST para el catálogo de fórmulas magistrales.
 * FormulaService no tiene ninguna anotación de Spring: llega por inyección
 * de constructor desde SpringApiConfig, igual que desde AppContext en la UI.
 */
@RestController
@RequestMapping("/api/formulas")
@Tag(name = "Fórmulas", description = "Gestión del catálogo de fórmulas magistrales")
public class FormulasController {

    private final FormulaService formulaService;

    public FormulasController(FormulaService formulaService) {
        this.formulaService = formulaService;
    }

    @GetMapping
    @Operation(summary = "Obtener el catálogo completo de fórmulas")
    public List<Formula> getAll() {
        return formulaService.getAllFormulas();
    }

    @PostMapping
    @Operation(summary = "Añadir una nueva fórmula al catálogo")
    public ResponseEntity<Void> crear(@RequestBody Formula formula) {
        // id = 0 → Formula.isNew() = true → FormulaService llama a insert
        Formula nueva = new Formula(formula.nombre(), formula.descripcion(), formula.precio());
        boolean ok = formulaService.guardarFormula(nueva);
        return ok ? ResponseEntity.status(201).build()
                  : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una fórmula existente")
    public ResponseEntity<Void> actualizar(@PathVariable int id, @RequestBody Formula formula) {
        Formula actualizada = new Formula(id, formula.nombre(), formula.descripcion(), formula.precio());
        boolean ok = formulaService.guardarFormula(actualizada);
        return ok ? ResponseEntity.ok().build()
                  : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una fórmula del catálogo")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        boolean ok = formulaService.eliminarFormula(id);
        return ok ? ResponseEntity.noContent().build()
                  : ResponseEntity.notFound().build();
    }
}
