package de.uniks.stp.controller;

import de.uniks.stp.AlternateServerListCellFactory;
import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
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


import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class HomeViewController {
    private BorderPane root;
    private ScrollPane scrollPaneUserBox;
    private ScrollPane scrollPaneServerBox;
    private ListView<User> onlineUsersList;
    private ObservableList<User> onlineUsers;
    private ListView<Server> serverList;
    private ObservableList<Server> onlineServers;

    private VBox userBox;
    private VBox currentUserBox;
    private VBox serverBox;
    private ModelBuilder builder;
    private Parent view;
    private Circle addServer;
    private Stage stage;
    private HBox viewBox;
    private Button settingsButton;


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
        settingsButton = (Button) view.lookup("#settingsButton");

        serverList = (ListView<Server>) scrollPaneServerBox.getContent().lookup("#serverList");
        serverList.setCellFactory(new AlternateServerListCellFactory());
        onlineServers = FXCollections.observableArrayList();
        this.serverList.setItems(onlineServers);

        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        onlineUsers = FXCollections.observableArrayList();
        this.onlineUsersList.setItems(onlineUsers);
        viewBox = (HBox) view.lookup("#viewBox");
        addServer = (Circle) view.lookup("#addServer");
        addServer.setOnMouseClicked(this::onshowCreateServer);

        this.settingsButton.setOnAction(this::settingsButtonOnClicked);

        setupBuilder();
        showServers();
        showCurrentUser();
        showUser();
    }

    private void setupBuilder() {
        this.builder.addPropertyChangeListener("onlineUsers",this::handleUserListChange);
        this.builder.addPropertyChangeListener("onlineServers",this::handleServerListChange);
    }

    private void handleUserListChange(PropertyChangeEvent event){
        onlineUsers.clear();
        onlineUsers.addAll(builder.getUsers());
    }

    private void handleServerListChange(PropertyChangeEvent event){
        onlineServers.clear();
        onlineServers.addAll(builder.getServer());
    }
    ///////////////////////////
    // Server
    ///////////////////////////

    private void onshowCreateServer(MouseEvent mouseEvent) {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/CreateServerView.fxml"));
            Scene scene = new Scene(root);
            CreateServerController createServerController = new CreateServerController(root, builder);
            createServerController.init();
            stage = new Stage();
            createServerController.showCreateServerView(this::onServerCreated);
            stage.setTitle("Create a new Server");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onServerCreated(){
        Platform.runLater(() ->{
            stage.close();
            List<Server> serverList = this.builder.getServer();
            Server newServer = serverList.get(serverList.size()-1);
            showServerView(newServer);
        });
    }

    public void showServerView(Server server) {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/ServerChatView.fxml"));
            ServerViewController serverController = new ServerViewController(root, builder, server);
            serverController.init();serverController.showServerChat();
            this.root.setCenter(serverController.getRoot());
            builder.clearUsers();
            // show online users and set it in root (BorderPain)
            serverController.showOnlineUsers(builder.getPersonalUser().getUserKey());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showServers() {
        onlineServers.clear();
        if (!builder.getPersonalUser().getUserKey().equals("")) {
            JSONArray jsonResponse = RestClient.getServers(builder.getPersonalUser().getUserKey());
            for (int i = 0; i < jsonResponse.length(); i++) {
                String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                if (!serverName.equals(builder.getPersonalUser().getName())) {
                    builder.buildServer(serverName, serverId);
                    onlineServers.add(new Server().setName(serverName).setId(serverId));
                }
            }
        }
    }
    ///////////////////////////
    // Users
    ///////////////////////////

    private void showUser() {
        onlineUsers.clear();
        JSONArray jsonResponse = RestClient.getUsers(builder.getPersonalUser().getUserKey());
        for (int i = 0; i < jsonResponse.length(); i++) {
            String userName = jsonResponse.getJSONObject(i).get("name").toString();
            if (!userName.equals(builder.getPersonalUser().getName())) {
                builder.buildUser(userName);
                onlineUsers.add(new User().setName(userName).setStatus(true));
            }
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

    public void stop() {
        this.settingsButton.setOnAction(null);
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    private void settingsButtonOnClicked(ActionEvent actionEvent) {
        StageManager.showSettingsScreen();
    }
}