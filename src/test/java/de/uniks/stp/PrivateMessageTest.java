package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
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
    private Stage stage;
    private StageManager app;
    private RestClient restClient;

    private static String testUserMainName;
    private static String testUserMainPw;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserTwoName;
    private static String testUserTwoPw;
    private static String testUserOne_UserKey;
    private static String testUserTwo_UserKey;
    private static String testUserTwo_ID;
    private static ClientEndpointConfig testUser1_CLIENT_CONFIG = null;
    private static ClientEndpointConfig testUser2_CLIENT_CONFIG = null;
    private static ClientTestEndpoint testUser1_CLIENT = null;
    private static ClientTestEndpoint testUser2_CLIENT = null;
    private CountDownLatch messageLatch;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
        this.restClient = new RestClient();
    }

    public void loginInit() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserMainName = body.getObject().getJSONObject("data").getString("name");
            testUserMainPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserMainName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserMainPw);

        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }

    private void setupWebSocketClient() {
        System.out.println("Starting WebSocket Client");
        testUser1_CLIENT_CONFIG = ClientEndpointConfig.Builder.create()
                .configurator(new TestWebSocketConfigurator(testUserOne_UserKey))
                .build();
        testUser2_CLIENT_CONFIG = ClientEndpointConfig.Builder.create()
                .configurator(new TestWebSocketConfigurator(testUserTwo_UserKey))
                .build();
        messageLatch = new CountDownLatch(1);
        testUser1_CLIENT = new ClientTestEndpoint();
        testUser2_CLIENT = new ClientTestEndpoint();
    }

    private void connectWebSocketClient() throws DeploymentException, IOException {
        try {
            ContainerProvider.getWebSocketContainer().connectToServer(testUser1_CLIENT, testUser1_CLIENT_CONFIG, URI.create("wss://ac.uniks.de/ws/chat?user=" + testUserOneName.replace(" ", "+")));
            ContainerProvider.getWebSocketContainer().connectToServer(testUser2_CLIENT, testUser2_CLIENT_CONFIG, URI.create("wss://ac.uniks.de/ws/chat?user=" + testUserTwoName.replace(" ", "+")));
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

    private void shutDownWebSocketClient() throws IOException {
        System.out.println("Closing WebSocket Client\n");
        testUser1_CLIENT.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test was finished"));
        testUser2_CLIENT.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test was finished"));
    }

    @Test
    public void testSendAllMessage() throws DeploymentException, IOException, InterruptedException {
        // user 1
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            testUserOne_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        // user 2
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserTwoName = body.getObject().getJSONObject("data").getString("name");
            testUserTwoPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserTwoName, testUserTwoPw, response -> {
            JsonNode body = response.getBody();
            testUserTwo_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        setupWebSocketClient();
        connectWebSocketClient();

        testUser2_CLIENT.sendMessage(new JSONObject().put("channel", "private").put("to", testUserOneName).put("message", "this is a test homie").toString());
        boolean messageReceivedByClient = messageLatch.await(20, TimeUnit.SECONDS);
        Assert.assertTrue("Time lapsed before message was received by client.", messageReceivedByClient);
        shutDownWebSocketClient();
        restClient.logout(testUserOne_UserKey, response -> {
        });
        restClient.logout(testUserTwo_UserKey, response -> {
        });
    }

    @Test
    public void showLastPrivateChatMessage() throws DeploymentException, IOException, InterruptedException {
        // user 1
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            testUserOne_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        // user 2
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserTwoName = body.getObject().getJSONObject("data").getString("name");
            testUserTwoPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserTwoName, testUserTwoPw, response -> {
            JsonNode body = response.getBody();
            testUserTwo_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        setupWebSocketClient();
        connectWebSocketClient();

        loginInit();

        restClient.getUsers(testUserOne_UserKey, response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                String userId = jsonResponse.getJSONObject(i).get("id").toString();
                if (userName.equals(testUserTwoName)) {
                    testUserTwo_ID = userId;
                }
            }
        });

        Thread.sleep(1000);
        testUser2_CLIENT.sendMessage(new JSONObject().put("channel", "private").put("to", testUserMainName).put("message", "This is a test message").toString());
        Thread.sleep(2000);

        Label message = lookup("#msg_" + testUserTwo_ID).query();
        Assert.assertEquals("This is a test message", message.getText());

        ListView<Channel> privateChatList = lookup("#privateChatList").query();
        clickOn(privateChatList.lookup("#" + testUserTwo_ID));
        Thread.sleep(2000);

        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        Thread.sleep(2000);

        clickOn("#sendButton");
        Thread.sleep(2000);

        message = lookup("#msg_" + testUserTwo_ID).query();
        Assert.assertEquals("Okay!", message.getText());

        shutDownWebSocketClient();
        restClient.logout(testUserOne_UserKey, response -> {
        });
        restClient.logout(testUserTwo_UserKey, response -> {
        });

        
        Thread.sleep(2000);
    }

    @Test
    public void testSendPrivateMessage() throws InterruptedException, IOException, DeploymentException {
        // user 1
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            testUserOne_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        // user 2
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserTwoName = body.getObject().getJSONObject("data").getString("name");
            testUserTwoPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserTwoName, testUserTwoPw, response -> {
            JsonNode body = response.getBody();
            testUserTwo_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        setupWebSocketClient();
        connectWebSocketClient();

        restClient.getUsers(testUserTwo_UserKey, response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                String userId = jsonResponse.getJSONObject(i).get("id").toString();
                if (userName.equals(testUserTwoName)) {
                    testUserTwo_ID = userId;
                }
            }
        });

        loginInit();

        Assert.assertEquals("Accord - Main", stage.getTitle());

        ListView<User> privateChatList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        doubleClickOn(privateChatList.lookup("#" + testUserTwo_ID));
        Thread.sleep(2000);

        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        Thread.sleep(500);

        clickOn("#sendButton");
        Thread.sleep(500);

        ListView<Message> privateChatMessageList = lookup("#messageListView").query();
        Label messageLabel = (Label) privateChatMessageList.lookup("#messageLabel");
        Label userNameLabel = (Label) privateChatMessageList.lookup("#userNameLabel");
        Assert.assertEquals(" Okay! ", messageLabel.getText());
        Assert.assertEquals(testUserMainName, userNameLabel.getText());

        shutDownWebSocketClient();
        restClient.logout(testUserOne_UserKey, response -> {
        });
        restClient.logout(testUserTwo_UserKey, response -> {
        });

        
        Thread.sleep(2000);
    }
}