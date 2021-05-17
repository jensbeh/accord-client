package de.uniks.stp.controller;

import de.uniks.stp.AlternateChannelListCellFactory;
import de.uniks.stp.AlternateServerListCellFactory;
import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.subcontroller.CreateServerController;
import de.uniks.stp.model.*;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
    private ObservableList<User> onlineUsers;
    private ObservableList<Server> onlineServers;
    private Parent view;
    private ListView<Channel> privateChatList;
    private ListView<Server> serverList;
    private ListView<User> onlineUsersList;
    private Circle addServer;
    private Circle homeButton;
    private Circle homeCircle;
    private Button settingsButton;
    private Button logoutButton;
    private static Channel selectedChat;
    private Stage stage;
    private ModelBuilder builder;
    private AlternateServerListCellFactory serverListCellFactory;
    private VBox chatViewContainer;


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
        logoutButton = (Button) view.lookup("#logoutButton");
        chatViewContainer = (VBox) view.lookup("#chatBox");
        Label label = new Label();
        label.setText("Welcome to Accord");
        label.setFont(new Font("Arial", 24));
        label.setTextFill(Color.WHITE);
        chatViewContainer.getChildren().add(label);

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

        serverListCellFactory = new AlternateServerListCellFactory();
        serverList.setCellFactory(serverListCellFactory);
        this.serverList.setOnMouseReleased(this::onServerClicked);
        onlineServers = FXCollections.observableArrayList();
        this.serverList.setItems(onlineServers);

        this.settingsButton.setOnAction(this::settingsButtonOnClicked);
        this.logoutButton.setOnAction(this::logoutButtonOnClicked);

        this.homeButton.setOnMouseClicked(this::homeButtonClicked);

        showServers();
        showCurrentUser();
        showUser();
    }


    ///////////////////////////
    // Server
    ///////////////////////////

    /**
     * Creates a createServer view in a new Stage.
     *
     * @param mouseEvent is called when clicked on the + Button.
     */
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

    /**
     * Closes the createServerStage and calls showServerView. Is
     * called after the ok button in createServer is clicked
     */
    public void onServerCreated() {
        Platform.runLater(() -> {
            stage.close();
            showServerView();
            showServers();
        });
    }

    /**
     * Changes the currently shown view to the Server view of the currentServer.
     * Also changes the online user list to an online and offline list of users in that server.
     */
    public void showServerView() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/ServerChatView.fxml"));
            ServerViewController serverController = new ServerViewController(root, builder, builder.getCurrentServer());
            serverController.init();
            serverController.showServerChat();
            this.root.setCenter(serverController.getRoot());
            // show online users and set it in root (BorderPain)
            serverController.showOnlineUsers(builder.getPersonalUser().getUserKey());
            showServerUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the clicked Server as currentServer and calls showServerView.
     *
     * @param mouseEvent is called when clicked on a Server
     */
    private void onServerClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1 && this.serverList.getItems().size() != 0) {
            if (this.builder.getCurrentServer() != (this.serverList.getSelectionModel().getSelectedItem())) {
                Server selectedServer = this.serverList.getSelectionModel().getSelectedItem();
                this.builder.setCurrentServer(selectedServer);
                updateServerListColor();
                showServerView();
            }
        }
    }

    /**
     * Updates the circles and change the current server or Home circle color
     */
    private void updateServerListColor() {
        if (builder.getCurrentServer() == null) {
            homeCircle.setFill(Paint.valueOf("#5a5c5e"));
        } else {
            homeCircle.setFill(Paint.valueOf("#a4a4a4"));
        }
        serverListCellFactory.setCurrentServer(builder.getCurrentServer());
        serverList.setItems(FXCollections.observableList(builder.getPersonalUser().getServer()));
    }

    /**
     * Get Servers and show Servers
     */
    private void showServers() {
        onlineServers.clear();
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

    /**
     * Get Server Users and set them in Online User List
     */
    private void showServerUsers() {
        restClient.getUsers(builder.getPersonalUser().getUserKey(), response -> {
            Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.getCurrentServer().getUser())));
        });
    }

    /**
     * Get the Online Users and reset old Online User List with new Online Users
     */
    private void showUser() {
        onlineUsers.clear();
        restClient.getUsers(builder.getPersonalUser().getUserKey(), response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                String userId = jsonResponse.getJSONObject(i).get("id").toString();
                if (!userName.equals(builder.getPersonalUser().getName())) {
                    builder.buildUser(userName, userId);
                    //runLater() is needed because it is called from outside the GUI thread and only the GUI thread can change the GUI
                    Platform.runLater(() -> onlineUsers.add(new User().setId(userId).setName(userName).setStatus(true)));
                }
            }
        });
    }

    /**
     * Display Current User
     */
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

    /**
     * Event Mouseclick on an existing chat
     * Opens the existing chat and shows the messages
     *
     * @param mouseEvent is called when double clicked on an existing chat
     */
    private void onprivateChatListClicked(MouseEvent mouseEvent) {
        if (this.privateChatList.getSelectionModel().getSelectedItem() != null) {
            selectedChat = this.privateChatList.getSelectionModel().getSelectedItem();
            this.privateChatList.getSelectionModel().getSelectedIndices();
            MessageViews();
        }
    }


    private void MessageViews() {
        loadChatView();
    }

    private void loadChatView() {
        this.chatViewContainer.getChildren().clear();
        try {
            Parent view = FXMLLoader.load(StageManager.class.getResource("ChatView.fxml"));
            ChatViewController messageController = new ChatViewController(view, builder);
            messageController.init();
            this.chatViewContainer.getChildren().add(view);
            chatViewContainer.setStyle("-fx-background-color: grey;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Event Mouseclick on an online user
     * Create new channel if chat not existing or open the existing chat and shows the messages
     *
     * @param mouseEvent is called when clicked on an online User
     */
    private void ononlineUsersListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && this.onlineUsers.size() != 0) {
            boolean flag = true;
            String selectedUserName = this.onlineUsersList.getSelectionModel().getSelectedItem().getName();
            String selectUserId = this.onlineUsersList.getSelectionModel().getSelectedItem().getId();
            for (Channel channel : privateChats) {
                if (channel.getName().equals(selectedUserName)) {
                    selectedChat = channel;
                    this.privateChatList.refresh();
                    flag = false;
                    break;
                }
            }
            if (flag) {
                selectedChat = new Channel().setName(selectedUserName).setId(selectUserId);
                privateChats.add(selectedChat);
            }
            MessageViews();
        }
    }

    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        this.addServer.setOnMouseClicked(null);
        this.homeButton.setOnMouseClicked(null);
        this.homeCircle.setOnMouseClicked(null);
        this.onlineUsersList.setOnMouseReleased(null);
        this.privateChatList.setOnMouseReleased(null);
        this.settingsButton.setOnAction(null);
        this.logoutButton.setOnAction(null);
    }

    /**
     * Set the Builder
     *
     * @param builder is the builder to set
     */
    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    /**
     * Clicking Settings Button opens the Settings Popup
     *
     * @param actionEvent is called when clicked on the Settings Button
     */
    private void settingsButtonOnClicked(ActionEvent actionEvent) {
        StageManager.showSettingsScreen();
    }

    /**
     * Clicking Home Button refreshes the Online Users List
     *
     * @param mouseEvent is called when clicked on the Home Button
     */
    private void homeButtonClicked(MouseEvent mouseEvent) {
        root.setCenter(viewBox);
        showUser();
        this.builder.setCurrentServer(null);
        updateServerListColor();
    }

    /**
     * Clicking Logout Button logs the currentUser out and returns to Login Screen
     *
     * @param actionEvent is called when clicked on the Logout Button
     */
    private void logoutButtonOnClicked(ActionEvent actionEvent) {
        RestClient restclient = new RestClient();
        restclient.logout(builder.getPersonalUser().getUserKey(), response -> {
            JSONObject result = response.getBody().getObject();
            if (result.get("status").equals("success")) {
                System.out.println(result.get("message"));
                Platform.runLater(StageManager::showLoginScreen);
            }
        });
    }

    /**
     * Get the current active Channel / selected Chat
     *
     * @return current active Channel
     */
    public static Channel getSelectedChat() {
        return selectedChat;
    }
}
