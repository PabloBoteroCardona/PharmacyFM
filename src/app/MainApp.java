package app;

import javafx.application.Application;
import javafx.stage.Stage;
import atlantafx.base.theme.CupertinoLight;




public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) {
	    // Aplicar tema moderno AtlantaFX
	    Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
	    LoginScreen.show(primaryStage);
	}

    public static void main(String[] args) {
        try {
            Database.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Error inicializando la base de datos:");
            e.printStackTrace();
        }
        launch(args);
    }
}
