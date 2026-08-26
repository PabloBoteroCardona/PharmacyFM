package com.pharmacyfm.ui.panels;

import com.pharmacyfm.AppContext;
import com.pharmacyfm.service.PedidoService;
import com.pharmacyfm.domain.model.Paciente;
import com.pharmacyfm.domain.model.Pedido;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Panel de seguimiento de pedidos para el área del paciente.
 *
 * Muestra la tabla con el historial de pedidos del paciente autenticado,
 * con su estado actual. El paciente puede recargar la lista para ver
 * las actualizaciones de estado realizadas por el administrador.
 *
 * Las columnas usan lambdas como cell value factories para compatibilidad
 * con el modelo Pedido definido como Java record.
 *
 * No instanciable: el método build(Paciente) es el único punto de entrada.
 */
public final class MisPedidosPanel {

    /** Servicio de pedidos obtenido de la raíz de composición. */
    private static final PedidoService pedidoService = AppContext.get().pedidoService();

    private MisPedidosPanel() {}

    /**
     * Construye y devuelve el panel de historial de pedidos del paciente.
     *
     * @param paciente Paciente autenticado cuyos pedidos se muestran.
     * @return VBox con la tabla de pedidos y el botón de actualizar.
     */
    public static VBox build(Paciente paciente) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Mis pedidos");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<Pedido> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Columnas con lambdas: compatibles con el record Pedido (accesores sin prefijo "get")
        TableColumn<Pedido, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().fecha()));

        TableColumn<Pedido, String> colFormula = new TableColumn<>("Fórmula");
        colFormula.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nombreFormula()));

        TableColumn<Pedido, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().cantidadConUnidad()));

        TableColumn<Pedido, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().estado().getLabel()));

        TableColumn<Pedido, String> colObs = new TableColumn<>("Observaciones");
        colObs.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().observaciones()));

        table.getColumns().addAll(List.of(colFecha, colFormula, colCantidad, colEstado, colObs));

        ObservableList<Pedido> data = FXCollections.observableArrayList();
        cargarPedidos(data, paciente);
        table.setItems(data);

        Button btnRecargar = new Button("Actualizar");
        btnRecargar.getStyleClass().add("btn-secondary");
        btnRecargar.setOnAction(_ -> cargarPedidos(data, paciente));

        HBox botonesBox = new HBox(btnRecargar);
        botonesBox.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, table, botonesBox);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    /** Recarga la lista observable consultando al servicio con el ID del paciente. */
    private static void cargarPedidos(ObservableList<Pedido> data, Paciente paciente) {
        data.clear();
        data.addAll(pedidoService.getPedidosByPaciente(paciente.id()));
    }
}
