package app;

import app.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginScreen {

    private static final AuthService authService = new AuthService();

    /**
     * Método reutilizable que devuelve la ruta del CSS.
     */
    private static String getCss() {
        return new java.io.File("src/resource/styles.css").toURI().toString();
    }

    public static void show(Stage stage) {

        // ---- Panel izquierdo ----
        Label lblEmoji = new Label("💊");
        lblEmoji.getStyleClass().add("login-app-badge");

        Label lblAppName = new Label("PharmacyFM");
        lblAppName.getStyleClass().add("login-app-title");

        Label lblSubtitle = new Label("Gestión de fórmulas magistrales para farmacias modernas.");
        lblSubtitle.getStyleClass().add("login-app-subtitle");
        lblSubtitle.setMaxWidth(220);
        lblSubtitle.setWrapText(true);

        VBox leftPanel = new VBox(20, lblEmoji, lblAppName, lblSubtitle);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.getStyleClass().add("login-left-panel");
        leftPanel.setPrefWidth(300);
        leftPanel.setMinWidth(260);

        // ---- Panel derecho ----
        Label lblFormTitle = new Label("Bienvenido");
        lblFormTitle.getStyleClass().add("login-form-title");

        Label lblFormSubtitle = new Label("Inicia sesión en tu cuenta");
        lblFormSubtitle.getStyleClass().add("login-form-subtitle");

        Label lblEmail = new Label("Email");
        lblEmail.getStyleClass().add("login-label");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("ejemplo@correo.com");
        txtEmail.setMaxWidth(Double.MAX_VALUE);

        Label lblPass = new Label("Contraseña");
        lblPass.getStyleClass().add("login-label");
        PasswordField txtPass = new PasswordField();
        txtPass.setMaxWidth(Double.MAX_VALUE);

        // Botón "¿Olvidaste tu contraseña?" centrado
        Button btnOlvide = new Button("¿Olvidaste tu contraseña?");
        btnOlvide.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a6b4a; " +
                           "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        HBox hbOlvide = new HBox(btnOlvide);
        hbOlvide.setAlignment(Pos.CENTER);
        VBox.setMargin(hbOlvide, new Insets(5, 0, 5, 0));

        Label lblError = new Label();
        lblError.getStyleClass().add("login-error");

        Button btnLogin = new Button("Entrar");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        Button btnRegistro = new Button("Registrarse");
        btnRegistro.getStyleClass().add("btn-secondary");
        btnRegistro.setMaxWidth(Double.MAX_VALUE);

        btnLogin.setOnAction(event -> {
            if (event == null) return;
            String email = txtEmail.getText().trim();
            String pass  = txtPass.getText().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                lblError.setText("Debes introducir email y contraseña.");
                return;
            }
            User user = authService.login(email, pass);
            if (user == null) {
                lblError.setText("Email o contraseña incorrectos.");
                return;
            }
            lblError.setText("");
            if ("admin".equalsIgnoreCase(user.getRol())) {
                AdminWindow.show(stage, user);
            } else {
                UserWindow.show(stage, user);
            }
        });

        btnRegistro.setOnAction(_ -> showRegistroDialog(stage));
        btnOlvide.setOnAction(_ -> showRecuperarPasswordDialog(stage));

        VBox rightPanel = new VBox(14,
                lblFormTitle,
                lblFormSubtitle,
                lblEmail, txtEmail,
                lblPass, txtPass,
                hbOlvide,
                lblError,
                btnLogin,
                btnRegistro
        );
        rightPanel.setAlignment(Pos.CENTER_LEFT);
        rightPanel.getStyleClass().add("login-right-panel");
        rightPanel.setPadding(new Insets(40));
        rightPanel.setPrefWidth(340);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        HBox root = new HBox(leftPanel, rightPanel);

        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getCss());

        stage.setTitle("PharmacyFM - Inicio de sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // ---- Diálogo recuperar contraseña ----
    private static void showRecuperarPasswordDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Recuperar contraseña");

        Label lblInfo = new Label("Introduce tu email y tu nueva contraseña.");
        lblInfo.setWrapText(true);

        Label lblEmail = new Label("Email:");
        lblEmail.getStyleClass().add("login-label");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("ejemplo@correo.com");

        Label lblNueva = new Label("Nueva contraseña:");
        lblNueva.getStyleClass().add("login-label");
        PasswordField txtNueva = new PasswordField();

        Label lblConfirmar = new Label("Confirmar contraseña:");
        lblConfirmar.getStyleClass().add("login-label");
        PasswordField txtConfirmar = new PasswordField();

        Label lblError = new Label();
        lblError.getStyleClass().add("login-error");

        Button btnGuardar  = new Button("Cambiar contraseña");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            String email     = txtEmail.getText().trim();
            String nueva     = txtNueva.getText().trim();
            String confirmar = txtConfirmar.getText().trim();
            if (email.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }
            if (!nueva.equals(confirmar)) {
                lblError.setText("Las contraseñas no coinciden.");
                return;
            }
            if (nueva.length() < 4) {
                lblError.setText("La contraseña debe tener al menos 4 caracteres.");
                return;
            }
            boolean ok = authService.recuperarPassword(email, nueva);
            if (ok) {
                mostrarAlerta("Contraseña actualizada correctamente. Ya puedes iniciar sesión.");
                dialog.close();
            } else {
                lblError.setText("No se encontró ningún usuario con ese email.");
            }
        });

        btnCancelar.setOnAction(_ -> dialog.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(12,
                lblInfo,
                lblEmail, txtEmail,
                lblNueva, txtNueva,
                lblConfirmar, txtConfirmar,
                lblError,
                botones
        );
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 380, 360);
        scene.getStylesheets().add(getCss());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ---- Diálogo registro ----
    private static void showRegistroDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Registro de nuevo usuario");

        Label lblInfo = new Label("Introduce tus datos para registrarte como paciente.");
        lblInfo.setWrapText(true);

        Label lblNombre = new Label("Nombre completo:");
        lblNombre.getStyleClass().add("login-label");
        TextField txtNombre = new TextField();

        Label lblEmail = new Label("Email:");
        lblEmail.getStyleClass().add("login-label");
        TextField txtEmail = new TextField();

        Label lblTelefono = new Label("Teléfono:");
        lblTelefono.getStyleClass().add("login-label");
        TextField txtTelefono = new TextField();
        

        Label lblPass = new Label("Contraseña:");
        lblPass.getStyleClass().add("login-label");
        PasswordField txtPass = new PasswordField();

        CheckBox chkPrivacidad = new CheckBox("He leído y acepto la política de privacidad *");
        chkPrivacidad.setWrapText(true);

        CheckBox chkComunicados = new CheckBox("Acepto recibir comunicados y novedades por correo electrónico");
        chkComunicados.setWrapText(true);

        Label lblError = new Label();
        lblError.getStyleClass().add("login-error");

        Button btnGuardar  = new Button("Registrarse");
        btnGuardar.getStyleClass().add("btn-primary");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(event -> {
            if (event == null) return;
            String nombre   = txtNombre.getText().trim();
            String email    = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String pass     = txtPass.getText().trim();
            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                lblError.setText("Nombre, email y contraseña son obligatorios.");
                return;
            }
            // Teléfono (mínimo 9 dígitos y que sean números)
            if (!telefono.matches("\\d{9,}")) { 
                lblError.setText("El teléfono debe tener al menos 9 dígitos numéricos.");
                return;
            }
            if (!chkPrivacidad.isSelected()) {
                lblError.setText("Debes aceptar la política de privacidad para registrarte.");
                return;
            }
            boolean ok = authService.registrarPaciente(nombre, email, pass, telefono);
            if (ok) {
                mostrarAlerta("Usuario registrado correctamente. Ya puedes iniciar sesión.");
                dialog.close();
            } else {
                lblError.setText("No se pudo registrar. ¿El email ya existe?");
            }
        });

        btnCancelar.setOnAction(_ -> dialog.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10,
                lblInfo,
                lblNombre, txtNombre,
                lblEmail, txtEmail,
                lblTelefono, txtTelefono,
                lblPass, txtPass,
                chkPrivacidad,
                chkComunicados,
                lblError,
                botones
        );
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 500);
        scene.getStylesheets().add(getCss());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}