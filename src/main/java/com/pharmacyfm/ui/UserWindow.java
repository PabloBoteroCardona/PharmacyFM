package com.pharmacyfm.ui;

import com.pharmacyfm.AppContext;
import com.pharmacyfm.service.PacienteService;
import com.pharmacyfm.ui.panels.MisPedidosPanel;
import com.pharmacyfm.ui.panels.SolicitarPedidoPanel;
import com.pharmacyfm.domain.model.Paciente;
import com.pharmacyfm.domain.model.User;

import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Ventana del área privada del paciente.
 *
 * Actúa como cáscara de navegación: resuelve el perfil de paciente
 * del usuario autenticado y monta las pestañas de solicitud y seguimiento.
 * Toda la lógica de cada sección vive en su panel correspondiente
 * (SolicitarPedidoPanel, MisPedidosPanel).
 */
public class UserWindow {

    /** Servicio de pacientes — necesario para resolver el perfil antes de montar la UI. */
    private static final PacienteService pacienteService = AppContext.get().pacienteService();

    /**
     * Configura y despliega el área de paciente en el escenario dado.
     * Si el usuario no tiene perfil de paciente se muestra una alerta
     * y se redirige a la pantalla de inicio de sesión.
     *
     * @param stage Escenario principal de la aplicación.
     * @param user  Usuario paciente autenticado.
     */
    public static void show(Stage stage, User user) {

        // Resolvemos el perfil de paciente antes de construir la UI
        Optional<Paciente> optPaciente = pacienteService.getPacientePorUsuario(user.id());
        if (optPaciente.isEmpty()) {
            AlertHelper.mostrarAlerta(
                    "No se ha encontrado la ficha de paciente asociada a este usuario.");
            LoginScreen.show(stage);
            return;
        }
        Paciente paciente = optPaciente.get();

        BorderPane root = new BorderPane();

        // ---- Encabezado personalizado ----
        // Usamos el nombre si existe, o el email como fallback
        String nombre = (user.nombre() != null && !user.nombre().isEmpty())
                ? user.nombre() : user.email();

        Label lblWelcome = new Label("Bienvenido/a, " + nombre + " (Paciente)");
        lblWelcome.getStyleClass().add("top-bar-title");

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.getStyleClass().add("btn-logout");
        btnLogout.setOnAction(_ -> LoginScreen.show(stage));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, lblWelcome, spacer, btnLogout);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        // ---- Pestañas de navegación — el contenido lo construye cada panel ----
        TabPane tabPane = new TabPane();

        Tab tabSolicitar = new Tab("Solicitar pedido");
        tabSolicitar.setClosable(false);
        tabSolicitar.setContent(SolicitarPedidoPanel.build(paciente));

        Tab tabMisPedidos = new Tab("Mis pedidos");
        tabMisPedidos.setClosable(false);
        tabMisPedidos.setContent(MisPedidosPanel.build(paciente));

        tabPane.getTabs().addAll(tabSolicitar, tabMisPedidos);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(
                UserWindow.class.getResource("/styles.css").toExternalForm());
        stage.setTitle("PharmacyFM - Área de paciente");
        stage.setScene(scene);
        stage.show();
    }
}
