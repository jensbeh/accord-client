package de.uniks.stp.net.websocket.privatesocket;

import com.github.cliftonlabs.json_simple.JsonException;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.home.PrivateViewController;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.model.User;
import de.uniks.stp.net.websocket.CustomWebSocketConfigurator;
import de.uniks.stp.util.JsonUtil;
import de.uniks.stp.util.ResourceManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class PrivateChatWebSocket extends Endpoint {

    public final String COM_NOOP = "noop";
    private final Timer noopTimer;
    private Session session;
    private ModelBuilder builder;
    private PrivateViewController privateViewController;
    private ChatViewController chatViewController;

    public PrivateChatWebSocket(URI endpoint, String userKey) {
        this.noopTimer = new Timer();
        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new CustomWebSocketConfigurator(userKey))
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientConfig, endpoint);
        } catch (Exception e) {
            System.err.println("Error during establishing WebSocket connection:");
        }
    }

    public PrivateViewController getPrivateViewController() {
        return privateViewController;
    }

    public void setPrivateViewController(PrivateViewController privateViewController) {
        this.privateViewController = privateViewController;
    }

    public ModelBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // Store session
        this.session = session;
        // add MessageHandler
        this.session.addMessageHandler(String.class, this::onMessage);

        this.noopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Send NOOP Message
                try {
                    sendMessage(COM_NOOP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000 * 30);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        // cancel timer
        try {
            this.noopTimer.cancel();
        } catch (Exception e) {
            e.addSuppressed(new NullPointerException());
        }
        // set session null
        this.session = null;
        System.out.println(closeReason.getCloseCode().toString());
        if (!closeReason.getCloseCode().toString().equals("NORMAL_CLOSURE")) {
            Platform.runLater(this::showNoConnectionAlert);
        }
        super.onClose(session, closeReason);
    }

    public void showNoConnectionAlert() {
        try {
            ResourceBundle lang = builder.getStageManager().getLangBundle();

            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("alert/ConnectionLost.fxml")), builder.getStageManager().getLangBundle());
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.initOwner(builder.getStageManager().getHomeViewController().getHomeView().getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

            // DropShadow of Scene
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/DropShadow/DropShadow.css")).toExternalForm());

            // create titleBar
            HBox titleBarBox = (HBox) root.lookup("#titleBarBox");
            Parent titleBarView = null;
            try {
                titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBarView.fxml")), builder.getStageManager().getLangBundle());
            } catch (IOException e) {
                e.printStackTrace();
            }
            titleBarBox.getChildren().add(titleBarView);
            TitleBarController titleBarController = new TitleBarController(stage, titleBarView, builder);
            titleBarController.init();
            titleBarController.setTheme();
            titleBarController.setMaximizable(false);
            titleBarController.setTitle(lang.getString("label.warning"));
            stage.setTitle(lang.getString("label.warning"));

            stage.setScene(scene);
            stage.show();

            Label noConnectionLabel = (Label) root.lookup("#label_noConnection");
            noConnectionLabel.setText(lang.getString("error.no_connection"));
            Button okButton = (Button) root.lookup("#button_OK");
            okButton.setOnAction((a) -> {
                stage.close();
                builder.getStageManager().showLoginScreen();
            });
            stage.setOnCloseRequest((a) -> builder.getStageManager().showLoginScreen());
            if (builder.getTheme().equals("Bright")) {
                root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Alert.css")).toExternalForm());
            } else {
                root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Alert.css")).toExternalForm());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMessage(String message) {
        // Process Message
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        // Use callback to handle it
        this.handleMessage(jsonObject);
    }

    public void sendMessage(String message) throws IOException {
        // check if session is still open
        if (this.session != null && this.session.isOpen()) {
            // send message
            this.session.getBasicRemote().sendText(message);
            this.session.getBasicRemote().flushBatch();
        }
    }

    public void stop() throws IOException {
        // cancel timer
        this.noopTimer.cancel();
        // close session
        this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "NORMAL_CLOSURE"));
    }

    public Session getSession() {
        return session;
    }

    public void setChatViewController(ChatViewController chatViewController) {
        this.chatViewController = chatViewController;
    }

    private boolean isBlocked(String name) {
        for (User user : builder.getBlockedUsers()) {
            if (user.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void handleMessage(JsonStructure msg) {
        JsonObject jsonObject = JsonUtil.parse(msg.toString());
        System.out.println("privateChatWebSocketClient");
        System.out.println(msg);
        if (jsonObject.containsKey("channel") && jsonObject.getString("channel").equals("private")) {
            privateMessage(jsonObject);
        }
        if (jsonObject.containsKey("action") && jsonObject.getString("action").equals("info")) {
            showChatAlert(jsonObject);
        }
    }

    private void privateMessage(JsonObject jsonObject) {
        Message message;
        String channelName;
        // currentUser send
        long timestamp = new Date().getTime();
        if (jsonObject.getString("from").equals(builder.getPersonalUser().getName())) {
            channelName = jsonObject.getString("to");
            privateViewController.getChatViewController().clearMessageField();
        } else { // currentUser received
            channelName = jsonObject.getString("from");
            if (isBlocked(channelName)) { // if user is blocked, block the message
                return;
            }
        }
        message = new Message().setMessage(jsonObject.getString("message")).
                setFrom(jsonObject.getString("from")).
                setTimestamp(timestamp);
        boolean newChat = checkIfNewChat(channelName, message);
        if (newChat) {
            createNewChat(channelName, message);
        }
        saveMessage(message);
        if (privateViewController.getChatViewController() != null) {
            Platform.runLater(() -> chatViewController.printMessage(message));
        }
    }

    private boolean checkIfNewChat(String channelName, Message message) {
        for (PrivateChat channel : builder.getPersonalUser().getPrivateChat()) {
            if (channel.getName().equals(channelName)) {
                channel.withMessage(message);
                if (!builder.isDoNotDisturb() && (builder.getCurrentPrivateChat() == null || channel != builder.getCurrentPrivateChat())) {
                    playSound();
                    updateUnreadCounter(channel, channel.getUnreadMessagesCounter() + 1);
                }
                privateViewController.getPrivateChatList().refresh();
                return false;
            }
        }
        return true;
    }

    private void playSound() {
        if (builder.isPlaySound()) {
            builder.playSound();
        }
    }

    private void updateUnreadCounter(PrivateChat channel, int count) {
        if (builder.isShowNotifications()) {
            channel.setUnreadMessagesCounter(count);
        }
    }

    private void createNewChat(String channelName, Message message) {
        String userId = "";
        for (User user : privateViewController.getOnlineUsersList().getItems()) {
            if (user.getName().equals(channelName)) {
                userId = user.getId();
            }
        }
        PrivateChat channel = new PrivateChat().setId(userId).setName(channelName);
        try {
            // load messages for new channel
            channel.withMessage(ResourceManager.loadPrivatChat(builder.getPersonalUser().getName(), channelName, channel));
            if (!builder.isDoNotDisturb()) {
                playSound();
                updateUnreadCounter(channel, 1);
            }
            builder.getPersonalUser().withPrivateChat(channel);
            Platform.runLater(() -> privateViewController.getPrivateChatList().getItems().add(channel));

        } catch (IOException | JsonException e) {
            e.printStackTrace();
        }
        channel.withMessage(message);
    }


    private void saveMessage(Message message) {
        if (builder.getPersonalUser().getName().equals(message.getFrom())) {
            ResourceManager.savePrivatChat(builder.getPersonalUser().getName(), builder.getCurrentPrivateChat().getName(), message);
        } else {
            ResourceManager.savePrivatChat(builder.getPersonalUser().getName(), message.getFrom(), message);
        }
    }

    private void showChatAlert(JsonObject jsonObject) {
        String errorTitle;
        String serverMessage = jsonObject.getJsonObject("data").getString("message");
        if (serverMessage.equals("This is not your username.")) {
            errorTitle = builder.getStageManager().getLangBundle().getString("error.username");
        } else {
            errorTitle = builder.getStageManager().getLangBundle().getString("error.chat");
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(errorTitle);
            if (serverMessage.equals("This is not your username.")) {
                alert.setHeaderText(builder.getStageManager().getLangBundle().getString("error.this_is_not_your_username"));
            } else {
                alert.setHeaderText(serverMessage);
            }
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                privateViewController.showUsers();
            }
        });
    }
}
