package de.uniks.stp.controller;

import de.uniks.stp.AlternateChannelListCellFactory;
import de.uniks.stp.AlternateServerListCellFactory;
import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.json.JSONArray;


import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class HomeViewController {
    private final RestClient restClient;
    private BorderPane root;
    private ScrollPane scrollPaneUserBox;
    private ScrollPane scrollPaneServerBox;
    private VBox userBox;
    private VBox currentUserBox;
    private VBox serverBox;
    private ModelBuilder builder;
    private Parent view;
    private ListView<Channel> privateChatList;
    private ObservableList<Channel> privateChats;
    private Channel selectedChat;
    private VBox messages;
    private ListView<User> onlineUsersList;
    private ObservableList<User> onlineUsers;
    private HBox messageBar;
    private ListView<Server> serverList;
    private ObservableList<Server> onlineServers;
    private Circle addServer;
    private Stage stage;
    private HBox viewBox;
    private Button settingsButton;
    private Circle homeButton;
    private Circle homeCircle;


    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = new RestClient();
    }

    public void init() {
        // Load all view references
        root = (BorderPane) view.lookup("#root");

        scrollPaneServerBox = (ScrollPane) view.lookup("#scrollPaneServerBox");
        scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        scrollPaneServerBox = (ScrollPane) view.lookup("#scrollPaneServerBox");

        homeCircle = (Circle) view.lookup("#homeCircle");
        homeButton = (Circle) view.lookup("#homeButton");

        currentUserBox = (VBox) scrollPaneUserBox.getContent().lookup("#currentUserBox");
        userBox = (VBox) scrollPaneUserBox.getContent().lookup("#userBox");
        serverBox = (VBox) scrollPaneServerBox.getContent().lookup("#serverBox");
        settingsButton = (Button) view.lookup("#settingsButton");
        messages = (VBox) view.lookup("#messages");
        messageBar = (HBox) view.lookup("#messagebar");
        messageBar.setOpacity(0);

        privateChatList = (ListView<Channel>) view.lookup("#privateChatList");
        privateChatList.setCellFactory(new AlternateChannelListCellFactory());
        this.privateChatList.setOnMouseReleased(this::onprivateChatListClicked);
        privateChats = FXCollections.observableArrayList();
        this.privateChatList.setItems(privateChats);

        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        this.onlineUsersList.setOnMouseReleased(this::ononlineUsersListClicked);
        onlineUsers = FXCollections.observableArrayList();
        this.onlineUsersList.setItems(onlineUsers);
        viewBox = (HBox) view.lookup("#viewBox");
        addServer = (Circle) view.lookup("#addServer");
        addServer.setOnMouseClicked(this::onshowCreateServer);

        serverList = (ListView<Server>) scrollPaneServerBox.getContent().lookup("#serverList");
        serverList.setCellFactory(new AlternateServerListCellFactory());
        onlineServers = FXCollections.observableArrayList();
        this.serverList.setItems(onlineServers);

        this.settingsButton.setOnAction(this::settingsButtonOnClicked);

        this.homeButton.setOnMouseClicked(this::homeButtonClicked);

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
            homeCircle.setFill(Paint.valueOf("#a4a4a4"));
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
            restClient.getServers(builder.getPersonalUser().getUserKey(), response -> {
                JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
                for (int i = 0; i < jsonResponse.length(); i++) {
                    String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                    String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                    if (!serverName.equals(builder.getPersonalUser().getName())) {
                        builder.buildServer(serverName, serverId);
                        onlineServers.add(new Server().setName(serverName).setId(serverId));
                    }
                }
            });
        }
    }
    ///////////////////////////
    // Users
    ///////////////////////////

    private void showUser() {
        onlineUsers.clear();
        restClient.getUsers(builder.getPersonalUser().getUserKey(), response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                if (!userName.equals(builder.getPersonalUser().getName())) {
                    builder.buildUser(userName);
                    onlineUsers.add(new User().setName(userName).setStatus(true));
                }
            }
        });
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

    private void onprivateChatListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            selectedChat = this.privateChatList.getSelectionModel().getSelectedItem();
            MessageViews();
        }
    }

    private void MessageViews() {
        this.messages.getChildren().clear();
        messageBar.setOpacity(1);
        for (Message msg : this.selectedChat.getMessage()) {
            try {
                Parent view = FXMLLoader.load(StageManager.class.getResource("Message.fxml"));
                MessageController messageController = new MessageController(msg, view, builder);
                messageController.init();

                this.messages.getChildren().add(view);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ononlineUsersListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && this.onlineUsers.size() != 0) {
            boolean flag = true;
            String selectedUserName = this.onlineUsersList.getSelectionModel().getSelectedItem().getName();
            for (Channel channel : privateChats) {
                if (channel.getName().equals(selectedUserName)) {
                    flag = false;
                    break;
                }
            }
            selectedChat = new Channel().setName(selectedUserName);
            if (flag) {
                privateChats.add(selectedChat);
            }
            MessageViews();
        }
    }

    public void stop() {
        this.addServer.setOnMouseClicked(null);
        this.homeButton.setOnMouseClicked(null);
        this.homeCircle.setOnMouseClicked(null);
        this.onlineUsersList.setOnMouseReleased(null);
        this.privateChatList.setOnMouseReleased(null);
        this.settingsButton.setOnAction(null);
        this.builder.stop();
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    private void settingsButtonOnClicked(ActionEvent actionEvent) {
        StageManager.showSettingsScreen();
    }

    private void homeButtonClicked(MouseEvent mouseEvent) {
        StageManager.showHome();
        homeCircle.setFill(Paint.valueOf("#5a5c5e"));
    }
}
