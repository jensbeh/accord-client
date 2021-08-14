package de.uniks.stp.net.websocket.privatesocket;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.home.PrivateViewController;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.websocket.CustomWebSocketConfigurator;
import de.uniks.stp.util.JsonUtil;
import de.uniks.stp.util.SortUser;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import javax.json.JsonObject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PrivateSystemWebSocketClient extends Endpoint {

    public final String COM_NOOP = "noop";
    private Timer noopTimer;
    private Session session;
    private ModelBuilder builder;
    private PrivateViewController privateViewController;

    public PrivateSystemWebSocketClient(URI endpoint, String userKey) {
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

    public void handleMessage(JsonObject msg) {
        JsonObject jsonMsg = JsonUtil.parse(msg.toString());
        String userAction = jsonMsg.getString("action");
        JsonObject jsonData = jsonMsg.getJsonObject("data");
        if (userAction.equals("userDescriptionChanged")) {
            updateUserDescription(jsonData);
        } else {
            String userName = jsonData.getString("name");
            String userId = jsonData.getString("id");

            if (userAction.equals("userJoined")) {
                builder.buildUser(userName, userId, "");
                builder.loadUserVolumes();
            }
            if (userAction.equals("userLeft")) {
                if (userName.equals(builder.getPersonalUser().getName())) {
                    builder.clear();
                    Platform.runLater(() -> builder.getStageManager().showLoginScreen());
                }
                List<User> userList = builder.getPersonalUser().getUser();
                User removeUser = builder.buildUser(userName, userId, "");
                if (userList.contains(removeUser)) {
                    builder.getPersonalUser().withoutUser(removeUser);
                }
            }

            Platform.runLater(() -> privateViewController.getOnlineUsersList().setItems(FXCollections.observableList(builder.
                    getPersonalUser().getUser()).sorted(new SortUser())));
            Platform.runLater(() -> privateViewController.getOnlineUsersList().refresh());
        }
    }

    private void updateUserDescription(JsonObject jsonData) {
        for (Server s : builder.getServers()) {
            updateServerUser(jsonData, s);
        }
        updatePrivateChatUser(jsonData);
    }

    private void updatePrivateChatUser(JsonObject jsonData) {
        for (User u : builder.getPersonalUser().getUser()) {
            if (u.getId().equals(jsonData.getString("id"))) {
                u.setDescription(jsonData.getString("description"));
                privateViewController.getOnlineUsersList().refresh();
                break;
            }
        }
    }

    private void updateServerUser(JsonObject jsonData, Server server) {
        for (User u : server.getUser()) {
            if (u.getId().equals(jsonData.getString("id"))) {
                u.setDescription(jsonData.getString("description"));
                builder.getHomeViewController().getServerCtrls().get(server).getOnlineUsersList().refresh();
                if (!builder.getPersonalUser().getId().equals(u.getId())) {
                    Platform.runLater(() -> builder.getSpotifyConnection().updateValuesUser(u.getDescription()));
                }
                break;
            }
        }
    }

    public void startNoopTimer() {
        if (this.noopTimer == null) {
            this.noopTimer = new Timer();
        }
    }
}
