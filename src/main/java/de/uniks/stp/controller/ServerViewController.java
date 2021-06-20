package de.uniks.stp.controller;

import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.*;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.ServerChatWebSocket;
import de.uniks.stp.net.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import util.SortUser;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static util.Constants.*;

/**
 * The class ServerViewController is about showing the ServerView. It is used to update the builder.
 */
public class ServerViewController {

    private static ModelBuilder builder;
    private final RestClient restClient;
    private final Server server;
    private final Parent view;
    private MenuButton serverMenuButton;
    private static Label textChannelLabel;
    private static Label generalLabel;
    private static Label welcomeToAccord;
    private ListView<User> onlineUsersList;
    private ListView<User> offlineUsersList;
    private VBox currentUserBox;

    private VBox chatBox;
    private ChatViewController messageViewController;
    private MenuItem serverSettings;
    private MenuItem inviteUsers;
    private Map<Categories, CategorySubController> categorySubControllerList;
    private VBox categoryBox;
    private final HomeViewController homeViewController;
    private Line dividerLineUser;
    private VBox userBox;
    private int loadedCategories;
    private int loadedChannel;
    private ServerChannel currentChannel;
    private ServerSystemWebSocket serverSystemWebSocket;
    private ServerChatWebSocket chatWebSocketClient;

    /**
     * "ServerViewController takes Parent view, ModelBuilder modelBuilder, Server server.
     */
    public ServerViewController(Parent view, ModelBuilder modelBuilder, Server server, HomeViewController homeViewController) {
        this.view = view;
        builder = modelBuilder;
        this.server = server;
        this.restClient = modelBuilder.getRestClient();
        this.homeViewController = homeViewController;
        this.serverSystemWebSocket = builder.getServerSystemWebSocket();
        this.chatWebSocketClient = builder.getServerChatWebSocketClient();
    }

    public ChatViewController getMessageViewController() {
        return messageViewController;
    }

    public Map<Categories, CategorySubController> getCategorySubControllerList() {
        return categorySubControllerList;
    }

    public VBox getCategoryBox() {
        return categoryBox;
    }

    public HomeViewController getHomeViewController() {
        return homeViewController;
    }

    public Server getServer() {
        return server;
    }

    public ServerChannel getCurrentChannel() {
        return this.currentChannel;
    }

    public void setCurrentChannel(ServerChannel channel) {
        this.currentChannel = channel;
    }

    public ServerSystemWebSocket getServerSystemWebSocket() {
        return serverSystemWebSocket;
    }

    public ServerChatWebSocket getChatWebSocketClient() {
        return chatWebSocketClient;
    }

    /**
     * Callback, when all server information are loaded
     */
    public interface ServerReadyCallback {
        void onSuccess(String status);
    }

    /**
     * Initialise all view parameters
     */
    @SuppressWarnings("unchecked")
    public void startController(ServerReadyCallback serverReadyCallback) {
        serverMenuButton = (MenuButton) view.lookup("#serverMenuButton");
        ScrollPane scrollPaneCategories = (ScrollPane) view.lookup("#scrollPaneCategories");
        categoryBox = (VBox) scrollPaneCategories.getContent().lookup("#categoryVbox");
        textChannelLabel = (Label) view.lookup("#textChannel");
        generalLabel = (Label) view.lookup("#general");
        welcomeToAccord = (Label) view.lookup("#welcomeToAccord");
        ScrollPane scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        currentUserBox = (VBox) scrollPaneUserBox.getContent().lookup("#currentUserBox");
        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        offlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#offlineUsers");
        offlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        dividerLineUser = (Line) scrollPaneUserBox.getContent().lookup("#dividerline_online_offline_user");
        userBox = (VBox) scrollPaneUserBox.getContent().lookup("#userBox");
        chatBox = (VBox) view.lookup("#chatBox");
        categorySubControllerList = new HashMap<>();
        currentChannel = null;

        loadServerInfo(status -> {
            if (status.equals("success")) {
                if (getServer().getCategories().size() == 0) {
                    loadCategories(serverReadyCallback::onSuccess);
                }
            }
        }); // members & (categories)
        buildSystemWebSocket();
        buildChatWebSocket();
    }

