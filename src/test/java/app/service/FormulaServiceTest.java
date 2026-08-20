package app.service;

import com.pharmacyfm.domain.model.Formula;
import com.pharmacyfm.domain.port.FormulaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de FormulaService con Mockito.
 *
 * FormulaRepository se sustituye por un mock; las pruebas verifican
 * únicamente la lógica de negocio del servicio: validaciones de nombre
 * y precio, y la bifurcación insert/update según Formula.isNew().
 *
 * Gracias a la inyección por constructor, el mock se pasa directamente
 * sin necesidad de reflexión ni anotaciones especiales de Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FormulaService — tests unitarios con mock")
class FormulaServiceTest {

    /** Mock del repositorio de fórmulas. */
    @Mock
    private FormulaRepository formulaRepository;

    /** Servicio bajo prueba, recibe el mock por constructor. */
    private FormulaService formulaService;

    @BeforeEach
    void setUp() {
        formulaService = new FormulaService(formulaRepository);
    }

    // =========================================================
    // getAllFormulas
    // =========================================================

    @Test
    @DisplayName("getAllFormulas delega en el repositorio y devuelve su resultado")
    void getAllFormulas_delegaEnRepositorio() {
        List<Formula> listaMock = List.of(
                new Formula(1, "Vitamina C", "Suplemento", 10.0),
                new Formula(2, "Ibuprofeno", "Antiinflamatorio", 8.5)
        );
        when(formulaRepository.findAll()).thenReturn(listaMock);

        List<Formula> resultado = formulaService.getAllFormulas();

        assertEquals(2, resultado.size(), "Debe devolver las 2 fórmulas del mock");
        verify(formulaRepository, times(1)).findAll();
    }

    // =========================================================
    // guardarFormula — validaciones
    // =========================================================

    @Test
    @DisplayName("guardarFormula con nombre nulo devuelve false sin llamar al repositorio")
    void guardarFormula_nombreNulo_devuelveFalse() {
        Formula invalida = new Formula(0, null, "desc", 5.0);

        boolean resultado = formulaService.guardarFormula(invalida);

        assertFalse(resultado, "Nombre nulo no es válido");
        verify(formulaRepository, never()).insert(any());
        verify(formulaRepository, never()).update(any());
    }

    @Test
    @DisplayName("guardarFormula con nombre vacío devuelve false sin llamar al repositorio")
    void guardarFormula_nombreVacio_devuelveFalse() {
        Formula invalida = new Formula(0, "   ", "desc", 5.0);

        boolean resultado = formulaService.guardarFormula(invalida);

        assertFalse(resultado, "Nombre solo con espacios no es válido");
        verify(formulaRepository, never()).insert(any());
    }

    @Test
    @DisplayName("guardarFormula con precio negativo devuelve false sin llamar al repositorio")
    void guardarFormula_precioNegativo_devuelveFalse() {
        Formula invalida = new Formula(0, "Fórmula X", "desc", -1.0);

        boolean resultado = formulaService.guardarFormula(invalida);

        assertFalse(resultado, "Precio negativo no es válido");
        verify(formulaRepository, never()).insert(any());
    }

    // =========================================================
    // guardarFormula — bifurcación insert / update
    // =========================================================

    @Test
    @DisplayName("guardarFormula nueva (id=0) delega en insert y devuelve true si tiene éxito")
    void guardarFormula_nueva_delegaEnInsert() {
        Formula nueva = new Formula("Gel hidratante", "Aloe vera 10%", 12.0);
        // id == 0 → isNew() devuelve true → debe llamar a insert
        when(formulaRepository.insert(nueva)).thenReturn(5);

        boolean resultado = formulaService.guardarFormula(nueva);

        assertTrue(resultado, "Insert exitoso debe devolver true");
        verify(formulaRepository, times(1)).insert(nueva);
        verify(formulaRepository, never()).update(any());
    }

    @Test
    @DisplayName("guardarFormula existente (id>0) delega en update y devuelve true si tiene éxito")
    void guardarFormula_existente_delegaEnUpdate() {
        Formula existente = new Formula(3, "Pomada base", "Petrolato simple", 7.0);
        // id == 3 → isNew() devuelve false → debe llamar a update
        when(formulaRepository.update(existente)).thenReturn(true);

        boolean resultado = formulaService.guardarFormula(existente);

        assertTrue(resultado, "Update exitoso debe devolver true");
        verify(formulaRepository, never()).insert(any());
        verify(formulaRepository, times(1)).update(existente);
    }

    // =========================================================
    // eliminarFormula
    // =========================================================

    @Test
    @DisplayName("eliminarFormula delega en delete y devuelve el resultado del repositorio")
    void eliminarFormula_delegaEnDelete() {
        when(formulaRepository.delete(7)).thenReturn(true);

        boolean resultado = formulaService.eliminarFormula(7);

        assertTrue(resultado, "Eliminación exitosa debe devolver true");
        verify(formulaRepository, times(1)).delete(7);
    }
}
