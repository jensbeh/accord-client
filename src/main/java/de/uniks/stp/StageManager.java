package de.uniks.stp;


import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.HomeViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class StageManager extends Application {
    private ModelBuilder builder;
    private Stage stage;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Accord");
        builder = new ModelBuilder();
        scene = new Scene(new VBox(), 900, 600);
        this.showHome();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        cleanup();
    }

    public void showHome() {
        try {
            Parent root  = FXMLLoader.load(StageManager.class.getResource("HomeView.fxml"));
            Scene scene = new Scene(root);
            HomeViewController homeViewController = new HomeViewController(root, builder);
            homeViewController.init();
            this.stage.setScene(scene);
            this.stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        // call cascading stop
    }
}
