package de.uniks.stp;

import de.uniks.stp.controller.LoginScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kong.unirest.Unirest;

public class StageManager extends Application {


    private Stage stage;
    private LoginScreenController loginCtrl;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        showLoginScreen();
        stage.show();
    }

    public void showLoginScreen() {
        cleanup();

        //show login screen
        try{
            Parent root = FXMLLoader.load(StageManager.class.getResource("LoginScreenView.fxml"));
            Scene scene = new Scene(root);
            this.loginCtrl = new LoginScreenController(root);
            this.loginCtrl.init();
            this.stage.setTitle("Accord - Login");
            this.stage.setResizable(false);
            this.stage.setScene(scene);
            this.stage.centerOnScreen();
        }catch (Exception e){
            System.err.println("Error on showing LoginScreen");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            super.stop();
            Unirest.shutDown();
        } catch (Exception e) {
            System.err.println("Error while shutdown");
            e.printStackTrace();
        }

        cleanup();
    }

    private static void cleanup() {
        // call cascading stop
    }
}
