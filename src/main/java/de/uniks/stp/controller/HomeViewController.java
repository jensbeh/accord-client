package de.uniks.stp.controller;

import de.uniks.stp.AlternateChannelListCellFactory;
import de.uniks.stp.AlternateServerListCellFactory;
import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.*;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.WSCallback;
import de.uniks.stp.net.WebSocketClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import util.JsonUtil;
import util.SortUser;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomeViewController {
    private final RestClient restClient;
    private BorderPane root;
    private ScrollPane scrollPaneUserBox;
    private ScrollPane scrollPaneServerBox;
    private VBox userBox;
    private VBox currentUserBox;
    private VBox serverBox;
    private VBox messages;
    private HBox messageBar;
    private HBox viewBox;
    private ObservableList<Channel> privateChats;
    private Parent view;
    private ListView<Channel> privateChatList;
    private ListView<Server> serverList;
    private ListView<User> onlineUsersList;
    private Circle addServer;
    private Circle homeButton;
    private Circle homeCircle;
    private Button settingsButton;
    private Button logoutButton;
    private Channel selectedChat;
    private Stage stage;
    private ModelBuilder builder;
    private WebSocketClient USER_CLIENT;
    private WebSocketClient SERVER_USER;

    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = new RestClient();
    }

    @SuppressWarnings("unchecked")
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
        logoutButton = (Button) view.lookup("#logoutButton");

        privateChatList = (ListView<Channel>) view.lookup("#privateChatList");

        privateChatList.setCellFactory(new AlternateChannelListCellFactory());
        this.privateChatList.setOnMouseReleased(this::onprivateChatListClicked);
        privateChats = FXCollections.observableArrayList();
        this.privateChatList.setItems(privateChats);

        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        this.onlineUsersList.setOnMouseReleased(this::ononlineUsersListClicked);
        viewBox = (HBox) view.lookup("#viewBox");
        addServer = (Circle) view.lookup("#addServer");
        addServer.setOnMouseClicked(this::onshowCreateServer);

        serverList = (ListView<Server>) scrollPaneServerBox.getContent().lookup("#serverList");
        serverList.setCellFactory(new AlternateServerListCellFactory());
        this.serverList.setOnMouseReleased(this::onServerClicked);

        this.settingsButton.setOnAction(this::settingsButtonOnClicked);
        this.logoutButton.setOnAction(this::logoutButtonOnClicked);

        this.homeButton.setOnMouseClicked(this::homeButtonClicked);

        showServers();
        showCurrentUser();
        showUsers();
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

    public void onServerCreated() {
        Platform.runLater(() -> {
            stage.close();
            try {
                if (SERVER_USER != null) {
                    if (SERVER_USER.getSession() != null) {
                        SERVER_USER.stop();
                    }
                }
                if (USER_CLIENT != null) {
                    if (USER_CLIENT.getSession() != null) {
                        USER_CLIENT.stop();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            showServerView();
        });
    }

    public void showServerView() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/ServerChatView.fxml"));
            ServerViewController serverController = new ServerViewController(root, builder, builder.getCurrentServer());
            serverController.init();
            serverController.showServerChat();
            this.root.setCenter(serverController.getRoot());
            // show online users and set it in root (BorderPain)
            Platform.runLater(() -> {
                showServers();
                showServerUsers();
            });

            SERVER_USER = new WebSocketClient(builder, new URI("wss://ac.uniks.de/ws/system?serverId=" + builder.getCurrentServer().getId()), new WSCallback() {

                @Override
                public void handleMessage(JsonStructure msg) {
                    System.out.println("msg: " + msg);
                    JsonObject jsonMsg = JsonUtil.parse(msg.toString());
                    String userAction = jsonMsg.getString("action");
                    JsonObject jsonData = jsonMsg.getJsonObject("data");
                    String userName = jsonData.getString("name");
                    String userId = jsonData.getString("id");

                    if (userAction.equals("userJoined")) {
                        builder.buildServerUser(userName, userId, true);
                    }
                    if (userAction.equals("userLeft")) {
                        builder.buildServerUser(userName, userId, false);
                    }
                    Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.getCurrentServer().getUser())));
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {

                }
            });

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void onServerClicked(MouseEvent mouseEvent) {
        try {
            if (SERVER_USER != null) {
                if (SERVER_USER.getSession() != null) {
                    SERVER_USER.stop();
                }
            }
            if (USER_CLIENT != null) {
                if (USER_CLIENT.getSession() != null) {
                    USER_CLIENT.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mouseEvent.getClickCount() == 1 && this.serverList.getItems().size() != 0) {
            if (this.builder.getCurrentServer() != (this.serverList.getSelectionModel().getSelectedItem())) {
                Server selectedServer = this.serverList.getSelectionModel().getSelectedItem();
                this.builder.setCurrentServer(selectedServer);
                showServerView();
            }
        }
    }

    private void showServers() {
        if (!builder.getPersonalUser().getUserKey().equals("")) {
            restClient.getServers(builder.getPersonalUser().getUserKey(), response -> {
                JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
                //List to track the online users in order to remove old users that are now offline
                ArrayList<Server> onlineServers = new ArrayList<>();
                for (int i = 0; i < jsonResponse.length(); i++) {
                    String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                    String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                    Server server = builder.buildServer(serverName, serverId);
                    onlineServers.add(server);
                }
                for (Server server : builder.getPersonalUser().getServer()) {
                    if (!onlineServers.contains(server)) {
                        builder.getPersonalUser().withoutServer(server);
                    }
                }
                Platform.runLater(() -> serverList.setItems(FXCollections.observableList(builder.getPersonalUser().getServer())));
            });
        }
    }
    ///////////////////////////
    // Users
    ///////////////////////////

    private void showServerUsers() {
        restClient.getUsers(builder.getPersonalUser().getUserKey(), response -> {
            Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.getCurrentServer().getUser())));
        });
    }

    private void showUsers() {
        restClient.getUsers(builder.getPersonalUser().getUserKey(), response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                String userId = jsonResponse.getJSONObject(i).get("id").toString();
                if (!userName.equals(builder.getPersonalUser().getName())) {
                    builder.buildUser(userName, userId);
                }
            }
            Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.getPersonalUser().getUser()).sorted(new SortUser())));
        });

        try {
            USER_CLIENT = new WebSocketClient(builder, new URI("wss://ac.uniks.de/ws/system"), new WSCallback() {
                @Override
                public void handleMessage(JsonStructure msg) {
                    System.out.println("msg: " + msg);
                    JsonObject jsonMsg = JsonUtil.parse(msg.toString());
                    String userAction = jsonMsg.getString("action");
                    JsonObject jsonData = jsonMsg.getJsonObject("data");
                    String userName = jsonData.getString("name");
                    String userId = jsonData.getString("id");

                    if (userAction.equals("userJoined")) {
                        builder.buildUser(userName, userId);
                    }
                    if (userAction.equals("userLeft")) {
                        List<User> userList = builder.getPersonalUser().getUser();
                        User removeUser = builder.buildUser(userName, userId);
                        if (userList.contains(removeUser)) {
                            builder.getPersonalUser().withoutUser(removeUser);
                        }
                    }
                    Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.getPersonalUser().getUser()).sorted(new SortUser())));
                }

                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println(closeReason.getCloseCode().toString());
                    if (!closeReason.getCloseCode().toString().equals("NORMAL_CLOSURE")) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Users cannot be displayed. No connection to server.", ButtonType.OK);
                            alert.setTitle("Error Dialog");
                            alert.setHeaderText("No Connection");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                showUsers();
                            }
                        });
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void showCurrentUser() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("UserProfileView.fxml"));
            UserProfileController userProfileController = new UserProfileController(root, builder);
            userProfileController.init();
            CurrentUser currentUser = builder.getPersonalUser();
            userProfileController.setUserName(currentUser.getName());
            userProfileController.setOnline();
            this.currentUserBox.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onprivateChatListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && this.privateChatList.getSelectionModel().getSelectedItem() != null) {
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
        if (mouseEvent.getClickCount() == 2 && this.onlineUsersList.getItems().size() != 0) {
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
        this.logoutButton.setOnAction(null);
        try {
            if (SERVER_USER != null) {
                if (SERVER_USER.getSession() != null) {
                    SERVER_USER.stop();
                }
            }
            if (USER_CLIENT != null) {
                if (USER_CLIENT.getSession() != null) {
                    USER_CLIENT.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void settingsButtonOnClicked(ActionEvent actionEvent) {
        StageManager.showSettingsScreen();
    }

    private void homeButtonClicked(MouseEvent mouseEvent) {
        try {
            if (SERVER_USER != null) {
                if (SERVER_USER.getSession() != null) {
                    SERVER_USER.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        root.setCenter(viewBox);
        this.builder.setCurrentServer(null);
        homeCircle.setFill(Paint.valueOf("#5a5c5e"));
        showUsers();
    }

    private void logoutButtonOnClicked(ActionEvent actionEvent) {
        try {
            if (SERVER_USER != null) {
                if (SERVER_USER.getSession() != null) {
                    SERVER_USER.stop();
                }
            }
            if (USER_CLIENT != null) {
                if (USER_CLIENT.getSession() != null) {
                    USER_CLIENT.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        RestClient restclient = new RestClient();
        restclient.logout(builder.getPersonalUser().getUserKey(), response -> {
            JSONObject result = response.getBody().getObject();
            if (result.get("status").equals("success")) {
                System.out.println(result.get("message"));
                Platform.runLater(StageManager::showLoginScreen);
            }
        });
    }
}
