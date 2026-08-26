package com.pharmacyfm.api.controller;

import com.pharmacyfm.service.PacienteService;
import com.pharmacyfm.domain.model.Paciente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para el directorio de pacientes.
 */
@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Consulta y actualización del directorio de pacientes")
public class PacientesController {

    private final PacienteService pacienteService;

    public PacientesController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    @Operation(summary = "Obtener el directorio completo de pacientes")
    public List<Paciente> getAll() {
        return pacienteService.getTodosPacientes();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar los datos de contacto de un paciente")
    public ResponseEntity<Void> actualizar(@PathVariable int id, @RequestBody Paciente paciente) {
        Paciente actualizado = new Paciente(
                id, paciente.idUsuario(), paciente.nombre(), paciente.telefono(), paciente.email());
        boolean ok = pacienteService.actualizarPaciente(actualizado);
        return ok ? ResponseEntity.ok().build()
                  : ResponseEntity.notFound().build();
    }
}
