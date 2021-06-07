package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ServerViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private static String testUserMainName;
    private static String testUserMainPw;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserOne_UserKey;
    private static String testUserOne_ID;
    private static String testServerId;
    private static ClientEndpointConfig testUser1_CLIENT_CONFIG = null;
    private static ServerViewControllerTest.ClientTestEndpoint testUser1_CLIENT = null;
    private CountDownLatch messageLatch;
    private RestClient restClient;


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

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    public void loginInitWithTemp() throws InterruptedException {
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

    public void loginInit(String name, String password) throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(name);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(password);

        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }


    public void loginInitWithTempUser() throws InterruptedException {
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
                .configurator(new ServerViewControllerTest.TestWebSocketConfigurator(testUserOne_UserKey))
                .build();
        messageLatch = new CountDownLatch(1);
        testUser1_CLIENT = new ServerViewControllerTest.ClientTestEndpoint();
    }

    private void connectWebSocketClient() throws DeploymentException, IOException {
        try {
            ContainerProvider.getWebSocketContainer().connectToServer(testUser1_CLIENT, testUser1_CLIENT_CONFIG, URI.create("wss://ac.uniks.de/ws/chat?user=" + testUserOneName.replace(" ", "+") + "&serverId=" + app.getBuilder().getCurrentServer().getId()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void shutDownWebSocketClient() throws IOException {
        System.out.println("Closing WebSocket Client\n");
        testUser1_CLIENT.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test was finished"));
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

    public void getServerId() throws InterruptedException {
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

        JsonNode body = restClient.postServer(testUserOne_UserKey, "TestServer Team Bit Shift");
        testServerId = body.getObject().getJSONObject("data").getString("id");

        restClient.logout(testUserOne_UserKey, response -> {
        });
    }

    //@Test
    public void showServerTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        MenuButton serverNameText = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer Team Bit Shift", serverNameText.getText());


        Thread.sleep(2000);
    }

    //@Test
    public void showServerUsersTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        app.getBuilder().buildServerUser(app.getBuilder().getCurrentServer(), "Test", "1234", false);
        app.getBuilder().buildServerUser(app.getBuilder().getCurrentServer(), "Test1", "12234", true);

        ScrollPane scrollPaneUserBox = lookup("#scrollPaneUserBox").query();
        ListView<User> onlineUserList = (ListView<User>) scrollPaneUserBox.lookup("#onlineUsers");
        ListView<User> offlineUserList = (ListView<User>) scrollPaneUserBox.lookup("#offlineUsers");
        app.getHomeViewController().getServerController().showOnlineOfflineUsers();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Assert.assertNotEquals(0, onlineUserList.getItems().size());
        Assert.assertNotEquals(0, offlineUserList.getItems().size());


        Thread.sleep(2000);
    }

    //@Test
    public void logoutMultiLogin() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        restClient.login(testUserOneName, testUserOnePw, response -> {
        });

        Thread.sleep(2000);
        Assert.assertEquals("Accord - Login", stage.getTitle());

        restClient.logout(testUserOne_UserKey, response -> {
        });
    }


    //@Test
    public void categoryViewTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        ListView channels = lookup("#channellist").queryListView();
        app.getBuilder().getCurrentServer().getCategories().get(0).withChannel(new Channel().setName("PARTEY"));
        Assert.assertEquals(app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().size(), channels.getItems().size());


        Thread.sleep(2000);
    }

    //@Test
    public void onNewMessageIconCounterTest() throws InterruptedException, DeploymentException, IOException {
        // test User 1 login
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

        loginInitWithTempUser();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();

        Button createLink = lookup("#createLink").query();
        TextField linkTextField = lookup("#linkTextField").query();
        clickOn(createLink);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        String serverLink = linkTextField.getText();
        String[] splitLink = serverLink.split("/");
        String serverId = splitLink[splitLink.length - 3];
        String inviteId = splitLink[splitLink.length - 1];
        restClient.joinServer(serverId, inviteId, testUserOneName, testUserOnePw, testUserOne_UserKey, response -> {
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        setupWebSocketClient();
        connectWebSocketClient();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        Channel channel = app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().get(0);

        testUser1_CLIENT.sendMessage(new JSONObject().put("channel", channel.getId()).put("message", "Testing notification").toString());
        Thread.sleep(2000);

        Label counter = lookup("#notificationCounter_" + channel.getId()).query();
        Circle background = lookup("#notificationCounterBackground_" + channel.getId()).query();
        Circle foreground = lookup("#notificationCounterForeground_" + channel.getId()).query();

        Assert.assertEquals("1", counter.getText());
        Assert.assertTrue(background.isVisible());
        Assert.assertTrue(foreground.isVisible());

        testUser1_CLIENT.sendMessage(new JSONObject().put("channel", channel.getId()).put("message", "Testing more notification").toString());
        Thread.sleep(2000);

        Label counterSecondTime = lookup("#notificationCounter_" + channel.getId()).query();
        Circle backgroundSecondTime = lookup("#notificationCounterBackground_" + channel.getId()).query();
        Circle foregroundSecondTime = lookup("#notificationCounterForeground_" + channel.getId()).query();
        Assert.assertEquals("2", counterSecondTime.getText());
        Assert.assertTrue(backgroundSecondTime.isVisible());
        Assert.assertTrue(foregroundSecondTime.isVisible());
        Thread.sleep(2000);

        ListView<User> channelList = lookup("#scrollPaneCategories").lookup("#categoryVbox").lookup("#channellist").query();
        doubleClickOn(channelList.lookup("#" + channel.getId()));

        Assert.assertFalse(lookup("#notificationCounter_" + testUserOne_UserKey).queryAll().contains(counterSecondTime));
        Assert.assertFalse(lookup("#notificationCounterBackground_" + testUserOne_UserKey).queryAll().contains(backgroundSecondTime));
        Assert.assertFalse(lookup("#notificationCounterForeground_" + testUserOne_UserKey).queryAll().contains(foregroundSecondTime));

        shutDownWebSocketClient();
        restClient.logout(testUserOne_UserKey, response -> {
        });
    }
}
