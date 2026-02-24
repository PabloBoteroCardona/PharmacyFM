package app;

import app.repository.PacienteRepository;
import app.service.FormulaService;
import app.service.PedidoService;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserWindow {

    private static final PacienteRepository pacienteRepository = new PacienteRepository();
    private static final FormulaService formulaService         = new FormulaService();
    private static final PedidoService pedidoService           = new PedidoService();

    private static String getCss() {
        return new java.io.File("src/resource/styles.css").toURI().toString();
    }

    public static void show(Stage stage, User user) {

        Paciente paciente = pacienteRepository.findByUserId(user.getId());
        if (paciente == null) {
            mostrarAlerta("No se ha encontrado la ficha de paciente asociada a este usuario.");
            LoginScreen.show(stage);
            return;
        }

        BorderPane root = new BorderPane();

        String nombre = (user.getNombre() != null && !user.getNombre().isEmpty())
                ? user.getNombre() : user.getEmail();

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

        TabPane tabPane = new TabPane();

        Tab tabSolicitar = new Tab("Solicitar pedido");
        tabSolicitar.setClosable(false);
        tabSolicitar.setContent(createSolicitarPedidoContent(paciente));

        Tab tabMisPedidos = new Tab("Mis pedidos");
        tabMisPedidos.setClosable(false);
        tabMisPedidos.setContent(createMisPedidosContent(paciente));

        tabPane.getTabs().addAll(tabSolicitar, tabMisPedidos);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getCss());
        stage.setTitle("PharmacyFM - Área de paciente");
        stage.setScene(scene);
        stage.show();
    }

    // =========================================================
    //             PESTAÑA "SOLICITAR PEDIDO"
    // =========================================================

    private static VBox createSolicitarPedidoContent(Paciente paciente) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Nueva solicitud de fórmula");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        RadioButton rbExistente    = new RadioButton("Seleccionar fórmula existente");
        RadioButton rbPersonalizada = new RadioButton("Fórmula personalizada");

        ToggleGroup grupo = new ToggleGroup();
        rbExistente.setToggleGroup(grupo);
        rbPersonalizada.setToggleGroup(grupo);
        rbExistente.setSelected(true);

        Label lblFormulaExistente = new Label("Fórmula:");
        ComboBox<Formula> cbFormulas = new ComboBox<>();
        cbFormulas.setItems(FXCollections.observableArrayList(formulaService.getAllFormulas()));
        cbFormulas.setMaxWidth(Double.MAX_VALUE);
        VBox boxExistente = new VBox(5, lblFormulaExistente, cbFormulas);

        Label lblFormulaPers = new Label("Nombre de la fórmula personalizada:");
        TextField txtFormulaPers = new TextField();
        VBox boxPers = new VBox(5, lblFormulaPers, txtFormulaPers);
        boxPers.setDisable(true);

        rbExistente.setOnAction(_ -> {
            boxExistente.setDisable(false);
            boxPers.setDisable(true);
        });

        rbPersonalizada.setOnAction(_ -> {
            boxExistente.setDisable(true);
            boxPers.setDisable(false);
        });

        Label lblCantidad = new Label("Cantidad:");
        TextField txtCantidad = new TextField("1");
        txtCantidad.setPrefWidth(80);

        ComboBox<String> cbUnidades = new ComboBox<>();
        cbUnidades.getItems().addAll("Unidades", "Cápsulas", "Gramos", "Mililitros", "Comprimidos");
        cbUnidades.setValue("Unidades");

        HBox hbCantidad = new HBox(10, txtCantidad, cbUnidades);
        hbCantidad.setAlignment(Pos.CENTER_LEFT);

        Label lblObs = new Label("Observaciones (ej. indicaciones del médico, aclaraciones):");
        TextArea txtObs = new TextArea();
        txtObs.setPrefRowCount(4);

        Button btnEnviar = new Button("Enviar solicitud");
        btnEnviar.getStyleClass().add("btn-primary");

        btnEnviar.setOnAction(event -> {
            if (event == null) return;

            int cantidad;
            try {
                cantidad = Integer.parseInt(txtCantidad.getText().trim());
            } catch (NumberFormatException ex) {
                mostrarAlerta("La cantidad no es válida.");
                return;
            }
            if (cantidad <= 0) {
                mostrarAlerta("La cantidad debe ser mayor que 0.");
                return;
            }

            String unidad        = cbUnidades.getValue();
            String observaciones = txtObs.getText().trim();
            boolean ok;

            if (rbExistente.isSelected()) {
                Formula sel = cbFormulas.getValue();
                if (sel == null) {
                    mostrarAlerta("Debes seleccionar una fórmula del listado.");
                    return;
                }
                ok = pedidoService.crearPedidoFormulaCatalogo(
                        paciente.getId(), sel.getId(), cantidad, unidad, observaciones);
            } else {
                String nombrePers = txtFormulaPers.getText().trim();
                if (nombrePers.isEmpty()) {
                    mostrarAlerta("Debes escribir el nombre de la fórmula personalizada.");
                    return;
                }
                ok = pedidoService.crearPedidoFormulaPersonalizada(
                        paciente.getId(), nombrePers, cantidad, unidad, observaciones);
            }

            if (ok) {
                mostrarAlerta("Solicitud enviada correctamente.");
                txtCantidad.setText("1");
                cbUnidades.setValue("Unidades");
                txtObs.clear();
                txtFormulaPers.clear();
                cbFormulas.getSelectionModel().clearSelection();
                rbExistente.setSelected(true);
                boxExistente.setDisable(false);
                boxPers.setDisable(true);
            } else {
                mostrarAlerta("Error al enviar la solicitud.");
            }
        });

        HBox opciones = new HBox(15, rbExistente, rbPersonalizada);
        opciones.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(
                title,
                opciones,
                boxExistente,
                boxPers,
                lblCantidad, hbCantidad,
                lblObs, txtObs,
                btnEnviar
        );

        return root;
    }

    // =========================================================
    //               PESTAÑA "MIS PEDIDOS"
    // =========================================================

    private static VBox createMisPedidosContent(Paciente paciente) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Mis pedidos");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<Pedido> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Pedido, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        TableColumn<Pedido, String> colFormula = new TableColumn<>("Fórmula");
        colFormula.setCellValueFactory(new PropertyValueFactory<>("nombreFormula"));

        // Vinculamos a getCantidadConUnidad() en lugar de getCantidad()
        TableColumn<Pedido, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadConUnidad"));

        TableColumn<Pedido, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Pedido, String> colObs = new TableColumn<>("Observaciones");
        colObs.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        table.getColumns().add(colFecha);
        table.getColumns().add(colFormula);
        table.getColumns().add(colCantidad);
        table.getColumns().add(colEstado);
        table.getColumns().add(colObs);

        ObservableList<Pedido> data = FXCollections.observableArrayList();
        cargarPedidosPaciente(data, paciente);
        table.setItems(data);

        Button btnRecargar = new Button("Actualizar");
        btnRecargar.getStyleClass().add("btn-secondary");
        btnRecargar.setOnAction(_ -> cargarPedidosPaciente(data, paciente));

        root.getChildren().addAll(title, table, btnRecargar);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    private static void cargarPedidosPaciente(ObservableList<Pedido> data, Paciente paciente) {
        data.clear();
        List<Pedido> lista = pedidoService.getPedidosByPaciente(paciente.getId());
        data.addAll(lista);
    }

    private static void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}