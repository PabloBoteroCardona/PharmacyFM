package app.service;

import com.pharmacyfm.domain.model.Paciente;
import com.pharmacyfm.domain.port.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de PacienteService con Mockito.
 *
 * PacienteRepository se sustituye por un mock; las pruebas verifican
 * únicamente la lógica de negocio del servicio: validaciones, delegación
 * al repositorio y manejo de valores no encontrados mediante Optional.
 *
 * Gracias a la inyección por constructor, el mock se pasa directamente
 * sin necesidad de reflexión ni anotaciones especiales de Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteService — tests unitarios con mock")
class PacienteServiceTest {

    /**
     * Mock del repositorio: simulamos el acceso a datos sin BD real.
     * Mockito lo inicializa automáticamente gracias a @Mock + @ExtendWith.
     */
    @Mock
    private PacienteRepository pacienteRepository;

    /** Servicio bajo prueba, recibe el mock por constructor. */
    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        // Inyectamos el mock directamente mediante el constructor
        pacienteService = new PacienteService(pacienteRepository);
    }

    // =========================================================
    // getTodosPacientes
    // =========================================================

    @Test
    @DisplayName("getTodosPacientes delega en el repositorio y devuelve su resultado")
    void getTodosPacientes_delegaEnRepositorio() {
        // Definimos el comportamiento simulado del mock
        List<Paciente> listaMock = List.of(
                new Paciente(1, 10, "Ana García",   "600111222", "ana@test.com"),
                new Paciente(2, 11, "Carlos López", "600333444", "carlos@test.com")
        );
        when(pacienteRepository.findAll()).thenReturn(listaMock);

        // Ejecutamos el método bajo prueba
        List<Paciente> resultado = pacienteService.getTodosPacientes();

        // Verificamos el resultado y que el repositorio fue llamado exactamente una vez
        assertEquals(2, resultado.size(), "Debe devolver los 2 pacientes del mock");
        verify(pacienteRepository, times(1)).findAll();
    }

    // =========================================================
    // getPacientePorUsuario
    // =========================================================

    @Test
    @DisplayName("getPacientePorUsuario devuelve Optional con el paciente si existe")
    void getPacientePorUsuario_existente_devuelveOptionalConPaciente() {
        Paciente p = new Paciente(5, 20, "María Ruiz", "600555666", "maria@test.com");
        when(pacienteRepository.findByUserId(20)).thenReturn(p);

        Optional<Paciente> resultado = pacienteService.getPacientePorUsuario(20);

        assertTrue(resultado.isPresent(), "El Optional debe contener un paciente");
        assertEquals("María Ruiz", resultado.get().nombre());
    }

    @Test
    @DisplayName("getPacientePorUsuario devuelve Optional vacío si no existe el paciente")
    void getPacientePorUsuario_noExistente_devuelveOptionalVacio() {
        // El repositorio devuelve null cuando no encuentra el paciente
        when(pacienteRepository.findByUserId(99)).thenReturn(null);

        Optional<Paciente> resultado = pacienteService.getPacientePorUsuario(99);

        assertTrue(resultado.isEmpty(), "El Optional debe estar vacío si el paciente no existe");
    }

    // =========================================================
    // actualizarPaciente — validaciones
    // =========================================================

    @Test
    @DisplayName("actualizarPaciente con ID 0 devuelve false sin llamar al repositorio")
    void actualizarPaciente_idCero_devuelveFalseSinLlamarRepositorio() {
        Paciente invalido = new Paciente(0, 1, "Test", "600000000", "t@t.com");

        boolean resultado = pacienteService.actualizarPaciente(invalido);

        assertFalse(resultado, "ID 0 no es un paciente persistido válido");
        // El repositorio no debe ser invocado si la validación falla antes
        verify(pacienteRepository, never()).update(any());
    }

    @Test
    @DisplayName("actualizarPaciente con nombre vacío devuelve false sin llamar al repositorio")
    void actualizarPaciente_nombreVacio_devuelveFalseSinLlamarRepositorio() {
        Paciente invalido = new Paciente(3, 1, "   ", "600000000", "t@t.com");

        boolean resultado = pacienteService.actualizarPaciente(invalido);

        assertFalse(resultado, "El nombre no puede ser solo espacios en blanco");
        verify(pacienteRepository, never()).update(any());
    }

    @Test
    @DisplayName("actualizarPaciente válido delega en el repositorio y devuelve su resultado")
    void actualizarPaciente_valido_delegaEnRepositorio() {
        Paciente valido = new Paciente(3, 1, "Luis Fernández", "600777888", "luis@test.com");
        when(pacienteRepository.update(valido)).thenReturn(true);

        boolean resultado = pacienteService.actualizarPaciente(valido);

        assertTrue(resultado, "La actualización debe reportar éxito");
        // Verificamos que el repositorio fue invocado con el objeto correcto
        verify(pacienteRepository, times(1)).update(valido);
    }

    @Test
    @DisplayName("actualizarPaciente devuelve false si el repositorio no encuentra la fila")
    void actualizarPaciente_noEncontradoEnBD_devuelveFalse() {
        Paciente p = new Paciente(999, 1, "Desconocido", "600000000", "x@x.com");
        when(pacienteRepository.update(p)).thenReturn(false);

        boolean resultado = pacienteService.actualizarPaciente(p);

        assertFalse(resultado, "Debe devolver false si la BD no actualizó ninguna fila");
    }
}
