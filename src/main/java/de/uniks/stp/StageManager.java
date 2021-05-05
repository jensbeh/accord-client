package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.HomeViewController;
import de.uniks.stp.controller.LoginScreenController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.Objects;


public class StageManager extends Application {
    private static ModelBuilder builder;
    private static Stage stage;
    private static HomeViewController homeViewController;
    private static LoginScreenController loginCtrl;

    @Override
    public void start(Stage primaryStage) {
        // start application
        stage = primaryStage;
        showLoginScreen();
        primaryStage.show();
    }

    public void showLoginScreen() {
        cleanup();

        //show login screen
        try{
            Parent root = FXMLLoader.load(StageManager.class.getResource("LoginScreenView.fxml"));
            Scene scene = new Scene(root);
            builder = new ModelBuilder();
            loginCtrl = new LoginScreenController(root, builder);
            loginCtrl.init();
            stage.setTitle("Accord - Login");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
        }catch (Exception e){
            System.err.println("Error on showing LoginScreen");
            e.printStackTrace();
        }
    }

    public static void showHome() {
        cleanup();
        try {
            Parent root  = FXMLLoader.load(StageManager.class.getResource("HomeView.fxml"));
            Scene scene = new Scene(root);
            homeViewController = new HomeViewController(root, builder);
            homeViewController.init();
            stage.setTitle("Accord - Main");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
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



    private static void cleanup() {
        // call cascading stop
        if (loginCtrl != null) {
            loginCtrl.stop();
            loginCtrl = null;
        }
        if (homeViewController != null) {
            homeViewController.stop();
            homeViewController = null;
        }
    }

    public ModelBuilder getBuilder() {
        return builder;
    }
}
