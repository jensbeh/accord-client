package de.uniks.stp;

import de.uniks.stp.controller.SettingsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class StageManager extends Application {

    private static Stage stage;
    private static SettingsController settingsController;

    @Override
    public void start(Stage primaryStage) {
        // start application
        stage = primaryStage;
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
    }

    public static void showSettingsScreen() {
        try {
            // load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/settings/Settings.fxml"));
            Scene scene = new Scene(root);

            // init controller
            settingsController = new SettingsController(root);
            settingsController.init();

            Stage subStage = new Stage();
            subStage.titleProperty().bind(LangString.lStr("window_title_settings"));
            subStage.setResizable(false);
            subStage.setScene(scene);
            subStage.centerOnScreen();
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest(event -> {
                if(settingsController != null) {
                    settingsController.stop();
                    settingsController = null;
                }
            });
            subStage.show();
        }
        catch (Exception e) {
            System.err.println("Error on showing Setting Screen");
            e.printStackTrace();
        }
    }
}
