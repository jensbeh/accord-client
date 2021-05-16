package de.uniks.stp.net;

import de.uniks.stp.builder.ModelBuilder;
import org.glassfish.json.JsonUtil;

import javax.json.JsonObject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;



public class WebSocketClient extends Endpoint{
    private Session session;
    private Timer noopTimer;
    private final ModelBuilder builder;
    public static final String COM_NOOP = "noop";

    private WSCallback callback;

    public WebSocketClient(ModelBuilder builder, URI endpoint, WSCallback callback) {
        this.builder = builder;
        this.noopTimer = new Timer();

        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new CustomWebSocketConfigurator(builder.getPersonalUser().getUserKey()))
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientConfig, endpoint);
            this.callback = callback;
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
                System.out.println("##### NOOP MESSAGE #####");
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
        this.callback.onClose(session, closeReason);
    }

    private void onMessage(String message) {
        // Process Message
        JsonObject jsonObject = (JsonObject) JsonUtil.toJson(message);
        // Use callback to handle it
        this.callback.handleMessage(jsonObject);
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
}