    /**
     * Initialise all view parameters
     */
    public void startShowServer() throws InterruptedException {
        System.out.println("show: " + this.server.getName());
        serverMenuButton.setText(this.server.getName());
        serverSettings = serverMenuButton.getItems().get(0);
        serverSettings.setOnAction(this::onServerSettingsClicked);
        if (serverMenuButton.getItems().size() > 1) {
            inviteUsers = serverMenuButton.getItems().get(1);
            inviteUsers.setOnAction(this::onInviteUsersClicked);
        }
        builder.setServerChatWebSocketClient(this.chatWebSocketClient); // TODO because of message view

        showCurrentUser();
        showOnlineOfflineUsers();

        Platform.runLater(this::generateCategoriesChannelViews);
        if (currentChannel != null) {
            showMessageView();
        }
    }

    /**
     * WebSocket for system messages.
     */
    public void buildSystemWebSocket() {
        if (serverSystemWebSocket == null) {
            serverSystemWebSocket = new ServerSystemWebSocket(URI.create(WS_SERVER_URL + WEBSOCKET_PATH + SERVER_SYSTEM_WEBSOCKET_PATH + this.server.getId()), builder.getPersonalUser().getUserKey());
            serverSystemWebSocket.setServerViewController(this);
            serverSystemWebSocket.setBuilder(builder);
            serverSystemWebSocket.setName(server.getName());
        }
        serverSystemWebSocket.setServerViewController(this);
        serverSystemWebSocket.setBuilder(builder);
        serverSystemWebSocket.setName(server.getName());
    }

    /**
     * WebSocket for chat messages.
     */
    private void buildChatWebSocket() {
        if (chatWebSocketClient == null) {
            chatWebSocketClient = new ServerChatWebSocket(URI.
                    create(WS_SERVER_URL + WEBSOCKET_PATH + CHAT_WEBSOCKET_PATH + builder.
                            getPersonalUser().getName().replace(" ", "+") + SERVER_WEBSOCKET_PATH + this.server.getId()), builder.getPersonalUser().getUserKey());
            chatWebSocketClient.setServerViewController(this);
            chatWebSocketClient.setServer(server);
            chatWebSocketClient.setBuilder(builder);
            chatWebSocketClient.setName(server.getName());
        }
        chatWebSocketClient.setServerViewController(this);
        chatWebSocketClient.setBuilder(builder);
        chatWebSocketClient.setServer(server);
        chatWebSocketClient.setName(server.getName());
    }

    /**
     * Method for changing the current serverName.
     */
    public void changeServerName() {
        serverMenuButton.setText(server.getName());
    }

