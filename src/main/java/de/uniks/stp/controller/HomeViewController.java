package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class HomeViewController {
    private BorderPane root;
    private ScrollPane scrollPaneUserBox;
    private ScrollPane scrollPaneServerBox;
    private VBox userBox;
    private VBox currentUserBox;
    private VBox serverBox;
    private ModelBuilder builder;
    private Parent view;
    private Circle addServer;
    private Stage stage;
    private HBox viewBox;

    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        root = (BorderPane) view.lookup("#root");
        scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        currentUserBox = (VBox) scrollPaneUserBox.getContent().lookup("#currentUserBox");
        userBox = (VBox) scrollPaneUserBox.getContent().lookup("#userBox");
        scrollPaneServerBox = (ScrollPane) view.lookup("#scrollPaneServerBox");
        serverBox = (VBox) scrollPaneServerBox.getContent().lookup("#serverBox");
        viewBox = (HBox) view.lookup("#viewBox");
        addServer = (Circle) view.lookup("#addServer");
        addServer.setOnMouseClicked(this::onshowCreateServer);
    }

    private void onshowCreateServer(MouseEvent mouseEvent) {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/CreateServerView.fxml"));
            Scene scene = new Scene(root);
            CreateServerController createServerController = new CreateServerController(root, builder);
            createServerController.init();
            stage = new Stage();
            createServerController.showCreateServerView(this::onServerCreated);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onServerCreated(){
        stage.close();
        List<Server> serverList = this.builder.getServer();
        Server newServer = serverList.get(serverList.size()-1);
        Platform.runLater(() -> {showServerView(newServer);});
    }

    public void showServerView(Server server) {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/ServerChatView.fxml"));
            ServerViewController serverController = new ServerViewController(root, builder, server);

            serverController.init();
            this.root.setCenter(serverController.showServer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showHome() {
        login();
        showServers();
        showCurrentUser();
        showUser();
    }

    private void login() {
        try {
            String userKey = RestClient.login("Peter Lustig", "1234");
            builder.buildPersonalUser("Peter Lustig", userKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showServers() {
        try {
            JSONArray jsonResponse = RestClient.getServers(builder.getPersonalUser().getUserKey());
            for (int i = 0; i < jsonResponse.length(); i++) {
                Parent root = FXMLLoader.load(StageManager.class.getResource("ServerProfileView.fxml"));
                ServerProfileController serverProfileController = new ServerProfileController(root, builder);
                serverProfileController.init();
                String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                builder.buildServer(serverName, serverId);
                serverProfileController.setServerName(serverName);
                this.serverBox.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCurrentUser() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("UserProfileView.fxml"));
            UserProfileController userProfileController = new UserProfileController(root, builder);
            userProfileController.init();
            User currentUser = builder.getPersonalUser();
            userProfileController.setUserName(currentUser.getName());
            userProfileController.setOnline();
            this.currentUserBox.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showUser() {
        try {
            JSONArray jsonResponse = RestClient.getUsers(builder.getPersonalUser().getUserKey());
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                if (!userName.equals(builder.getPersonalUser().getName())) {
                    Parent root = FXMLLoader.load(StageManager.class.getResource("UserProfileView.fxml"));
                    UserProfileController userProfileController = new UserProfileController(root, builder);
                    userProfileController.init();
                    builder.buildUser(userName);
                    userProfileController.setUserName(userName);
                    userProfileController.setOnline();
                    this.userBox.getChildren().add(root);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }
}