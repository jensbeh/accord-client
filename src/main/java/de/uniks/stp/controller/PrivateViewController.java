package de.uniks.stp.controller;

import de.uniks.stp.AlternateChannelListCellFactory;
import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.WSCallback;
import de.uniks.stp.net.WebSocketClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import util.JsonUtil;
import util.SortUser;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static util.Constants.*;

public class PrivateViewController {

    private HBox root;
    private final RestClient restClient;

    private final Parent view;
    private ScrollPane scrollPaneUserBox;
    private ModelBuilder builder;
    private VBox userBox;
    private VBox currentUserBox;
    private VBox chatBox;
    private HBox viewBox;
    private ObservableList<Channel> privateChats;
    private ListView<Channel> privateChatList;
    private ListView<User> onlineUsersList;
    private static Channel selectedChat;
    private WebSocketClient USER_CLIENT;
    private WebSocketClient privateChatWebSocketCLient;
    private TextField messageField;
    private static Label welcomeToAccord;
    private ChatViewController messageViewController;

    public PrivateViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        restClient = new RestClient();
    }

    public void init() {
        root = (HBox) view.lookup("#root");
        scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        currentUserBox = (VBox) scrollPaneUserBox.getContent().lookup("#currentUserBox");
        userBox = (VBox) scrollPaneUserBox.getContent().lookup("#userBox");
        chatBox = (VBox) view.lookup("#chatBox");
        privateChatList = (ListView<Channel>) view.lookup("#privateChatList");
        privateChatList.setCellFactory(new AlternateChannelListCellFactory());
        this.privateChatList.setOnMouseReleased(this::onprivateChatListClicked);
        privateChats = FXCollections.observableArrayList();
        this.privateChatList.setItems(privateChats);
        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        this.onlineUsersList.setOnMouseReleased(this::onOnlineUsersListClicked);
        viewBox = (HBox) view.lookup("#viewBox");
        messageField = (TextField) view.lookup("#messageField");
        welcomeToAccord = (Label) view.lookup("#welcomeToAccord");
        showCurrentUser();
        showUsers();

        if (builder.getPrivateChatWebSocketCLient() == null) {
            privateChatWebSocketCLient = new WebSocketClient(builder, URI.
                    create(WS_SERVER_URL + WEBSOCKET_PATH + CHAT_WEBSOCKET_PATH + builder.
                    getPersonalUser().getName().replace(" ", "+")), new WSCallback() {
                /**
                 * handles server response
                 *
                 * @param msg is the response from the server as a JsonStructure
                 */
                @Override
                public void handleMessage(JsonStructure msg) {
                    JsonObject jsonObject = JsonUtil.parse(msg.toString());
                    System.out.println("privateChatWebSocketClient");
                    System.out.println(msg);
                    if (jsonObject.containsKey("channel") && jsonObject.getString("channel").equals("private")) {
                        Message message;
                        String channelName;
                        Boolean newChat = true;
                        if (jsonObject.getString("from").equals(builder.getPersonalUser().getName())) {
                            channelName = jsonObject.getString("to");
                            message = new Message().setMessage(jsonObject.getString("message")).
                                    setFrom(jsonObject.getString("from")).
                                    setTimestamp(jsonObject.getInt("timestamp"));
                            messageViewController.clearMessageField();
                        } else {
                            channelName = jsonObject.getString("from");
                            message = new Message().setMessage(jsonObject.getString("message")).
                                    setFrom(jsonObject.getString("from")).
                                    setTimestamp(jsonObject.getInt("timestamp"));
                        }
                        for (Channel c : builder.getPersonalUser().getPrivateChat()) {
                            if (c.getName().equals(channelName)) {
                                c.withMessage(message);
                                privateChatList.refresh();
                                newChat = false;
                                break;
                            }
                        }
                        if (newChat) {
                            String userId = "";
                            for (User user : onlineUsersList.getItems()) {
                                if (user.getName().equals(channelName)) {
                                    userId = user.getId();
                                }
                            }
                            Channel channel = new Channel().setId(userId).setName(channelName).withMessage(message);
                            builder.getPersonalUser().withPrivateChat(channel);
                            Platform.runLater(() -> privateChatList.getItems().add(channel));
                        }
                        if (messageViewController != null) {
                            ChatViewController.printMessage(message);
                        }
                    }
                    if (jsonObject.containsKey("action") && jsonObject.getString("action").equals("info")) {
                        String errorTitle;
                        String serverMessage = jsonObject.getJsonObject("data").getString("message");
                        if (serverMessage.equals("This is not your username.")) {
                            errorTitle = StageManager.getLangBundle().getString("error.username");
                        } else {
                            errorTitle = StageManager.getLangBundle().getString("error.chat");
                        }
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                            alert.setTitle(errorTitle);
                            if (serverMessage.equals("This is not your username.")) {
                                alert.setHeaderText(StageManager.getLangBundle().getString("error.this_is_not_your_username"));
                            } else {
                                alert.setHeaderText(serverMessage);
                            }
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                showUsers();
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
                            alert.setTitle(StageManager.getLangBundle().getString("error.no_connection"));
                            alert.setHeaderText(StageManager.getLangBundle().getString("error.no_connection_text"));
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                showUsers();
                            }
                        });
                    }
                }
            });
            builder.setPrivateChatWebSocketCLient(privateChatWebSocketCLient);
        } else {
            privateChatWebSocketCLient = builder.getPrivateChatWebSocketCLient();
        }
    }

    private void startWebsocketConnection() {
        try {
            USER_CLIENT = new WebSocketClient(builder,
                    new URI(WS_SERVER_URL + WEBSOCKET_PATH + SYSTEM_WEBSOCKET_PATH), new WSCallback() {
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
                        if (userName.equals(builder.getPersonalUser().getName())) {
                            Platform.runLater(StageManager::showLoginScreen);
                        }
                        List<User> userList = builder.getPersonalUser().getUser();
                        User removeUser = builder.buildUser(userName, userId);
                        if (userList.contains(removeUser)) {
                            builder.getPersonalUser().withoutUser(removeUser);
                        }
                    }
                    Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.
                            getPersonalUser().getUser()).sorted(new SortUser())));
                }

                public void onClose(Session session, CloseReason closeReason) {
                }
            });
            builder.setUSER_CLIENT(USER_CLIENT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the Online Users and reset old Online User List with new Online Users
     */
    public void showUsers() {
        restClient.getUsers(builder.getPersonalUser().getUserKey(), response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                String userId = jsonResponse.getJSONObject(i).get("id").toString();
                if (!userName.equals(builder.getPersonalUser().getName())) {
                    builder.buildUser(userName, userId);
                }
            }
            Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.getPersonalUser().
                    getUser()).sorted(new SortUser())));
        });
        startWebsocketConnection();
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
     * Event Mouseclick on an existing chat
     * Opens the existing chat and shows the messages
     *
     * @param mouseEvent is called when double clicked on an existing chat
     */
    private void onprivateChatListClicked(MouseEvent mouseEvent) {
        if (this.privateChatList.getSelectionModel().getSelectedItem() != null) {
            if (selectedChat == null || !selectedChat.equals(this.privateChatList.getSelectionModel().
                    getSelectedItem())) {
                selectedChat = this.privateChatList.getSelectionModel().getSelectedItem();
                this.privateChatList.refresh();
                MessageViews();
            }
        }
    }

    /**
     * Message View cleanup and display recent messages with selected Chat
     */
    public void MessageViews() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("ChatView.fxml"), StageManager.getLangBundle());
            messageViewController = new ChatViewController(root, builder);
            this.chatBox.getChildren().clear();
            messageViewController.init();
            this.chatBox.getChildren().add(root);

            if(PrivateViewController.getSelectedChat() != null) {
                for (Message msg : PrivateViewController.getSelectedChat().getMessage()) {
                    // Display each Message which are saved
                    ChatViewController.printMessage(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event Mouseclick on an online user
     * Create new channel if chat not existing or open the existing chat and shows the messages
     *
     * @param mouseEvent is called when clicked on an online User
     */
    private void onOnlineUsersListClicked(MouseEvent mouseEvent) {
        Channel currentChannel = selectedChat;
        if (mouseEvent.getClickCount() == 2 && this.onlineUsersList.getItems().size() != 0) {
            boolean chatExisting = false;
            String selectedUserName = this.onlineUsersList.getSelectionModel().getSelectedItem().getName();
            String selectUserId = this.onlineUsersList.getSelectionModel().getSelectedItem().getId();
            for (Channel channel : builder.getPersonalUser().getPrivateChat()) {
                if (channel.getName().equals(selectedUserName)) {
                    selectedChat = channel;
                    this.privateChatList.refresh();
                    chatExisting = true;
                    break;
                }
            }
            if (!chatExisting) {
                selectedChat = new Channel().setName(selectedUserName).setId(selectUserId);
                builder.getPersonalUser().withPrivateChat(selectedChat);
                this.privateChatList.setItems(FXCollections.observableArrayList(builder.getPersonalUser().
                        getPrivateChat()));
            }
            if (!selectedChat.equals(currentChannel))
                MessageViews();
        }
    }

    /**
     * Get the current active Channel / selected Chat
     *
     * @return current active Channel
     */
    public static Channel getSelectedChat() {
        return selectedChat;
    }


    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        this.onlineUsersList.setOnMouseReleased(null);
        this.privateChatList.setOnMouseReleased(null);
        try {
            if (USER_CLIENT != null) {
                if (USER_CLIENT.getSession() != null) {
                    USER_CLIENT.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (welcomeToAccord != null)
            welcomeToAccord.setText(lang.getString("label.welcome_to_accord"));

        ChatViewController.onLanguageChanged();
    }
}
