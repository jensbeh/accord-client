package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.WebSocketClient;
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

public class PrivateViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    private static String testUserMainName;
    private static String testUserMainPw;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserTwoName;
    private static String testUserTwoPw;
    private static String testUser1_KEY;
    private static String testUser2_KEY;
    private static String testUser2_ID;
    private static ClientEndpointConfig testUser1_CLIENT_CONFIG = null;
    private static ClientEndpointConfig testUser2_CLIENT_CONFIG = null;
    private static ClientTestEndpoint testUser1_CLIENT = null;
    private static ClientTestEndpoint testUser2_CLIENT = null;
    private CountDownLatch messageLatch;

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

    public void login(String userName, String password) {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(userName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(password);
        clickOn("#loginButton");
    }

    private void setupWebsocketClient() {
        System.out.println("Starting WebSocket Client");
        testUser1_CLIENT_CONFIG = ClientEndpointConfig.Builder.create()
                .configurator(new PrivateViewControllerTest.TestWebSocketConfigurator(testUser1_KEY))
                .build();
        testUser2_CLIENT_CONFIG = ClientEndpointConfig.Builder.create()
                .configurator(new PrivateViewControllerTest.TestWebSocketConfigurator(testUser2_KEY))
                .build();
        messageLatch = new CountDownLatch(1);
        testUser1_CLIENT = new PrivateViewControllerTest.ClientTestEndpoint();
        testUser2_CLIENT = new PrivateViewControllerTest.ClientTestEndpoint();
    }

    private void connectWebSocketClient() throws DeploymentException, IOException {
        try {
            ContainerProvider.getWebSocketContainer().connectToServer(testUser1_CLIENT, testUser1_CLIENT_CONFIG, URI.create("wss://ac.uniks.de/ws/chat?user=Gudrun"));
            ContainerProvider.getWebSocketContainer().connectToServer(testUser2_CLIENT, testUser2_CLIENT_CONFIG, URI.create("wss://ac.uniks.de/ws/chat?user=Jutta"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void shutDownWebSocketClient() throws IOException {
        System.out.println("Closing WebSocket Client\n");
        testUser1_CLIENT.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test was finished"));
        testUser2_CLIENT.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test was finished"));
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
    public void noConnectionOnWebSocketTest() throws InterruptedException {
        login("Peter Lustig", "1234");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        WebSocketClient ws = app.getBuilder().getPrivateChatWebSocketCLient();
        ws.onClose(ws.getSession(), new CloseReason(new CloseReason.CloseCode() {
            /**
             * Returns the code number, for example the integer '1000' for normal closure.
             *
             * @return the code number
             */
            @Override
            public int getCode() {
                return 1006;
            }
        }, "no Connection"));
        Thread.sleep(2000);
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
            }
        }
        Assert.assertEquals("No Connection Error", result);
    }

    @Test
    public void chatPartnerIsOffline() throws InterruptedException, IOException {
        login("Peter Lustig", "1234");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        WebSocketClient ws = app.getBuilder().getPrivateChatWebSocketCLient();
        ws.sendMessage(new JSONObject().put("channel", "private").put("to", "-").put("message", "Test").toString());
        Thread.sleep(2000);
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
            }
        }
        Assert.assertEquals("Chat Error", result);
    }

    @Test
    public void invalidUsername() throws InterruptedException, IOException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("+");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1");
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Thread.sleep(2000);
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
            }
        }
        Assert.assertEquals("Username Error", result);
    }


    @Test
    public void onNewMessageIconCounterTest() throws InterruptedException, DeploymentException, IOException {

        // test User 1 & 2 login
        RestClient restClient = new RestClient();
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            testUser1_KEY = body.getObject().getJSONObject("data").getString("userKey");
        });

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserTwoName = body.getObject().getJSONObject("data").getString("name");
            testUserTwoPw = body.getObject().getJSONObject("data").getString("password");
        });
        restClient.login(testUserTwoName, testUserTwoPw, response -> {
            JsonNode body = response.getBody();
            testUser2_KEY = body.getObject().getJSONObject("data").getString("userKey");
        });

        Thread.sleep(2000);
        setupWebsocketClient();
        connectWebSocketClient();

        // main User login
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserMainName = body.getObject().getJSONObject("data").getString("name");
            testUserMainPw = body.getObject().getJSONObject("data").getString("password");
        });
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserMainName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserMainPw);
        clickOn("#loginButton"); // in homeView
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        // get id from testUser2
        restClient.getUsers(testUser1_KEY, response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            for (int i = 0; i < jsonResponse.length(); i++) {
                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                String userId = jsonResponse.getJSONObject(i).get("id").toString();
                if (userName.equals(testUserTwoName)) {
                    testUser2_ID = userId;
                }
            }
        });

        Thread.sleep(2000);
        testUser2_CLIENT.sendMessage(new JSONObject().put("channel", "private").put("to", testUserMainName).put("message", "Testing notification").toString());
        Thread.sleep(2000);
        Label message = lookup("#msg_" + testUser2_ID).query();
        Assert.assertEquals("This is a test message", message.getText());

        clickOn("#logoutButton");
    }

    @Test
    public void showLastPrivateChatMessage() throws DeploymentException, IOException, InterruptedException {

        Thread.sleep(1000);
        testUser2_CLIENT.sendMessage(new JSONObject().put("channel", "private").put("to", "Tuser0").put("message", "This is a test message").toString());
        Thread.sleep(2000);
        Label message = lookup("#msg_" + testUser2_ID).query();
        Assert.assertEquals("This is a test message", message.getText());

        ListView<Channel> privateChatList = lookup("#privateChatList").query();
        clickOn(privateChatList.lookup("#" + testUser2_ID));
        Thread.sleep(2000);
        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        Thread.sleep(500);
        clickOn("#sendButton");
        Thread.sleep(500);
        message = lookup("#msg_" + testUser2_ID).query();
        Assert.assertEquals("Okay!", message.getText());
/*
        shutDownWebSocketClient();
        restClient.logout(testUser1_KEY, response -> {
        });
        restClient.logout(testUser2_KEY, response -> {
        });
  */
    }
}
