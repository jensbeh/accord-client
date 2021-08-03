package de.uniks.stp.net.websocket.privatesocket;

import com.github.cliftonlabs.json_simple.JsonException;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.home.PrivateViewController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.model.User;
import de.uniks.stp.net.websocket.CustomWebSocketConfigurator;
import de.uniks.stp.util.JsonUtil;
import de.uniks.stp.util.ResourceManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

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
            showNoConAlert();
        }
        super.onClose(session, closeReason);
    }

    public void showNoConAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
            alert.setTitle(StageManager.getLangBundle().getString("error.no_connection"));
            alert.setHeaderText(StageManager.getLangBundle().getString("error.no_connection_text"));
            alert.setOnCloseRequest(e -> StageManager.showLoginScreen());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                StageManager.showLoginScreen();
            }
        });
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
        PrivateChat channel = new PrivateChat().setId(userId).setName(channelName).withMessage(message);
        try {
            // load messages for new channel
            channel.withMessage(ResourceManager.loadPrivatChat(builder.getPersonalUser().getName(), channelName, channel));
            channel.withMessage(message);
            if (!builder.isDoNotDisturb()) {
                playSound();
                updateUnreadCounter(channel, 1);
            }
            builder.getPersonalUser().withPrivateChat(channel);
            Platform.runLater(() -> privateViewController.getPrivateChatList().getItems().add(channel));

        } catch (IOException | JsonException e) {
            e.printStackTrace();
        }
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
                privateViewController.showUsers();
            }
        });
    }
}
