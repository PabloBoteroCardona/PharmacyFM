package app;

import app.repository.PacienteRepository;
import app.service.FormulaService;
import app.service.PedidoService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminWindow {

    // Inicialización de servicios y repositorios para la gestión de datos
    private static final FormulaService formulaService         = new FormulaService();
    private static final PedidoService pedidoService           = new PedidoService();
    private static final PacienteRepository pacienteRepository = new PacienteRepository();

    // Método para cargar la hoja de estilos personalizada
    private static String getCss() {
        return new java.io.File("src/resource/styles.css").toURI().toString();
    }

    // Configuración y despliegue de la ventana principal del administrador
    public static void show(Stage stage, User user) {

        BorderPane root = new BorderPane();

        // ---- Barra superior con saludo y cierre de sesión ----
        Label lblWelcome = new Label("Panel Administrador - " + user.getNombre());
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

        // ---- Configuración de las pestañas de navegación ----
        javafx.scene.control.TabPane tabs = new javafx.scene.control.TabPane();

        // Pestaña de Pedidos
        javafx.scene.control.Tab tabPedidos = new javafx.scene.control.Tab("Pedidos");
        tabPedidos.setClosable(false);
        tabPedidos.setContent(createPedidosContent());

        // Pestaña de Fórmulas
        javafx.scene.control.Tab tabFormulas = new javafx.scene.control.Tab("Fórmulas");
        tabFormulas.setClosable(false);
        tabFormulas.setContent(createFormulasContent());

        // Pestaña de Pacientes
        javafx.scene.control.Tab tabPacientes = new javafx.scene.control.Tab("Pacientes");
        tabPacientes.setClosable(false);
        tabPacientes.setContent(createPacientesContent());

        tabs.getTabs().addAll(tabPedidos, tabFormulas, tabPacientes);
        root.setCenter(tabs);

        // Visualización de la escena principal
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(getCss());
        stage.setScene(scene);
        stage.setTitle("PharmacyFM - Administrador");
        stage.show();
    }

    // ============================================================
    // SECCIÓN: GESTIÓN DE PEDIDOS
    // ============================================================

    private static VBox createPedidosContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Gestión de pedidos");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Definición de la tabla de pedidos y sus columnas
        TableView<Pedido> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Pedido, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        TableColumn<Pedido, String> colPaciente = new TableColumn<>("Paciente");
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));

        TableColumn<Pedido, String> colFormula = new TableColumn<>("Fórmula");
        colFormula.setCellValueFactory(new PropertyValueFactory<>("nombreFormula"));

        TableColumn<Pedido, String> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidadConUnidad"));

        TableColumn<Pedido, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Pedido, String> colObs = new TableColumn<>("Observaciones");
        colObs.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        table.getColumns().add(colFecha);
        table.getColumns().add(colPaciente);
        table.getColumns().add(colFormula);
        table.getColumns().add(colCant);
        table.getColumns().add(colEstado);
        table.getColumns().add(colObs);

        // Carga inicial de datos en la tabla
        ObservableList<Pedido> data = FXCollections.observableArrayList();
        cargarPedidos(data);
        table.setItems(data);

        // Botones de control para la sección de pedidos
        Button btnActualizarEstado = new Button("Cambiar estado");
        btnActualizarEstado.getStyleClass().add("btn-primary");

        Button btnRecargar = new Button("Actualizar");
        btnRecargar.getStyleClass().add("btn-secondary");

        btnActualizarEstado.setOnAction(_ -> mostrarCambiarEstadoDialog(table, data));
        btnRecargar.setOnAction(_ -> cargarPedidos(data));

        HBox botones = new HBox(10, btnActualizarEstado, btnRecargar);
        botones.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, table, botones);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }
    
    // Abre un diálogo modal para modificar el estado del pedido seleccionado
    private static void mostrarCambiarEstadoDialog(TableView<Pedido> table, ObservableList<Pedido> data) {
        Pedido selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Selecciona un pedido.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Cambiar estado del pedido");

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.setMaxWidth(Double.MAX_VALUE);
        cbEstado.getItems().addAll("Pendiente", "En preparación", "Listo", "Entregado", "Cancelado");
        cbEstado.setValue(selected.getEstado());

        Button btnGuardar  = new Button("Guardar");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        // Acción de guardado y refresco de la tabla
        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            boolean ok = pedidoService.actualizarEstado(selected.getId(), cbEstado.getValue());
            if (ok) {
                mostrarAlerta("Estado actualizado.");
                cargarPedidos(data);
                dialog.close();
            } else {
                mostrarAlerta("Error guardando el estado.");
            }
        });

        btnCancelar.setOnAction(_ -> dialog.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10, new Label("Estado del pedido:"), cbEstado, botones);
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 300, 160);
        scene.getStylesheets().add(getCss());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Refresca la información de pedidos consultando al servicio
    private static void cargarPedidos(ObservableList<Pedido> data) {
        data.clear();
        data.addAll(pedidoService.getAllPedidos());
    }

    // ============================================================
    // SECCIÓN: GESTIÓN DE FÓRMULAS
    // ============================================================

    private static VBox createFormulasContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Gestión de Fórmulas Magistrales");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Tabla de fórmulas con columnas vinculadas a los atributos del objeto Formula
        TableView<Formula> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Formula, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Formula, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        TableColumn<Formula, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        table.getColumns().add(colNombre);
        table.getColumns().add(colDesc);
        table.getColumns().add(colPrecio);

        ObservableList<Formula> data = FXCollections.observableArrayList(formulaService.getAllFormulas());
        table.setItems(data);

        // Botones para operaciones CRUD de fórmulas
        Button btnAdd  = new Button("Añadir");
        btnAdd.getStyleClass().add("btn-primary");

        Button btnEdit = new Button("Editar");
        btnEdit.getStyleClass().add("btn-secondary");

        Button btnDel  = new Button("Eliminar");
        btnDel.getStyleClass().add("btn-secondary");

        btnAdd.setOnAction(_ -> mostrarDialogFormula(null, data));

        btnEdit.setOnAction(_ -> {
            Formula sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogFormula(sel, data);
            else mostrarAlerta("Selecciona una fórmula.");
        });

        btnDel.setOnAction(_ -> {
            Formula sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                boolean ok = formulaService.eliminarFormula(sel.getId());
                if (ok) data.setAll(formulaService.getAllFormulas());
                else mostrarAlerta("No se pudo eliminar la fórmula.");
            } else {
                mostrarAlerta("Selecciona una fórmula.");
            }
        });

        HBox botones = new HBox(10, btnAdd, btnEdit, btnDel);
        botones.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, table, botones);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    // Maneja la creación y edición de fórmulas mediante un diálogo común
    private static void mostrarDialogFormula(Formula formulaOriginal, ObservableList<Formula> data) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(formulaOriginal == null ? "Nueva fórmula" : "Editar fórmula");

        final Formula formula = (formulaOriginal == null) ? new Formula("", "", 0.0) : formulaOriginal;

        TextField txtNombre      = new TextField(formula.getNombre());
        txtNombre.setMaxWidth(Double.MAX_VALUE);

        TextField txtDescripcion = new TextField(formula.getDescripcion());
        txtDescripcion.setMaxWidth(Double.MAX_VALUE);

        TextField txtPrecio      = new TextField(String.valueOf(formula.getPrecio()));
        txtPrecio.setMaxWidth(Double.MAX_VALUE);

        Button btnGuardar  = new Button("Guardar");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            try {
                // Validación de entrada numérica para el precio
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                formula.setNombre(txtNombre.getText().trim());
                formula.setDescripcion(txtDescripcion.getText().trim());
                formula.setPrecio(precio);
                
                boolean ok = formulaService.guardarFormula(formula);
                if (ok) {
                    data.setAll(formulaService.getAllFormulas());
                    dlg.close();
                } else {
                    mostrarAlerta("Error guardando la fórmula.");
                }
            } catch (NumberFormatException ex) {
                mostrarAlerta("Precio inválido. Usa un número, por ejemplo 12.5");
            }
        });

        btnCancelar.setOnAction(_ -> dlg.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10,
                new Label("Nombre:"), txtNombre,
                new Label("Descripción:"), txtDescripcion,
                new Label("Precio:"), txtPrecio,
                botones
        );
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 350, 280);
        scene.getStylesheets().add(getCss());
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    // ============================================================
    // SECCIÓN: GESTIÓN DE PACIENTES
    // ============================================================

    private static VBox createPacientesContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Gestión de Pacientes");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Tabla de pacientes con columnas de información de contacto
        TableView<Paciente> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Paciente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Paciente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Paciente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        table.getColumns().add(colNombre);
        table.getColumns().add(colEmail);
        table.getColumns().add(colTelefono);

        ObservableList<Paciente> data =
                FXCollections.observableArrayList(pacienteRepository.findAll());
        table.setItems(data);

        Button btnEditar   = new Button("Editar datos");
        btnEditar.getStyleClass().add("btn-primary");

        Button btnRecargar = new Button("Actualizar");
        btnRecargar.getStyleClass().add("btn-secondary");

        btnEditar.setOnAction(_ -> {
            Paciente sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogEditarPaciente(sel, data);
            else mostrarAlerta("Selecciona un paciente.");
        });

        btnRecargar.setOnAction(_ -> data.setAll(pacienteRepository.findAll()));

        HBox botones = new HBox(10, btnEditar, btnRecargar);
        botones.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, table, botones);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    // Formulario para la actualización de datos personales del paciente
    private static void mostrarDialogEditarPaciente(Paciente p, ObservableList<Paciente> data) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Editar datos del paciente");

        TextField txtNombre   = new TextField(p.getNombre());
        TextField txtEmail    = new TextField(p.getEmail());
        TextField txtTelefono = new TextField(p.getTelefono());

        Button btnGuardar  = new Button("Guardar");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            p.setNombre(txtNombre.getText().trim());
            p.setEmail(txtEmail.getText().trim());
            p.setTelefono(txtTelefono.getText().trim());
            
            boolean ok = pacienteRepository.update(p);
            if (ok) {
                data.setAll(pacienteRepository.findAll());
                dlg.close();
            } else {
                mostrarAlerta("Error guardando los cambios.");
            }
        });

        btnCancelar.setOnAction(_ -> dlg.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10,
                new Label("Nombre:"), txtNombre,
                new Label("Email:"), txtEmail,
                new Label("Teléfono:"), txtTelefono,
                botones
        );
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 350, 260);
        scene.getStylesheets().add(getCss());
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    // ============================================================
    // UTILIDADES GENERALES
    // ============================================================

    // Centraliza la creación de ventanas de aviso para feedback del sistema
    private static void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}