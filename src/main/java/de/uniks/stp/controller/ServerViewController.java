package de.uniks.stp.controller;

import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.*;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.WSCallback;
import de.uniks.stp.net.WebSocketClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import kong.unirest.JsonNode;
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
import java.util.*;

import static util.Constants.*;

/**
 * The class ServerViewController is about showing the ServerView. It is used to update the builder.
 */
public class ServerViewController {

    private static ModelBuilder builder;
    private final RestClient restClient;
    private Server server;
    private final Parent view;
    private ScrollPane scrollPaneUserBox;
    private MenuButton serverMenuButton;
    private static Label textChannelLabel;
    private static Label generalLabel;
    private static Label welcomeToAccord;
    private static Button sendMessageButton;
    private ListView<User> onlineUsersList;
    private ListView<User> offlineUsersList;
    private VBox currentUserBox;
    private WebSocketClient systemWebSocketClient;
    private WebSocketClient chatWebSocketClient;
    private VBox chatBox;
    private ChatViewController messageViewController;
    private MenuItem serverSettings;
    private MenuItem inviteUsers;
    private static Map<Categories, CategorySubController> categorySubControllerList;
    private VBox categoryBox;
    private ScrollPane scrollPaneCategories;

    /**
     * "ServerViewController takes Parent view, ModelBuilder modelBuilder, Server server.
     * It also creates a new restClient"
     */
    public ServerViewController(Parent view, ModelBuilder modelBuilder, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
        this.restClient = new RestClient();
    }

    public static Channel getSelectedChat() {
        return builder.getCurrentServerChannel();
    }

    public static void setSelectedChat(Channel Chat) {
        builder.setCurrentServerChannel(Chat);
    }