    /**
     * Initial Chat View and load chat history which is saved in list
     */
    public void showMessageView() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("ChatView.fxml")), StageManager.getLangBundle());
            this.messageViewController = new ChatViewController(root, builder, currentChannel);
            this.chatBox.getChildren().clear();
            this.messageViewController.init();
            this.chatBox.getChildren().add(root);
            if (this.server != null && currentChannel != null) {
                for (Message msg : currentChannel.getMessage()) {
                    // Display each Message which are saved
                    ChatViewController.printMessage(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Display Current User
     */
    private void showCurrentUser() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("UserProfileView.fxml")));
            UserProfileController userProfileController = new UserProfileController(root);
            userProfileController.init();
            CurrentUser currentUser = builder.getPersonalUser();
            userProfileController.setUserName(currentUser.getName());
            userProfileController.setOnline();
            this.currentUserBox.getChildren().clear();
            this.currentUserBox.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the builder and get the ServerUser as well as the categories. Also sets their online and offline Status.
     */
    public interface ServerInfoCallback {
        void onSuccess(String status);
    }

    /**
     * Method to get Server information
     */
    public void loadServerInfo(ServerInfoCallback serverInfoCallback) {
        restClient.getServerUsers(this.server.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            this.server.setOwner(body.getObject().getJSONObject("data").getString("owner"));
            if (status.equals("success")) {
                JSONArray members = body.getObject().getJSONObject("data").getJSONArray("members");
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    String id = member.getString("id");
                    String name = member.getString("name");
                    boolean online = member.getBoolean("online");
                    builder.buildServerUser(this.server, name, id, online);
                }
                serverInfoCallback.onSuccess(status);
            } else if (status.equals("failure")) {
                System.out.println(body.getObject().getString("message"));
            }
        });
    }


    /**
     * Split Users into offline and online users then update the list
     */
    public void showOnlineOfflineUsers() {
        ArrayList<User> onlineUsers = new ArrayList<>();
        ArrayList<User> offlineUsers = new ArrayList<>();
        for (User user : this.server.getUser()) {
            if (user.isStatus()) {
                if (user.getName().equals(builder.getPersonalUser().getName())) {
                    Platform.runLater(() -> checkForOwnership(user.getId()));
                }
                if (!this.server.getCurrentUser().getName().equals(user.getName())) {
                    onlineUsers.add(user);
                }
            } else {
                if (!this.server.getCurrentUser().getName().equals(user.getName())) {
                    offlineUsers.add(user);
                }
            }
        }

        Platform.runLater(() -> {
            this.dividerLineUser.setVisible(onlineUsers.size() > 0 && offlineUsers.size() > 0);
            if (onlineUsers.size() == 0) {
                onlineUsersList.prefHeightProperty().bind(onlineUsersList.fixedCellSizeProperty().multiply(0));
                offlineUsersList.prefHeightProperty().bind(offlineUsersList.fixedCellSizeProperty().multiply(offlineUsers.size()));
                onlineUsersList.setItems(FXCollections.observableList(onlineUsers).sorted(new SortUser()));
                offlineUsersList.setItems(FXCollections.observableList(offlineUsers).sorted(new SortUser()));
                userBox.setSpacing(0);
            } else {
                userBox.setSpacing(8);
                onlineUsersList.prefHeightProperty().bind(onlineUsersList.fixedCellSizeProperty().multiply(onlineUsers.size()));
                offlineUsersList.prefHeightProperty().bind(offlineUsersList.fixedCellSizeProperty().multiply(offlineUsers.size()));
                onlineUsersList.setItems(FXCollections.observableList(onlineUsers).sorted(new SortUser()));
                offlineUsersList.setItems(FXCollections.observableList(offlineUsers).sorted(new SortUser()));
            }
        });
    }

    /**
     * Callback, when all category information are loaded
     */
    public interface CategoriesLoadedCallback {
        void onSuccess(String status);
    }

    /**
     * Gets categories from server and adds in list
     */
    public void loadCategories(CategoriesLoadedCallback categoriesLoadedCallback) {
        restClient.getServerCategories(this.server.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                JSONArray data = body.getObject().getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject categoryInfo = data.getJSONObject(i);
                    Categories categories = new Categories();
                    categories.setId(categoryInfo.getString("id"));
                    categories.setName(categoryInfo.getString("name"));
                    this.server.withCategories(categories);
                    loadChannels(categories, status1 -> {
                        loadedCategories++;
                        if (loadedCategories == data.length()) {
                            loadedCategories = 0;
                            categoriesLoadedCallback.onSuccess(status1);
                        }
                    });
                }
            }
        });
    }

    /**
     * Callback, when all channel information are loaded
     */
    public interface ChannelLoadedCallback {
        void onSuccess(String status);
    }

    /**
     * Gets all channels for a category and adds in list
     *
     * @param cat the category to load the channels from it
     */
    public void loadChannels(Categories cat, ChannelLoadedCallback channelLoadedCallback) {
        restClient.getCategoryChannels(this.server.getId(), cat.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                JSONArray data = body.getObject().getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject channelInfo = data.getJSONObject(i);
                    ServerChannel channel = new ServerChannel();
                    channel.setCurrentUser(builder.getPersonalUser());
                    channel.setId(channelInfo.getString("id"));
                    channel.setName(channelInfo.getString("name"));
                    channel.setCategories(cat);
                    boolean boolPrivilege = channelInfo.getBoolean("privileged");
                    channel.setPrivilege(boolPrivilege);

                    JSONObject json = new JSONObject(channelInfo.toString());
                    JSONArray jsonArray = json.getJSONArray("members");
                    String memberId;

                    for (int j = 0; j < jsonArray.length(); j++) {
                        memberId = jsonArray.getString(j);
                        for (User user : this.server.getUser()) {
                            if (user.getId().equals(memberId)) {
                                channel.withPrivilegedUsers(user);
                            }
                        }
                    }
                    loadChannelMessages(channel, status1 -> {
                        loadedChannel++;
                        if (loadedChannel == data.length()) {
                            loadedChannel = 0;
                            channelLoadedCallback.onSuccess(status1);
                        }
                    });
                }
            }
        });
    }

    /**
     * Callback, when all message information are loaded
     */
    public interface MessagesLoadedCallback {
        void onSuccess(String status);

    }

    private void loadChannelMessages(ServerChannel channel, MessagesLoadedCallback messagesLoadedCallback) {
        restClient.getChannelMessages(new Date().getTime(), this.server.getId(), channel.getCategories().getId(), channel.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                JSONArray data = body.getObject().getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonData = data.getJSONObject(i);
                    String from = jsonData.getString("from");
                    long timestamp = jsonData.getLong("timestamp");
                    String text = jsonData.getString("text");
                    Message message = new Message().setMessage(text).setFrom(from).setTimestamp(timestamp);
                    channel.withMessage(message);
                }
                messagesLoadedCallback.onSuccess(status);
            }
        });
    }

    public void stop() {
        try {
            if (this.serverSystemWebSocket != null) {
                if (this.serverSystemWebSocket.getSession() != null) {
                    this.serverSystemWebSocket.stop();
                }
            }
            if (this.chatWebSocketClient != null) {
                if (this.chatWebSocketClient.getSession() != null) {
                    this.chatWebSocketClient.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.serverSettings != null) {
            this.serverSettings.setOnAction(null);
        }
        if (serverMenuButton.getItems().size() > 1 && this.inviteUsers != null) {
            this.inviteUsers.setOnAction(null);
        }

        for (CategorySubController categorySubController : this.categorySubControllerList.values()) {
            categorySubController.stop();
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (textChannelLabel != null)
            textChannelLabel.setText(lang.getString("label.textChannel"));

        if (generalLabel != null)
            generalLabel.setText(lang.getString("label.general"));

        if (welcomeToAccord != null)
            welcomeToAccord.setText(lang.getString("label.welcome_to_accord"));
    }

    private void onServerSettingsClicked(ActionEvent actionEvent) {
        StageManager.showServerSettingsScreen();
    }

    private void onInviteUsersClicked(ActionEvent actionEvent) {
        StageManager.showInviteUsersScreen();
    }


    /**
     * generates new views for all categories of the server
     */
    private void generateCategoriesChannelViews() {
        Platform.runLater(() -> this.categoryBox.getChildren().clear());
        for (Categories categories : builder.getCurrentServer().getCategories()) {
            generateCategoryChannelView(categories);
        }
    }

    /**
     * generates a new view for a category with a FIXED width for the scrollPane
     */
    public void generateCategoryChannelView(Categories categories) {
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("CategorySubView.fxml")));
            view.setId(categories.getId());
            CategorySubController tempCategorySubController = new CategorySubController(view, this, categories);
            tempCategorySubController.init();
            categorySubControllerList.put(categories, tempCategorySubController);
            Platform.runLater(() -> this.categoryBox.getChildren().add(view));
        } catch (Exception e) {
            System.err.println("Error on showing Server Settings Field Screen");
            e.printStackTrace();
        }
    }

    private void checkForOwnership(String id) {
        if (!this.server.getOwner().equals(id)) {
            if (serverMenuButton.getItems().size() >= 2) {
                serverMenuButton.getItems().remove(1);
            }
        }
    }

    /**
     * reset current channel and throw user out from chat view
     */
    public void throwOutUserFromChatView() {
        setCurrentChannel(null);
        if (this.messageViewController != null) {
            this.messageViewController.stop();
        }
        Platform.runLater(() -> this.chatBox.getChildren().clear());
    }

    /**
     * refresh all channels to avoid multiple visual selected channels
     */
    public void refreshAllChannelLists() {
        for (Map.Entry<Categories, CategorySubController> entry : categorySubControllerList.entrySet()) {
            entry.getValue().refreshChannelList();
        }
    }
}
