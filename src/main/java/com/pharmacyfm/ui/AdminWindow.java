package com.pharmacyfm.ui;

import com.pharmacyfm.ui.panels.FormulasPanel;
import com.pharmacyfm.ui.panels.PacientesPanel;
import com.pharmacyfm.ui.panels.PedidosPanel;
import com.pharmacyfm.domain.model.User;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Ventana principal del administrador.
 *
 * Actúa como cáscara de navegación: monta la barra superior y las pestañas
 * de Pedidos, Fórmulas y Pacientes. Toda la lógica de cada sección vive
 * en su panel correspondiente (PedidosPanel, FormulasPanel, PacientesPanel).
 */
public class AdminWindow {

    /**
     * Configura y despliega la ventana del administrador en el escenario dado.
     *
     * @param stage Escenario principal de la aplicación.
     * @param user  Usuario administrador autenticado.
     */
    public static void show(Stage stage, User user) {

        BorderPane root = new BorderPane();

        // ---- Barra superior con saludo y cierre de sesión ----
        Label lblWelcome = new Label("Panel Administrador - " + user.nombre());
        lblWelcome.getStyleClass().add("top-bar-title");

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.getStyleClass().add("btn-logout");
        btnLogout.setOnAction(_ -> LoginScreen.show(stage));

        // Espaciador para empujar el botón de logout a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, lblWelcome, spacer, btnLogout);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        // ---- Pestañas de navegación — el contenido lo construye cada panel ----
        TabPane tabs = new TabPane();

        Tab tabPedidos = new Tab("Pedidos");
        tabPedidos.setClosable(false);
        tabPedidos.setContent(PedidosPanel.build());

        Tab tabFormulas = new Tab("Fórmulas");
        tabFormulas.setClosable(false);
        tabFormulas.setContent(FormulasPanel.build());

        Tab tabPacientes = new Tab("Pacientes");
        tabPacientes.setClosable(false);
        tabPacientes.setContent(PacientesPanel.build());

        tabs.getTabs().addAll(tabPedidos, tabFormulas, tabPacientes);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(
                AdminWindow.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("PharmacyFM - Administrador");
        stage.show();
    }
}
