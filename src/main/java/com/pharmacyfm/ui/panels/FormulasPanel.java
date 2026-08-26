package com.pharmacyfm.ui.panels;

import com.pharmacyfm.AppContext;
import com.pharmacyfm.service.FormulaService;
import com.pharmacyfm.ui.AlertHelper;
import com.pharmacyfm.domain.model.Formula;

import javafx.beans.property.ReadOnlyObjectWrapper;
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
 * Panel CRUD del catálogo de fórmulas magistrales para el área de administración.
 *
 * Permite listar, añadir, editar y eliminar fórmulas del catálogo.
 * El diálogo de crear/editar es reutilizable: recibe null para creación
 * y la fórmula existente para edición.
 *
 * Las columnas usan lambdas como cell value factories para compatibilidad
 * con el modelo Formula definido como Java record.
 *
 * No instanciable: el método build() es el único punto de entrada.
 */
public final class FormulasPanel {

    /** Servicio de fórmulas obtenido de la raíz de composición. */
    private static final FormulaService formulaService = AppContext.get().formulaService();

    private FormulasPanel() {}

    /**
     * Construye y devuelve el panel completo de gestión del catálogo de fórmulas.
     *
     * @return VBox con la tabla y los controles CRUD de fórmulas.
     */
    public static VBox build() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Gestión de Fórmulas Magistrales");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<Formula> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Columnas con lambdas: compatibles con el record Formula (accesores sin prefijo "get")
        TableColumn<Formula, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nombre()));

        TableColumn<Formula, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().descripcion()));

        TableColumn<Formula, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().precio()));

        table.getColumns().addAll(List.of(colNombre, colDesc, colPrecio));

        ObservableList<Formula> data = FXCollections.observableArrayList(formulaService.getAllFormulas());
        table.setItems(data);

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
            else AlertHelper.mostrarAlerta("Selecciona una fórmula.");
        });

        btnDel.setOnAction(_ -> {
            Formula sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                boolean ok = formulaService.eliminarFormula(sel.id());
                if (ok) data.setAll(formulaService.getAllFormulas());
                else AlertHelper.mostrarAlerta("No se pudo eliminar la fórmula.");
            } else {
                AlertHelper.mostrarAlerta("Selecciona una fórmula.");
            }
        });

        HBox botones = new HBox(10, btnAdd, btnEdit, btnDel);
        botones.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, table, botones);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    /**
     * Muestra el diálogo de creación o edición de una fórmula.
     * Si formulaOriginal es null se abre en modo creación; si no, en modo edición.
     *
     * @param formulaOriginal Fórmula a editar, o null para crear una nueva.
     * @param data            Lista observable que se refresca tras guardar.
     */
    private static void mostrarDialogFormula(Formula formulaOriginal, ObservableList<Formula> data) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(formulaOriginal == null ? "Nueva fórmula" : "Editar fórmula");

        // Si no hay fórmula original, creamos un objeto vacío temporal (id == 0)
        final Formula formula = (formulaOriginal == null) ? new Formula("", "", 0.0) : formulaOriginal;

        TextField txtNombre = new TextField(formula.nombre());
        txtNombre.setMaxWidth(Double.MAX_VALUE);

        TextField txtDescripcion = new TextField(formula.descripcion());
        txtDescripcion.setMaxWidth(Double.MAX_VALUE);

        TextField txtPrecio = new TextField(String.valueOf(formula.precio()));
        txtPrecio.setMaxWidth(Double.MAX_VALUE);

        Button btnGuardar  = new Button("Guardar");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            try {
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                // Construimos la fórmula conservando el ID original (0 si es nueva)
                Formula toSave = new Formula(
                        formula.id(),
                        txtNombre.getText().trim(),
                        txtDescripcion.getText().trim(),
                        precio
                );
                boolean ok = formulaService.guardarFormula(toSave);
                if (ok) {
                    data.setAll(formulaService.getAllFormulas());
                    dlg.close();
                } else {
                    AlertHelper.mostrarAlerta("Error guardando la fórmula.");
                }
            } catch (NumberFormatException ex) {
                AlertHelper.mostrarAlerta("Precio inválido. Usa un número, por ejemplo 12.5");
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
        scene.getStylesheets().add(AlertHelper.getCss());
        dlg.setScene(scene);
        dlg.showAndWait();
    }
}
