package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PrivateMessageTest extends ApplicationTest {

    private static String GUDRUN_KEY;
    private static String JUTTA_KEY;
    private static String JUTTA_ID;
    private static ClientEndpointConfig GUDRUN_CLIENT_CONFIG = null;
    private static ClientEndpointConfig JUTTA_CLIENT_CONFIG = null;
    private static ClientTestEndpoint GUDRUN_CLIENT = null;
    private static ClientTestEndpoint JUTTA_CLIENT = null;
    private CountDownLatch messageLatch;
    private Stage stage;
    private StageManager app;


    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
    }

    @Override
    public void start(Stage stage) {
        //start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }


    private void setupWebsocketClient() {
        System.out.println("Starting WebSocket Client");
        GUDRUN_CLIENT_CONFIG = ClientEndpointConfig.Builder.create()
                .configurator(new TestWebSocketConfigurator(GUDRUN_KEY))
                .build();
        JUTTA_CLIENT_CONFIG = ClientEndpointConfig.Builder.create()
                .configurator(new TestWebSocketConfigurator(JUTTA_KEY))
                .build();
        messageLatch = new CountDownLatch(1);
        GUDRUN_CLIENT = new ClientTestEndpoint();
        JUTTA_CLIENT = new ClientTestEndpoint();
    }

    private void shutDownWebSocketClient() throws IOException {
        System.out.println("Closing WebSocket Client\n");
        GUDRUN_CLIENT.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test was finished"));
        JUTTA_CLIENT.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test was finished"));
    }

    @Test
    public void testSendAllMessage() throws DeploymentException, IOException, InterruptedException {
        RestClient restClient = new RestClient();
        restClient.login("Gudrun", "1", response -> {
            JsonNode body = response.getBody();
            GUDRUN_KEY = body.getObject().getJSONObject("data").getString("userKey");
        });
        restClient.login("Jutta", "1", response -> {
            JsonNode body = response.getBody();
            JUTTA_KEY = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);
        setupWebsocketClient();
        connectWebSocketClient();

        JUTTA_CLIENT.sendMessage(new JSONObject().put("channel", "private").put("to", "Gudrun").put("message", "this is a test homie").toString());
        boolean messageReceivedByClient = messageLatch.await(20, TimeUnit.SECONDS);
        Assert.assertTrue("Time lapsed before message was received by client.", messageReceivedByClient);
        shutDownWebSocketClient();
        restClient.logout(GUDRUN_KEY, response -> {
        });
        restClient.logout(JUTTA_KEY, response -> {
        });
    }

    private void connectWebSocketClient() throws DeploymentException, IOException {
        try {
            ContainerProvider.getWebSocketContainer().connectToServer(GUDRUN_CLIENT, GUDRUN_CLIENT_CONFIG, URI.create("wss://ac.uniks.de/ws/chat?user=Gudrun"));
            ContainerProvider.getWebSocketContainer().connectToServer(JUTTA_CLIENT, JUTTA_CLIENT_CONFIG, URI.create("wss://ac.uniks.de/ws/chat?user=Jutta"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    class ClientTestEndpoint extends Endpoint {

        private Session session;

        public Session getSession() {
            return session;
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            this.session = session;
            this.session.addMessageHandler(String.class, this::onMessage);
        }

        public void sendMessage(String message) throws IOException {
            if (this.session != null && this.session.isOpen()) {
                this.session.getBasicRemote().sendText(message);
                this.session.getBasicRemote().flushBatch();
            }
        }

        private void onMessage(String message) {
            System.out.println("TEST CLIENT Received message: " + message);
            if (message.contains("this is a test homie")) {
                messageLatch.countDown(); // Count incoming messages
            }
        }
    }

    class TestWebSocketConfigurator extends ClientEndpointConfig.Configurator {
        private final String name;

        public TestWebSocketConfigurator(String name) {
            this.name = name;
        }

        @Override
        public void beforeRequest(Map<String, List<String>> headers) {
            super.beforeRequest(headers);
            ArrayList<String> key = new ArrayList<>();
            key.add(this.name);
            headers.put("userkey", key);
        }
    }

    @Test
    public void showLastPrivateChatMessage() throws DeploymentException, IOException, InterruptedException {
        RestClient restClient = new RestClient();
        restClient.login("Gudrun", "1", response -> {
            JsonNode body = response.getBody();
            GUDRUN_KEY = body.getObject().getJSONObject("data").getString("userKey");
        });
        restClient.login("Jutta", "1", response -> {
            JsonNode body = response.getBody();
            JUTTA_KEY = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);
        setupWebsocketClient();
        connectWebSocketClient();

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Tuser0");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#homeButton");

        restClient.getUsers(GUDRUN_KEY, response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                String userId = jsonResponse.getJSONObject(i).get("id").toString();
                if (userName.equals("Jutta")) {
                    JUTTA_ID = userId;
                }
            }
        });

        Thread.sleep(1000);
        JUTTA_CLIENT.sendMessage(new JSONObject().put("channel", "private").put("to", "Tuser0").put("message", "This is a test message").toString());
        Thread.sleep(2000);
        Label message = lookup("#msg_" + JUTTA_ID).query();
        Assert.assertEquals("This is a test message", message.getText());

        ListView<Channel> privateChatList = lookup("#privateChatList").query();
        clickOn(privateChatList.lookup("#" + JUTTA_ID));
        Thread.sleep(2000);
        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        Thread.sleep(500);
        clickOn("#sendButton");
        Thread.sleep(500);
        message = lookup("#msg_" + JUTTA_ID).query();
        Assert.assertEquals("Okay!", message.getText());

        shutDownWebSocketClient();
        restClient.logout(GUDRUN_KEY, response -> {
        });
        restClient.logout(JUTTA_KEY, response -> {
        });
    }
}