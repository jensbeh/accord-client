package de.uniks.stp.controller;


import de.uniks.stp.AlternatePrivateChatListCellFactory;
import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.model.User;
import de.uniks.stp.net.PrivateChatWebSocket;
import de.uniks.stp.net.PrivateSystemWebSocketClient;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import util.ResourceManager;
import util.SortUser;

import javax.json.JsonException;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.ResourceBundle;

import static util.Constants.*;

public class PrivateViewController {

    private final RestClient restClient;

    private final Parent view;
    private final ModelBuilder builder;
    private HBox root;
    private VBox currentUserBox;
    private VBox audioConnectionBox;
    private Button disconnectAudioButton;
    private VBox chatBox;
    private ListView<PrivateChat> privateChatList;
    private ListView<User> onlineUsersList;
    private static PrivateChat selectedChat;
    private static Label welcomeToAccord;
    private ChatViewController messageViewController;
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;
    private PrivateChatWebSocket privateChatWebSocket;

    public PrivateViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        restClient = modelBuilder.getRestClient();
        this.privateSystemWebSocketClient = modelBuilder.getUSER_CLIENT();
        this.privateChatWebSocket = modelBuilder.getPrivateChatWebSocketClient();
    }

    public ChatViewController getMessageViewController() {
        return messageViewController;
    }

    @SuppressWarnings("unchecked")
    public void init() {
        root = (HBox) view.lookup("#root");
        //ScrollPane scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        currentUserBox = (VBox) view.lookup("#currentUserBox");
        audioConnectionBox = (VBox) view.lookup("#audioConnectionBox");
        chatBox = (VBox) view.lookup("#chatBox");
        privateChatList = (ListView<PrivateChat>) view.lookup("#privateChatList");
        privateChatList.setCellFactory(new AlternatePrivateChatListCellFactory());
        AlternatePrivateChatListCellFactory.setTheme(builder.getTheme());
        this.privateChatList.setOnMouseReleased(this::onPrivateChatListClicked);
        ObservableList<PrivateChat> privateChats = FXCollections.observableArrayList();
        this.privateChatList.setItems(privateChats);
        onlineUsersList = (ListView<User>) view.lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        this.onlineUsersList.setOnMouseReleased(this::onOnlineUsersListClicked);
        welcomeToAccord = (Label) view.lookup("#welcomeToAccord");
        showCurrentUser();
        showUsers();

        if (privateChatWebSocket == null) {
            privateChatWebSocket = new PrivateChatWebSocket(URI.create(WS_SERVER_URL + WEBSOCKET_PATH + CHAT_WEBSOCKET_PATH + builder.
                    getPersonalUser().getName().replace(" ", "+")), builder.getPersonalUser().getUserKey());
        }
        privateChatWebSocket.setBuilder(builder);
        privateChatWebSocket.setPrivateViewController(this);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
    }

    private void startWebSocketConnection() {
        if (privateSystemWebSocketClient == null) {
            privateSystemWebSocketClient = new PrivateSystemWebSocketClient(URI.create(WS_SERVER_URL + WEBSOCKET_PATH + SYSTEM_WEBSOCKET_PATH), builder.getPersonalUser().getUserKey());
            privateSystemWebSocketClient.setBuilder(builder);
            privateSystemWebSocketClient.setPrivateViewController(this);
        }
        privateSystemWebSocketClient.setPrivateViewController(this);
        privateSystemWebSocketClient.setBuilder(builder);
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
    }


    public ListView<PrivateChat> getPrivateChatList() {
        return privateChatList;
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
                } else {
                    builder.getPersonalUser().setId(userId);
                }
            }
            Platform.runLater(() -> onlineUsersList.setItems(FXCollections.observableList(builder.getPersonalUser().
                    getUser()).sorted(new SortUser())));
        });
        startWebSocketConnection();
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
     * Display AudioConnectedBox
     */
    public void showAudioConnectedBox() {
        if (builder.getCurrentAudioChannel() != null) {
            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("AudioConnectedBox.fxml")));
                AudioConnectedBoxController audioConnectedBoxController = new AudioConnectedBoxController(root);
                audioConnectedBoxController.init();
                audioConnectedBoxController.setServerName(builder.getCurrentAudioChannel().getCategories().getServer().getName());
                audioConnectedBoxController.setAudioChannelName(builder.getCurrentAudioChannel().getName());

                Platform.runLater(() -> {
                    this.audioConnectionBox.getChildren().clear();
                    this.audioConnectionBox.getChildren().add(root);
                    disconnectAudioButton = (Button) view.lookup("#button_disconnectAudio");
                    disconnectAudioButton.setOnAction(this::onAudioDisconnectClicked);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this.audioConnectionBox.getChildren().size() > 0) {
            this.audioConnectionBox.getChildren().clear();
        }
    }

    /**
     * when audio disconnect button is clicked
     */
    private void onAudioDisconnectClicked(ActionEvent actionEvent) {
        builder.getRestClient().leaveVoiceChannel(builder.getCurrentAudioChannel().getCategories().getServer().getId(), builder.getCurrentAudioChannel().getCategories().getId(), builder.getCurrentAudioChannel().getId(), builder.getPersonalUser().getUserKey(), response -> {
            this.disconnectAudioButton.setOnAction(null);

            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                System.out.println(body);
            }
        });

        Platform.runLater(() -> {
            this.audioConnectionBox.getChildren().clear();
        });
    }

    /**
     * Event Mouseclick on an existing chat
     * Opens the existing chat and shows the messages
     *
     * @param mouseEvent is called when double clicked on an existing chat
     */
    private void onPrivateChatListClicked(MouseEvent mouseEvent) {
        if (this.privateChatList.getSelectionModel().getSelectedItem() != null) {
            if (selectedChat == null || !selectedChat.equals(this.privateChatList.getSelectionModel().
                    getSelectedItem())) {
                selectedChat = this.privateChatList.getSelectionModel().getSelectedItem();
                if (selectedChat.getUnreadMessagesCounter() > 0) {
                    selectedChat.setUnreadMessagesCounter(0);
                }
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
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("ChatView.fxml")), StageManager.getLangBundle());
            messageViewController = new ChatViewController(root, builder);
            this.chatBox.getChildren().clear();
            messageViewController.init();
            messageViewController.setTheme();
            this.chatBox.getChildren().add(root);

            if (PrivateViewController.getSelectedChat() != null) {
                for (Message msg : PrivateViewController.getSelectedChat().getMessage()) {
                    // Display each Message which are saved
                    ChatViewController.printMessage(msg);
                }
            }
        } catch (IOException | JsonException e) {
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
        PrivateChat currentChannel = selectedChat;
        if (mouseEvent.getClickCount() == 2 && this.onlineUsersList.getItems().size() != 0) {
            boolean chatExisting = false;
            String selectedUserName = this.onlineUsersList.getSelectionModel().getSelectedItem().getName();
            String selectUserId = this.onlineUsersList.getSelectionModel().getSelectedItem().getId();
            for (PrivateChat channel : builder.getPersonalUser().getPrivateChat()) {
                if (channel.getName().equals(selectedUserName)) {
                    selectedChat = channel;
                    if (selectedChat.getUnreadMessagesCounter() > 0) {
                        selectedChat.setUnreadMessagesCounter(0);
                    }
                    this.privateChatList.refresh();
                    chatExisting = true;
                    break;
                }
            }
            if (!chatExisting) {
                selectedChat = new PrivateChat().setName(selectedUserName).setId(selectUserId);
                builder.getPersonalUser().withPrivateChat(selectedChat);
                try {
                    // load messages for new channel
                    selectedChat.withMessage(ResourceManager.loadPrivatChat(builder.getPersonalUser().getName(), selectedChat.getName(), selectedChat));
                } catch (IOException | JsonException | com.github.cliftonlabs.json_simple.JsonException e) {
                    e.printStackTrace();
                }
                this.privateChatList.setItems(FXCollections.observableArrayList(builder.getPersonalUser().
                        getPrivateChat()));
            }
            if (!selectedChat.equals(currentChannel)) {
                MessageViews();
            }
        }
    }

    /**
     * Get the current active Channel / selected Chat
     *
     * @return current active Channel
     */
    public static PrivateChat getSelectedChat() {
        return selectedChat;
    }

    public static void setSelectedChat(PrivateChat channel) {
        selectedChat = channel;
    }

    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        this.onlineUsersList.setOnMouseReleased(null);
        this.privateChatList.setOnMouseReleased(null);
        try {
            if (privateSystemWebSocketClient != null) {
                if (privateSystemWebSocketClient.getSession() != null) {
                    privateSystemWebSocketClient.stop();
                }
            }
            if (privateChatWebSocket != null) {
                if (privateChatWebSocket.getSession() != null) {
                    privateChatWebSocket.stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ListView<User> getOnlineUsersList() {
        return onlineUsersList;
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

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/bright/PrivateView.css")).toExternalForm());
        if (messageViewController != null) {
            messageViewController.setTheme();
        }
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/dark/PrivateView.css")).toExternalForm());
        if (messageViewController != null) {
            messageViewController.setTheme();
        }
    }
}
