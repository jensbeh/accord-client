package de.uniks.stp.net;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import util.JsonUtil;
import util.SortUser;

import javax.json.JsonObject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PrivateSystemWebSocketClient extends Endpoint {

    private Session session;
    private Timer noopTimer;
    private ModelBuilder builder;
    public static final String COM_NOOP = "noop";
    private PrivateViewController privateViewController;

    public void setPrivateViewController(PrivateViewController privateViewController) {
        this.privateViewController = privateViewController;
    }

    public ModelBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    public PrivateSystemWebSocketClient(URI endpoint, String userKey) {
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
                System.out.println("##### NOOP MESSAGE FROM " + "SYSTEM" + " #####");
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

    public void handleMessage(JsonObject msg) {
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
        Platform.runLater(() -> privateViewController.getOnlineUsersList().setItems(FXCollections.observableList(builder.
                getPersonalUser().getUser()).sorted(new SortUser())));
    }
}
