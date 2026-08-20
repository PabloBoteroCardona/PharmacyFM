package app.ui.panels;

import app.AppContext;
import app.service.FormulaService;
import app.service.PedidoService;
import app.ui.AlertHelper;
import com.pharmacyfm.domain.model.Formula;
import com.pharmacyfm.domain.model.Paciente;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Panel de solicitud de nuevas fórmulas magistrales para el área del paciente.
 *
 * Permite al paciente elegir entre una fórmula del catálogo o especificar
 * una fórmula personalizada con su nombre, cantidad, unidad y observaciones.
 * Ambas rutas delegan en el servicio de pedidos para la persistencia.
 *
 * No instanciable: el método build(Paciente) es el único punto de entrada.
 */
public final class SolicitarPedidoPanel {

    private static final FormulaService formulaService = AppContext.get().formulaService();
    private static final PedidoService  pedidoService  = AppContext.get().pedidoService();

    private SolicitarPedidoPanel() {}

    /**
     * Construye y devuelve el panel de solicitud de pedido para un paciente concreto.
     *
     * @param paciente Paciente autenticado cuyo id se usará para el pedido.
     * @return VBox con el formulario de solicitud de fórmula.
     */
    public static VBox build(Paciente paciente) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Nueva solicitud de fórmula");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Selección del tipo de fórmula: del catálogo o personalizada
        RadioButton rbExistente    = new RadioButton("Seleccionar fórmula existente");
        RadioButton rbPersonalizada = new RadioButton("Fórmula personalizada");

        ToggleGroup grupo = new ToggleGroup();
        rbExistente.setToggleGroup(grupo);
        rbPersonalizada.setToggleGroup(grupo);
        rbExistente.setSelected(true);

        // Sección para seleccionar una fórmula del catálogo
        Label lblFormulaExistente = new Label("Fórmula:");
        ComboBox<Formula> cbFormulas = new ComboBox<>();
        cbFormulas.setItems(FXCollections.observableArrayList(formulaService.getAllFormulas()));
        cbFormulas.setMaxWidth(Double.MAX_VALUE);
        VBox boxExistente = new VBox(5, lblFormulaExistente, cbFormulas);

        // Sección para indicar el nombre de una fórmula no catalogada
        Label lblFormulaPers = new Label("Nombre de la fórmula personalizada:");
        TextField txtFormulaPers = new TextField();
        VBox boxPers = new VBox(5, lblFormulaPers, txtFormulaPers);
        boxPers.setDisable(true);

        // Intercambiar secciones al cambiar el tipo de pedido
        rbExistente.setOnAction(_ -> { boxExistente.setDisable(false); boxPers.setDisable(true); });
        rbPersonalizada.setOnAction(_ -> { boxExistente.setDisable(true); boxPers.setDisable(false); });

        // Campos de cantidad y unidad
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
                AlertHelper.mostrarAlerta("La cantidad no es válida.");
                return;
            }
            if (cantidad <= 0) {
                AlertHelper.mostrarAlerta("La cantidad debe ser mayor que 0.");
                return;
            }

            String unidad        = cbUnidades.getValue();
            String observaciones = txtObs.getText().trim();
            boolean ok;

            // Delegación al servicio según el tipo de pedido seleccionado
            if (rbExistente.isSelected()) {
                Formula sel = cbFormulas.getValue();
                if (sel == null) {
                    AlertHelper.mostrarAlerta("Debes seleccionar una fórmula del listado.");
                    return;
                }
                ok = pedidoService.crearPedidoFormulaCatalogo(
                        paciente.id(), sel.id(), cantidad, unidad, observaciones);
            } else {
                String nombrePers = txtFormulaPers.getText().trim();
                if (nombrePers.isEmpty()) {
                    AlertHelper.mostrarAlerta("Debes escribir el nombre de la fórmula personalizada.");
                    return;
                }
                ok = pedidoService.crearPedidoFormulaPersonalizada(
                        paciente.id(), nombrePers, cantidad, unidad, observaciones);
            }

            // Feedback al paciente y limpieza del formulario tras el envío exitoso
            if (ok) {
                AlertHelper.mostrarAlerta("Solicitud enviada correctamente.");
                txtCantidad.setText("1");
                cbUnidades.setValue("Unidades");
                txtObs.clear();
                txtFormulaPers.clear();
                cbFormulas.getSelectionModel().clearSelection();
                rbExistente.setSelected(true);
                boxExistente.setDisable(false);
                boxPers.setDisable(true);
            } else {
                AlertHelper.mostrarAlerta("Error al enviar la solicitud.");
            }
        });

        HBox opciones = new HBox(15, rbExistente, rbPersonalizada);
        opciones.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, opciones, boxExistente, boxPers,
                lblCantidad, hbCantidad, lblObs, txtObs, btnEnviar);
        return root;
    }
}