    /**
     * Initialise all view parameters
     */
    public void startController() {
        serverMenuButton = (MenuButton) view.lookup("#serverMenuButton");
        scrollPaneCategories = (ScrollPane) view.lookup("#scrollPaneCategories");
        categoryBox = (VBox) scrollPaneCategories.getContent().lookup("#categoryVbox");
        textChannelLabel = (Label) view.lookup("#textChannel");
        generalLabel = (Label) view.lookup("#general");
        welcomeToAccord = (Label) view.lookup("#welcomeToAccord");
        scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        currentUserBox = (VBox) scrollPaneUserBox.getContent().lookup("#currentUserBox");
        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        offlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#offlineUsers");
        offlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        chatBox = (VBox) view.lookup("#chatBox");
        categorySubControllerList = new HashMap<>();

        loadServerInfos(new ServerInfoCallback() {
            @Override
            public void onSuccess(String status) {
                if (status.equals("success")) {
                    loadCategories();
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
        System.out.println(this.server.getName());
        serverMenuButton.setText(this.server.getName());
        serverSettings = serverMenuButton.getItems().get(0);
        serverSettings.setOnAction(this::onServerSettingsClicked);
        inviteUsers = serverMenuButton.getItems().get(1);
        inviteUsers.setOnAction(this::onInviteUsersClicked);
        builder.setServerChatWebSocketClient(this.chatWebSocketClient);

        showCurrentUser();
        showOnlineOfflineUsers();

        Platform.runLater(this::generateCategoriesChannelViews);
        if (builder.getCurrentServerChannel() != null) {
            showMessageView();
        }
    }

    /**
     * Get Server Users and set them in Online User List
     */
    private void buildSystemWebSocket() {
        try {
            systemWebSocketClient = new WebSocketClient(builder, URI.
                    create(WS_SERVER_URL + WEBSOCKET_PATH + SERVER_SYSTEM_WEBSOCKET_PATH + this.server.getId()),
                    new WSCallback() {

                        @Override
                        public void handleMessage(JsonStructure msg) {
                            System.out.println("msg: " + msg);
                            JsonObject jsonMsg = JsonUtil.parse(msg.toString());
                            String userAction = jsonMsg.getString("action");
                            JsonObject jsonData = jsonMsg.getJsonObject("data");
                            String userName = jsonData.getString("name");
                            String userId = jsonData.getString("id");

                            if (userAction.equals("categoryCreated")) {
                                createCategory(jsonData);
                            }
                            if (userAction.equals("categoryDeleted")) {
                                deleteCategory(jsonData);
                            }
                            if (userAction.equals("categoryUpdated")) {
                                updateCategory(jsonData);
                            }

                            if (userAction.equals("userJoined")) {
                                builder.buildServerUser(server, userName, userId, true);
                            }
                            if (userAction.equals("userLeft")) {
                                if (userName.equals(builder.getPersonalUser().getName()) && builder.getCurrentServer() == server) {
                                    Platform.runLater(StageManager::showLoginScreen);
                                }
                                builder.buildServerUser(server, userName, userId, false);
                            }
                            if (builder.getCurrentServer() == server) {
                                showOnlineOfflineUsers();
                            }
                        }

                        @Override
                        public void onClose(Session session, CloseReason closeReason) {
                            System.out.println(closeReason.getCloseCode().toString());
                            if (!closeReason.getCloseCode().toString().equals("NORMAL_CLOSURE")) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, StageManager.getLangBundle().getString("error.user_cannot_be_displayed"), ButtonType.OK);
                                    alert.setTitle(StageManager.getLangBundle().getString("error.dialog"));
                                    alert.setHeaderText(StageManager.getLangBundle().getString("error.no_connection"));
                                    Optional<ButtonType> result = alert.showAndWait();
                                    if (result.isPresent() && result.get() == ButtonType.OK) {
                                        buildSystemWebSocket();
                                    }
                                });
                            }
                        }
                    });
            //builder.setSERVER_USER(systemWebSocketClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildChatWebSocket() {
        chatWebSocketClient = new WebSocketClient(builder, URI.
                create(WS_SERVER_URL + WEBSOCKET_PATH + CHAT_WEBSOCKET_PATH + builder.
                        getPersonalUser().getName().replace(" ", "+") + SERVER_WEBSOCKET_PATH + this.server.getId()),
                new WSCallback() {
                    /**
                     * handles server response
                     *
                     * @param msg is the response from the server as a JsonStructure
                     */
                    @Override
                    public void handleMessage(JsonStructure msg) {
                        JsonObject jsonObject = JsonUtil.parse(msg.toString());
                        System.out.println("serverChatWebSocketClient");
                        System.out.println(msg);

                        if (jsonObject.containsKey("channel")) {
                            Message message = null;
                            String id = jsonObject.getString("id");
                            String channelId = jsonObject.getString("channel");
                            int timestamp = jsonObject.getInt("timestamp");
                            String from = jsonObject.getString("from");
                            String text = jsonObject.getString("text");

                            // currentUser send
                            if (from.equals(builder.getPersonalUser().getName())) {
                                message = new Message().setMessage(text).
                                        setFrom(from).
                                        setTimestamp(timestamp).
                                        setChannel(builder.getCurrentServerChannel());
                                if (messageViewController != null && builder.getCurrentServer() == server) {
                                    Platform.runLater(() -> messageViewController.clearMessageField());
                                }
                            }
                            // currentUser received
                            else if (!from.equals(builder.getPersonalUser().getName())) {
                                message = new Message().setMessage(text).
                                        setFrom(from).
                                        setTimestamp(timestamp).
                                        setChannel(builder.getCurrentServerChannel());
                                if (messageViewController != null && builder.getCurrentServer() == server) {
                                    Platform.runLater(() -> messageViewController.clearMessageField());
                                }

                                for (Categories categories : server.getCategories()) {
                                    for (Channel channel : categories.getChannel()) {
                                        if (channel.getId().equals(channelId)) {
                                            channel.withMessage(message);
                                            if (builder.getCurrentServerChannel() == null || channel != builder.getCurrentServerChannel()) {
                                                channel.setUnreadMessagesCounter(channel.getUnreadMessagesCounter() + 1);
                                            }
                                            if (builder.getCurrentServer() == server) {
                                                categorySubControllerList.get(categories).refreshChannelList();
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            if (messageViewController != null && builder.getCurrentServer() == server) {
                                assert message != null;
                                builder.getCurrentServerChannel().withMessage(message);
                                ChatViewController.printMessage(message);
                            }
                        }
                        if (jsonObject.containsKey("action") && jsonObject.getString("action").equals("info")) {
                            String errorTitle;
                            String serverMessage = jsonObject.getJsonObject("data").getString("message");
                            if (serverMessage.equals("This is not your username.")) {
                                errorTitle = "Username Error";
                            } else {
                                errorTitle = "Chat Error";
                            }
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                                alert.setTitle(errorTitle);
                                alert.setHeaderText(serverMessage);
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.isPresent() && result.get() == ButtonType.OK) {
                                    buildSystemWebSocket();
                                }
                            });
                        }
                    }

                    @Override
                    public void onClose(Session session, CloseReason closeReason) {
                        System.out.println(closeReason.getCloseCode().toString());
                        if (!closeReason.getCloseCode().toString().equals("NORMAL_CLOSURE")) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
                                alert.setTitle("No Connection Error");
                                alert.setHeaderText("No Connection - Please check and try again later");
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.isPresent() && result.get() == ButtonType.OK) {
                                    buildSystemWebSocket();
                                }
                            });
                        }
                    }
                });
        //builder.setServerChatWebSocketClient(serverChatWebSocketClient);
    }

    /**
     * Initial Chat View and load chat history which is saved in list
     */
    public void showMessageView() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("ChatView.fxml"), StageManager.getLangBundle());
            this.messageViewController = new ChatViewController(root, builder);
            this.chatBox.getChildren().clear();
            this.messageViewController.init();
            this.chatBox.getChildren().add(root);

            if (builder.getCurrentServer() != null && builder.getCurrentServerChannel() != null) {
                for (Message msg : builder.getCurrentServerChannel().getMessage()) {
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
            Parent root = FXMLLoader.load(StageManager.class.getResource("UserProfileView.fxml"));
            UserProfileController userProfileController = new UserProfileController(root, builder);
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

    public void loadServerInfos(ServerInfoCallback serverInfoCallback) {
        restClient.getServerUsers(this.server.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            System.out.println(status);
            this.server.setOwner(body.getObject().getJSONObject("data").getString("owner"));
            //builder.getCurrentServer().setOwner(body.getObject().getJSONObject("data").getString("owner"));
            if (status.equals("success")) {
                JSONArray members = body.getObject().getJSONObject("data").getJSONArray("members");
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    String id = member.getString("id");
                    String name = member.getString("name");
                    boolean online = member.getBoolean("online");
                    builder.buildServerUser(server, name, id, online);
                }
                serverInfoCallback.onSuccess(status);
            } else if (status.equals("failure")) {
                System.out.println(body.getObject().getString("message"));
            }
        });
    }

    /**
     * adds a new Controller for a new Category with new view
     */
    private void createCategory(JsonObject jsonData) {
        String serverId = jsonData.getString("server");
        String categoryId = jsonData.getString("id");
        String name = jsonData.getString("name");
        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                boolean found = false;
                for (Categories categories : server.getCategories()) {
                    if (categories.getId().equals(categoryId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Categories category = new Categories().setName(name).setId(categoryId);
                    server.withCategories(category);
                    if (builder.getCurrentServer() == server) { //TODO vielleicht nicht ausreichend um Map zu erweitern
                        generateCategoryChannelView(category);
                    }
                }
            }
        }
    }

    /**
     * deletes a category with controller and view
     */
    private void deleteCategory(JsonObject jsonData) {
        String serverId = jsonData.getString("this.server");
        String categoryId = jsonData.getString("id");
        String name = jsonData.getString("name");

        Categories deletedCategory = new Categories().setName(name).setId(categoryId);
        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                for (Categories categories : server.getCategories()) {
                    if (categories.getId().equals(deletedCategory.getId())) {
                        server.withoutCategories(categories);
                        if (builder.getCurrentServer() == this.server) { //TODO vllt nicht ausreichend wegen Map
                            for (Node view : categoryBox.getChildren()) {
                                if (view.getId().equals(deletedCategory.getId())) {
                                    Platform.runLater(() -> this.categoryBox.getChildren().remove(view));
                                    categorySubControllerList.get(categories).stop();
                                    categorySubControllerList.remove(categories);

                                    if (deletedCategory.getChannel().contains(builder.getCurrentServerChannel()) || builder.getCurrentServer().getCategories().size() == 0) {
                                        builder.setCurrentServerChannel(null);
                                        setSelectedChat(null);
                                        messageViewController.stop();
                                        Platform.runLater(() -> this.chatBox.getChildren().clear());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * rename a Category and update it on the view
     */
    private void updateCategory(JsonObject jsonData) {
        String serverId = jsonData.getString("server");
        String categoryId = jsonData.getString("id");
        String name = jsonData.getString("name");

        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                for (Categories categories : server.getCategories()) {
                    if (categories.getId().equals(categoryId) && !categories.getName().equals(name)) {
                        categories.setName(name);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Split Users into offline and online users then update the list
     */
    public void showOnlineOfflineUsers() {
        ArrayList<User> onlineUsers = new ArrayList<>();
        ArrayList<User> offlineUsers = new ArrayList<>();
        for (User user : builder.getCurrentServer().getUser()) {
            if (user.isStatus()) {
                if (user.getName().equals(builder.getPersonalUser().getName())) {
                    checkForOwnership(user.getId());
                }
                onlineUsers.add(user);
            } else {
                offlineUsers.add(user);
            }
        }
        Platform.runLater(() -> {
            onlineUsersList.prefHeightProperty().bind(onlineUsersList.fixedCellSizeProperty().multiply(onlineUsers.size()));
            offlineUsersList.prefHeightProperty().bind(offlineUsersList.fixedCellSizeProperty().multiply(offlineUsers.size()));
            onlineUsersList.setItems(FXCollections.observableList(onlineUsers).sorted(new SortUser()));
            offlineUsersList.setItems(FXCollections.observableList(offlineUsers).sorted(new SortUser()));
        });
    }

    /**
     * Gets categories from server and adds in list
     */
    public void loadCategories() {
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
                    //builder.getCurrentServer().withCategories(categories);
                    loadChannels(categories);
                }
            }
        });
    }

    /**
     * Gets all channels for a category and adds in list
     *
     * @param cat the category to load the channels from it
     */
    public void loadChannels(Categories cat) {
        restClient.getCategoryChannels(this.server.getId(), cat.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                JSONArray data = body.getObject().getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject channelInfo = data.getJSONObject(i);
                    Channel channel = new Channel();
                    channel.setCurrentUser(builder.getPersonalUser());
                    channel.setId(channelInfo.getString("id"));
                    channel.setName(channelInfo.getString("name"));
                    channel.setCategories(cat);
                    loadChannelMessages(channel);
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
                }
            }
        });
    }

    private void loadChannelMessages(Channel channel) {
        System.out.println(new Date().getTime());
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
            }
        });
    }

    public void stop() {
        onlineUsersList.setItems(null);
        offlineUsersList.setItems(null);
        try {
            if (systemWebSocketClient != null) {
                if (systemWebSocketClient.getSession() != null) {
                    systemWebSocketClient.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSettings.setOnAction(null);
        inviteUsers.setOnAction(null);

        for (CategorySubController categorySubController : categorySubControllerList.values()) {
            categorySubController.stop();
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (textChannelLabel != null)
            textChannelLabel.setText(lang.getString("label.textchannel"));

        if (generalLabel != null)
            generalLabel.setText(lang.getString("label.general"));

        if (welcomeToAccord != null)
            welcomeToAccord.setText(lang.getString("label.welcome_to_accord"));

        if (sendMessageButton != null)
            sendMessageButton.setText(lang.getString("button.send"));
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
    private void generateCategoryChannelView(Categories categories) {
        try {
            Parent view = FXMLLoader.load(StageManager.class.getResource("CategorySubView.fxml"));
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
            serverMenuButton.getItems().remove(1);
        }
    }
}
