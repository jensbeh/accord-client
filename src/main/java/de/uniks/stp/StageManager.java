package de.uniks.stp;

import de.uniks.stp.controller.HomeViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageManager extends Application {

    private static Stage stage;
    private static ServerEditor serverEditor;
    private static HomeViewController homeViewController;

    @Override
    public void start(Stage primaryStage) {
        // start application
        stage = primaryStage;
        showHomeScreen();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        cleanup();
    }

    public static void showHomeScreen() {
        cleanup();
        serverEditor = new ServerEditor();
        // show HomeScreen
        // load view
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/HomeView.fxml"));
            Scene scene = new Scene(root);

            // init controller
            homeViewController = new HomeViewController(root,serverEditor);
            homeViewController.init();
            // display
            stage.setTitle("Accord - Home");
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error in showHomeScreen");
            e.printStackTrace();
        }
    }


    private static void cleanup() {
        // call cascading stop
    }
}
