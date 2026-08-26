package com.pharmacyfm.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Utilidad de diálogos: centraliza la creación de alertas informativas
 * y el acceso a la hoja de estilos compartida por todas las ventanas.
 *
 * Al estar en el paquete app.ui, sirve tanto a los paneles de administrador
 * como a los del área de paciente, evitando que cada clase replique el código.
 *
 * No instanciable: todos sus métodos son estáticos.
 */
public final class AlertHelper {

    private AlertHelper() {
        throw new UnsupportedOperationException("Clase de utilidades, no instanciable");
    }

    /**
     * Muestra un cuadro de diálogo informativo modal y espera a que el usuario lo cierre.
     *
     * @param mensaje Texto del mensaje a mostrar al usuario.
     */
    public static void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /**
     * Devuelve la URL de la hoja de estilos de la aplicación en el classpath.
     * Usada por cada panel al crear sus propias escenas o ventanas modales.
     *
     * @return URL en formato String lista para pasarla a {@code scene.getStylesheets().add(...)}.
     */
    public static String getCss() {
        return AlertHelper.class.getResource("/styles.css").toExternalForm();
    }
}
