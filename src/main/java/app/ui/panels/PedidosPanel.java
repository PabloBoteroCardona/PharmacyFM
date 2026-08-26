package app.ui.panels;

import app.AppContext;
import app.service.PedidoService;
import app.ui.AlertHelper;
import com.pharmacyfm.domain.model.Pedido;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Panel de gestión de pedidos para el área de administración.
 *
 * Muestra todos los pedidos del sistema en una tabla y permite al
 * administrador cambiar el estado de un pedido seleccionado mediante
 * un diálogo modal.
 *
 * Las columnas usan lambdas como cell value factories para compatibilidad
 * con el modelo Pedido definido como Java record.
 *
 * No instanciable: el método build() es el único punto de entrada.
 */
public final class PedidosPanel {

    /** Servicio de pedidos obtenido de la raíz de composición. */
    private static final PedidoService pedidoService = AppContext.get().pedidoService();

    private PedidosPanel() {}

    /**
     * Construye y devuelve el panel completo de gestión de pedidos.
     *
     * @return VBox con la tabla y los controles de administración de pedidos.
     */
    public static VBox build() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Gestión de pedidos");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<Pedido> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Columnas con lambdas: compatibles con el record Pedido (accesores sin prefijo "get")
        TableColumn<Pedido, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().fecha()));

        TableColumn<Pedido, String> colPaciente = new TableColumn<>("Paciente");
        colPaciente.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nombrePaciente()));

        TableColumn<Pedido, String> colFormula = new TableColumn<>("Fórmula");
        colFormula.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nombreFormula()));

        TableColumn<Pedido, String> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().cantidadConUnidad()));

        TableColumn<Pedido, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().estado().getLabel()));

        TableColumn<Pedido, String> colObs = new TableColumn<>("Observaciones");
        colObs.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().observaciones()));

        table.getColumns().addAll(List.of(colFecha, colPaciente, colFormula, colCant, colEstado, colObs));

        ObservableList<Pedido> data = FXCollections.observableArrayList();
        cargarPedidos(data);
        table.setItems(data);

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

    /**
     * Abre un diálogo modal para cambiar el estado del pedido seleccionado en la tabla.
     * Si no hay ningún pedido seleccionado muestra una alerta informativa.
     */
    private static void mostrarCambiarEstadoDialog(TableView<Pedido> table, ObservableList<Pedido> data) {
        Pedido selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.mostrarAlerta("Selecciona un pedido.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Cambiar estado del pedido");

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.setMaxWidth(Double.MAX_VALUE);
        cbEstado.getItems().addAll("Pendiente", "En preparación", "Listo", "Entregado", "Cancelado");
        // El estado se muestra como texto legible a partir del enum tipado
        cbEstado.setValue(selected.estado().getLabel());

        Button btnGuardar  = new Button("Guardar");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            boolean ok = pedidoService.actualizarEstado(selected.id(), cbEstado.getValue());
            if (ok) {
                AlertHelper.mostrarAlerta("Estado actualizado.");
                cargarPedidos(data);
                dialog.close();
            } else {
                AlertHelper.mostrarAlerta("Error guardando el estado.");
            }
        });

        btnCancelar.setOnAction(_ -> dialog.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10, new Label("Estado del pedido:"), cbEstado, botones);
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 300, 160);
        scene.getStylesheets().add(AlertHelper.getCss());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /** Recarga la lista observable consultando al servicio de pedidos. */
    private static void cargarPedidos(ObservableList<Pedido> data) {
        data.clear();
        data.addAll(pedidoService.getAllPedidos());
    }
}
