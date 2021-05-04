package de.uniks.stp;

import de.uniks.stp.controller.SettingsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageManager extends Application {

    private static Stage stage;
    private static SettingsController settingsController;

    @Override
    public void start(Stage primaryStage) {
        // start application
        stage = primaryStage;
        showSettingsScreen();
        primaryStage.show();

        SettingsController.setup();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        cleanup();
    }

    private static void cleanup() {
        // call cascading stop
        if(settingsController != null) {
            settingsController.stop();
            settingsController = null;
        }
    }

    public static void showSettingsScreen() {
        try {
            // load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/settings/Settings.fxml"));
            Scene scene = new Scene(root);
            stage.titleProperty().bind(LangString.lStr("window_title_settings"));

            // init controller
            settingsController = new SettingsController(root);
            settingsController.init();

            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
        }
        catch (Exception e) {
            System.err.println("Error on showing Setting Screen");
            e.printStackTrace();
        }
    }
}
