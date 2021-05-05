package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.LoginScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.Objects;

public class StageManager extends Application {


    private Stage stage;
    private LoginScreenController loginCtrl;
    private ModelBuilder builder;

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
            builder = new ModelBuilder();
            this.loginCtrl = new LoginScreenController(root, builder);
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
    public void stop() {
        try {
            super.stop();

            //automatic logout if application is closed
            if (!Objects.isNull(builder.getPersonalUser())) {
                String userKey = builder.getPersonalUser().getUserKey();
                if (userKey != null && !userKey.isEmpty()) {
                    JsonNode body = Unirest.post("https://ac.uniks.de/api/users/logout").header("userKey", userKey).asJson().getBody();
                    System.out.println("Logged out");
                }
            }


            Unirest.shutDown();
        } catch (Exception e) {
            System.err.println("Error while shutdown");
            e.printStackTrace();
        }
        cleanup();
    }

    private void cleanup() {
        // call cascading stop
        if (loginCtrl != null) {
            loginCtrl.stop();
            loginCtrl = null;
        }
    }
}
