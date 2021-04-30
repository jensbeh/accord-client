package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.CreateServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class StageManager extends Application {

    private Stage stage;
    private Scene scene;
    private Stage primaryStage;
    private VBox root;
    private ModelBuilder builder;

    @Override
    public void start(Stage primaryStage) {
        // start application
        stage = primaryStage;
        stage.setTitle("Accord");
        builder = new ModelBuilder();
        scene = new Scene(new VBox(), 900, 600);
        this.showTMPScreen();
    }

    private void showTMPScreen() {
        try {
            Parent root  = FXMLLoader.load(StageManager.class.getResource("controller/CreateServerView.fxml"));
            Scene scene = new Scene(root);
            CreateServerController createServerController = new CreateServerController(root, builder);
            createServerController.init();
            createServerController.showCreateServerView();
            this.stage.setScene(scene);
            this.stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
