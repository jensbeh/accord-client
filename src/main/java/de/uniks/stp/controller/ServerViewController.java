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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import static util.Constants.*;

/**
 * The class ServerViewController is about showing the ServerView. It is used to update the builder.
 */
public class ServerViewController {

    private final RestClient restClient;
    private static Server server;
    private final Parent view;
    private HBox root;
    private ScrollPane scrollPaneUserBox;
    private ModelBuilder builder;
    private VBox channelBox;
    private VBox textChannelBox;
    private Label serverNameText;
    private static Label textChannelLabel;
    private static Label generalLabel;
    private static Label welcomeToAccord;
    private TextField sendTextField;
    private static Button sendMessageButton;
    private ListView<User> onlineUsersList;
    private ListView<User> offlineUsersList;
    private VBox userBox;
    private VBox currentUserBox;
    private WebSocketClient SERVER_USER;
    private WebSocketClient serverChatWebSocketClient;
    private VBox messages;
    private ChatViewController messageViewController;
    private ListView<Channel> serverChatList;
    private static String channelId;
    private static String channelName;

    public static String channelId() {
        return channelId;
    }

    /**
     * "ServerViewController takes Parent view, ModelBuilder modelBuilder, Server server.
     * It also creates a new restClient"
     */
    public ServerViewController(Parent view, ModelBuilder modelBuilder, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
        restClient = new RestClient();
    }

