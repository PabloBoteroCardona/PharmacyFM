package app.ui.panels;

import app.AppContext;
import app.service.PacienteService;
import app.ui.AlertHelper;
import com.pharmacyfm.domain.model.Paciente;

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

/**
 * Panel de gestión del directorio de pacientes para el área de administración.
 *
 * Lista todos los pacientes registrados y permite editar sus datos de contacto
 * (nombre, email, teléfono) mediante un diálogo modal.
 *
 * Las columnas usan lambdas como cell value factories para compatibilidad
 * con el modelo Paciente definido como Java record.
 *
 * No instanciable: el método build() es el único punto de entrada.
 */
public final class PacientesPanel {

    /** Servicio de pacientes obtenido de la raíz de composición. */
    private static final PacienteService pacienteService = AppContext.get().pacienteService();

    private PacientesPanel() {}

    /**
     * Construye y devuelve el panel completo de gestión de pacientes.
     *
     * @return VBox con la tabla y los controles de administración de pacientes.
     */
    public static VBox build() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Gestión de Pacientes");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<Paciente> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Columnas con lambdas: compatibles con el record Paciente (accesores sin prefijo "get")
        TableColumn<Paciente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nombre()));

        TableColumn<Paciente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().email()));

        TableColumn<Paciente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().telefono()));

        table.getColumns().addAll(colNombre, colEmail, colTelefono);

        ObservableList<Paciente> data = FXCollections.observableArrayList(pacienteService.getTodosPacientes());
        table.setItems(data);

        Button btnEditar   = new Button("Editar datos");
        btnEditar.getStyleClass().add("btn-primary");

        Button btnRecargar = new Button("Actualizar");
        btnRecargar.getStyleClass().add("btn-secondary");

        btnEditar.setOnAction(_ -> {
            Paciente sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogEditarPaciente(sel, data);
            else AlertHelper.mostrarAlerta("Selecciona un paciente.");
        });

        btnRecargar.setOnAction(_ -> data.setAll(pacienteService.getTodosPacientes()));

        HBox botones = new HBox(10, btnEditar, btnRecargar);
        botones.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, table, botones);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    /**
     * Muestra el diálogo de edición de datos de contacto del paciente seleccionado.
     * El ID del usuario (idUsuario) no es editable; se conserva en la copia inmutable.
     *
     * @param p    Paciente a editar.
     * @param data Lista observable que se refresca tras guardar.
     */
    private static void mostrarDialogEditarPaciente(Paciente p, ObservableList<Paciente> data) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Editar datos del paciente");

        TextField txtNombre   = new TextField(p.nombre());
        TextField txtEmail    = new TextField(p.email());
        TextField txtTelefono = new TextField(p.telefono());

        Button btnGuardar  = new Button("Guardar");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            // Usamos el constructor del record conservando id e idUsuario intactos
            Paciente actualizado = new Paciente(
                    p.id(), p.idUsuario(),
                    txtNombre.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtEmail.getText().trim()
            );
            boolean ok = pacienteService.actualizarPaciente(actualizado);
            if (ok) {
                data.setAll(pacienteService.getTodosPacientes());
                dlg.close();
            } else {
                AlertHelper.mostrarAlerta("Error guardando los cambios.");
            }
        });

        btnCancelar.setOnAction(_ -> dlg.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10,
                new Label("Nombre:"),   txtNombre,
                new Label("Email:"),    txtEmail,
                new Label("Teléfono:"), txtTelefono,
                botones
        );
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 350, 260);
        scene.getStylesheets().add(AlertHelper.getCss());
        dlg.setScene(scene);
        dlg.showAndWait();
    }
}
