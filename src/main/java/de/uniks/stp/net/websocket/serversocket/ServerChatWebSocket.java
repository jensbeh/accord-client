package de.uniks.stp.net.websocket.serversocket;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.server.ServerViewController;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.net.websocket.CustomWebSocketConfigurator;
import de.uniks.stp.util.JsonUtil;
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

public class ServerChatWebSocket extends Endpoint {

    public final String COM_NOOP = "noop";
    private Timer noopTimer;
    private Session session;
    private ModelBuilder builder;
    private ServerViewController serverViewController;
    private ChatViewController chatViewController;

    public ServerChatWebSocket(URI endpoint, String userKey) {
        startNoopTimer();
        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new CustomWebSocketConfigurator(userKey))
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientConfig, endpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setServerViewController(ServerViewController serverViewController) {
        this.serverViewController = serverViewController;
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
        this.noopTimer.cancel();
        // set session null
        this.session = null;
    }

    private void onMessage(String message) {
        // Process Message
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        // Use callback to handle it
        this.handleMessage(jsonObject);
    }

    public void handleMessage(JsonStructure msg) {
        JsonObject jsonObject = JsonUtil.parse(msg.toString());

        if (jsonObject.containsKey("channel")) {
            serverMessage(jsonObject);
        }
        if (jsonObject.containsKey("action") && jsonObject.getString("action").equals("info")) {
            showServerChatAlert(jsonObject);
        }
    }

    private void showServerChatAlert(JsonObject jsonObject) {
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
                serverViewController.buildSystemWebSocket();
            }
        });
    }

    private void serverMessage(JsonObject jsonObject) {
        String channelId = jsonObject.getString("channel");
        String from = jsonObject.getString("from");
        String text = jsonObject.getString("text");
        String id = jsonObject.getString("id");
        long timestamp = new Date().getTime();
        // currentUser send
        Message message = new Message().setMessage(text).setFrom(from).setTimestamp(timestamp).setId(id).setServerChannel(serverViewController.getCurrentChannel());
        if (serverViewController.getChatViewController() != null && serverViewController.getCurrentChannel().getId().equals(channelId)) {
            Platform.runLater(() -> serverViewController.getChatViewController().clearMessageField());
        }
        // currentUser received
        else if (!from.equals(builder.getPersonalUser().getName())) {
            for (Categories categories : this.serverViewController.getServer().getCategories()) {
                addMessageToChannel(categories, channelId, message);
            }
        }
        if (serverViewController.getChatViewController() != null && serverViewController.getCurrentChannel().getId().equals(channelId)) {
            assert message != null;
            serverViewController.getCurrentChannel().withMessage(message);
            Platform.runLater(() -> chatViewController.printMessage(message, true));
        }
    }

    private void addMessageToChannel(Categories categories, String channelId, Message message) {
        for (ServerChannel channel : categories.getChannel()) {
            if (channel.getId().equals(channelId)) {
                channel.withMessage(message);
                messageNotifications(channel);
                if (builder.getCurrentServer() == serverViewController.getServer()) {
                    serverViewController.getCategorySubControllerList().get(categories).refreshChannelList();
                }
                break;
            }
        }
    }

    private void messageNotifications(ServerChannel channel) {
        if (!builder.isDoNotDisturb() && (serverViewController.getCurrentChannel() == null || channel != serverViewController.getCurrentChannel())) {
            if (builder.isPlaySound()) {
                builder.playSound();
            }
            if (builder.isShowNotifications()) {
                channel.setUnreadMessagesCounter(channel.getUnreadMessagesCounter() + 1);
            }
        }
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

    public void startNoopTimer() {
        if (this.noopTimer == null) {
            this.noopTimer = new Timer();
        }
    }
}
