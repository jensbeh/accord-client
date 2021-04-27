package de.uniks.stp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageManager extends Application {


    @Override
    public void start(Stage primaryStage) {
        // start application
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        cleanup();
    }

    private static void cleanup() {
        // call cascading stop
    }
}
