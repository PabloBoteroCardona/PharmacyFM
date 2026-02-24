package app;

import javafx.application.Application;
import javafx.stage.Stage;
import atlantafx.base.theme.CupertinoLight;

/**
 * Clase principal que actúa como punto de entrada de la aplicación (Entry Point).
 * Se encarga de la inicialización del sistema, la base de datos y el arranque de la interfaz.
 */
public class MainApp extends Application {

    /**
     * Método de inicio de la infraestructura de JavaFX.
     * @param primaryStage El escenario principal sobre el que se montarán las diferentes pantallas.
     */
    @Override
    public void start(Stage primaryStage) {
        // Configuración de la apariencia global de la aplicación.
        // Se aplica el tema 'CupertinoLight' de la librería AtlantaFX para lograr una estética moderna y profesional.
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        
        // Llamada inicial para mostrar la pantalla de inicio de sesión.
        LoginScreen.show(primaryStage);
    }

    /**
     * Método principal de ejecución.
     */
    public static void main(String[] args) {
        try {
            // Inicialización de la base de datos SQLite antes de lanzar la interfaz.
            // Esto asegura que las tablas y el usuario administrador existan desde el primer segundo.
            Database.initializeDatabase();
        } catch (Exception e) {
            // Manejo de excepciones críticas durante el arranque.
            System.err.println("Error crítico al inicializar la base de datos:");
            e.printStackTrace();
        }
        
        // Lanzamiento del ciclo de vida de la aplicación JavaFX.
        launch(args);
    }
}