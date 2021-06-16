package de.uniks.stp.net;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import util.JsonUtil;
import util.SortUser;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static util.Constants.*;

public class PrivateChatWebSocket extends Endpoint {

    private String name;
    private Session session;
    private Timer noopTimer;
    private ModelBuilder builder;
    public static final String COM_NOOP = "noop";
    private PrivateViewController privateViewController;

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

    public PrivateChatWebSocket(URI endpoint, String userKey) {
        this.noopTimer = new Timer();
        this.privateViewController = privateViewController;
        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new CustomWebSocketConfigurator(userKey))
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientConfig, endpoint);
        } catch (Exception e) {
            System.err.println("Error during establishing websocket connection:");
        }
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
                System.out.println("##### NOOP MESSAGE FROM " + "PRIVATE CHAT" + " #####");
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
        // cancel timer
        this.noopTimer.cancel();
        // set session null
        this.session = null;
        super.onClose(session, closeReason);
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
        this.session.close();
    }

    public Session getSession() {
        return session;
    }

    public void handleMessage(JsonStructure msg) {
        JsonObject jsonObject = JsonUtil.parse(msg.toString());
        System.out.println("privateChatWebSocketClient");
        System.out.println(msg);
        if (jsonObject.containsKey("channel") && jsonObject.getString("channel").equals("private")) {
            Message message;
            String channelName;
            Boolean newChat = true;
            // currentUser send
            long timestamp = new Date().getTime();
            if (jsonObject.getString("from").equals(builder.getPersonalUser().getName())) {
                channelName = jsonObject.getString("to");
                message = new Message().setMessage(jsonObject.getString("message")).
                        setFrom(jsonObject.getString("from")).
                        setTimestamp(timestamp);
                privateViewController.getMessageViewController().clearMessageField();
            } else { // currentUser received
                channelName = jsonObject.getString("from");
                message = new Message().setMessage(jsonObject.getString("message")).
                        setFrom(jsonObject.getString("from")).
                        setTimestamp(timestamp);
            }
            for (PrivateChat channel : builder.getPersonalUser().getPrivateChat()) {
                if (channel.getName().equals(channelName)) {
                    channel.withMessage(message);
//                    if (!builder.isDoNotDisturb() && (PrivateViewController.getSelectedChat() == null || channel != PrivateViewController.getSelectedChat())) {
//                        if (builder.isPlaySound()) {
//                            builder.playSound();
//                        }
//                        if (builder.isShowNotifications()) {
//                            channel.setUnreadMessagesCounter(channel.getUnreadMessagesCounter() + 1);
//                        }
//                    }
                    privateViewController.getPrivateChatList().refresh();
                    newChat = false;
                    break;
                }
            }
            if (newChat) {
                String userId = "";
                for (User user : privateViewController.getOnlineUsersList().getItems()) {
                    if (user.getName().equals(channelName)) {
                        userId = user.getId();
                    }
                }
                PrivateChat channel = new PrivateChat().setId(userId).setName(channelName).withMessage(message);
//                if (!builder.isDoNotDisturb()) {
//                    if (builder.isPlaySound()) {
//                        builder.playSound();
//                    }
//                    if (builder.isShowNotifications()) {
//                        channel.setUnreadMessagesCounter(1);
//                    }
//                }
                builder.getPersonalUser().withPrivateChat(channel);
                Platform.runLater(() -> privateViewController.getPrivateChatList().getItems().add(channel));
            }
            if (privateViewController.getMessageViewController() != null) {
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
                    privateViewController.showUsers();
                }
            });
        }
    }
}