    /**
     * Initialise all view parameters
     */
    public void init() throws InterruptedException {
        root = (HBox) view.lookup("#root");
        channelBox = (VBox) view.lookup("#channelBox");
        serverNameText = (Label) view.lookup("#serverName");
        serverNameText.setText(server.getName());
        textChannelLabel = (Label) view.lookup("#textChannel");
        generalLabel = (Label) view.lookup("#general");
        welcomeToAccord = (Label) view.lookup("#welcomeToAccord");
        textChannelBox = (VBox) view.lookup("#textChannelBox");
        scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        currentUserBox = (VBox) scrollPaneUserBox.getContent().lookup("#currentUserBox");
        userBox = (VBox) scrollPaneUserBox.getContent().lookup("#userBox");
        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        offlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#offlineUsers");
        offlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        messages = (VBox) view.lookup("#chatBox");

        showCurrentUser();
        loadServerInfos(new ServerInfoCallback() {
            @Override
            public void onSuccess(String status) {
                loadServerChannel();
            }
        }); // members & (categories)
        showServerUsers();
        Platform.runLater(this::showMessageView);

        serverChatWebSocketClient = new WebSocketClient(builder, URI.
                create(WS_SERVER_URL + WEBSOCKET_PATH + CHAT_WEBSOCKET_PATH + builder.
                        getPersonalUser().getName().replace(" ", "+") + SERVER_WEBSOCKET_PATH +
                        server.getId()), new WSCallback() {
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
                if (jsonObject.containsKey("channel") && jsonObject.getString("channel").equals(channelId())) {
                    Message message = null;
                    if (jsonObject.getString("from").equals(builder.getPersonalUser().getName())) {
                        message = new Message().setMessage(jsonObject.getString("text")).
                                setFrom(jsonObject.getString("from")).
                                setTimestamp(jsonObject.getInt("timestamp")).
                                setChannel(builder.getCurrentServerChannel());
                        if (messageViewController != null) {
                            Platform.runLater(() -> messageViewController.clearMessageField());
                        }
                    }
                    if (messageViewController != null) {
                        assert message != null;
                        builder.getCurrentServer().getCategories().get(0).getChannel().get(0).withMessage(message);
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
                            showServerUsers();
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
                            showServerUsers();
                        }
                    });
                }
            }
        });
        builder.setServerChatWebSocketClient(serverChatWebSocketClient);
    }

    private void loadServerChannel() {
        restClient.getCategoryChannels(server.getId(), builder.getCurrentServer().getCategories().get(0).getId(),
                builder.getPersonalUser().getUserKey(), response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        JSONArray ob = body.getObject().getJSONArray("data");
                        JSONObject object = ob.getJSONObject(0);
                        channelId = object.get("id").toString();
                        channelName = object.get("name").toString();
                        if (builder.getCurrentServer().getCategories().get(0).getChannel().isEmpty()) {
                            Channel channel = new Channel().setId(channelId).setName(channelName);
                            builder.getCurrentServer().getCategories().get(0).withChannel(channel);
                            builder.setCurrentServerChannel(channel);
                        }
                    } else if (status.equals("failure")) {
                        System.out.println(body.getObject().getString("message"));
                    }
                });
    }

    private void showMessageView() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("ChatView.fxml"), StageManager.getLangBundle());
            messageViewController = new ChatViewController(root, builder);
            this.messages.getChildren().clear();
            messageViewController.init();
            this.messages.getChildren().add(root);
            if (builder.getCurrentServer() != null) {
                if (builder.getCurrentServer().getCategories().size() != 0) {
                    if (builder.getCurrentServer().getCategories().get(0) != null) {
                        if (builder.getCurrentServer().getCategories().get(0).getChannel().size() != 0) {
                            for (Message msg : builder.getCurrentServer().getCategories().get(0).getChannel().get(0).getMessage()) {
                                // Display each Message which are saved
                                ChatViewController.printMessage(msg);
                            }
                        }
                    }
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
        restClient.getServerUsers(server.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                JSONArray category = body.getObject().getJSONObject("data").getJSONArray("categories");
                JSONArray members = body.getObject().getJSONObject("data").getJSONArray("members");
                if (builder.getCurrentServer().getCategories().size() == 0) {
                    for (int i = 0; i < category.length(); i++) {
                        Categories categories = new Categories();
                        categories.setId(category.getString(i));
                        builder.getCurrentServer().withCategories(categories);
                    }
                }
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    String id = member.getString("id");
                    String name = member.getString("name");
                    boolean online = member.getBoolean("online");
                    builder.buildServerUser(name, id, online);
                }
            } else if (status.equals("failure")) {
                System.out.println(body.getObject().getString("message"));
            }
            serverInfoCallback.onSuccess(status);
        });
    }

    /**
     * Get Server Users and set them in Online User List
     */
    private void showServerUsers() {
        try {
            SERVER_USER = new WebSocketClient(builder, new URI(WS_SERVER_URL + WEBSOCKET_PATH + SERVER_SYSTEM_WEBSOCKET_PATH + builder.getCurrentServer().getId()), new WSCallback() {

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
                        if (userName.equals(builder.getPersonalUser().getName())) {
                            Platform.runLater(StageManager::showLoginScreen);
                        }
                        builder.buildServerUser(userName, userId, false);
                    }
                    showOnlineOfflineUsers();
                }

                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println(closeReason.getCloseCode().toString());
                    if (!closeReason.getCloseCode().toString().equals("NORMAL_CLOSURE")) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, StageManager.getLangBundle().getString("error.user_cannot_be_displayed"), ButtonType.OK);
                            alert.setTitle(StageManager.getLangBundle().getString("error.dialog"));
                            alert.setHeaderText(StageManager.getLangBundle().getString("error.no_connection"));
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                showServerUsers();
                            }
                        });
                    }
                }
            });
            builder.setSERVER_USER(SERVER_USER);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        showOnlineOfflineUsers();
    }

    /**
     * Split Users into offline and online users then update the list
     */
    public void showOnlineOfflineUsers() {
        ArrayList<User> onlineUsers = new ArrayList<>();
        ArrayList<User> offlineUsers = new ArrayList<>();
        for (User user : builder.getCurrentServer().getUser()) {
            if (user.isStatus()) {
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

    public void stop() {
        onlineUsersList.setItems(null);
        offlineUsersList.setItems(null);
        try {
            if (SERVER_USER != null) {
                if (SERVER_USER.getSession() != null) {
                    SERVER_USER.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Server getSelectedServer() {
        return server;
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

}
