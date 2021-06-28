package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.*;
import de.uniks.stp.net.udp.AudioStreamClient;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.glassfish.json.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.JsonObject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private static final String testUserOneName = "Peter";
    private static final String testUserOnePw = "1234";


    @Mock
    private RestClient restClient;

    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;

    @Mock
    private HttpResponse<JsonNode> response;

    @Mock
    private HttpResponse<JsonNode> response2;

    @Mock
    private HttpResponse<JsonNode> response3;

    @Mock
    private HttpResponse<JsonNode> response4;

    @Mock
    private HttpResponse<JsonNode> response5;

    @Mock
    private DatagramSocket mockAudioSocket;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor2;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor3;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor4;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor5;

    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        builder = new ModelBuilder();
        this.stage = stage;
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);
        AudioStreamClient.setSocket(mockAudioSocket);
        app.start(stage);
        stage.centerOnScreen();
    }

    @InjectMocks
    StageManager mockApp = new StageManager();

    @BeforeAll
    static void setup() throws IOException {
        MockitoAnnotations.openMocks(ServerViewControllerTest.class);
    }

    public void mockGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "TestServer Team Bit Shift")));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response2.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor2.getValue();
            callback.completed(response2);
            mockGetServerUser();
            return null;
        }).when(restClient).getServers(anyString(), callbackCaptor2.capture());
    }

    public void mockGetServerUser() {
        JSONArray members = new JSONArray();
        JSONArray categories = new JSONArray();
        categories.put("60b77ba0026b3534ca5a61ae");
        JSONObject member = new JSONObject();
        member.put("id", "60ad230ac77d3f78988b3e5b")
                .put("name", "Peter Lustig")
                .put("online", true);
        members.put(member);
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject()
                        .put("id", "5e2fbd8770dd077d03df505")
                        .put("name", "JOIdk")
                        .put("owner", "60ad230ac77d3f78988b3e5b")
                        .put("categories", categories)
                        .put("members", members)
                );
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response3.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor3.getValue();
            callback.completed(response3);
            mockGetCategories();
            return null;
        }).when(restClient).getServerUsers(anyString(), anyString(), callbackCaptor3.capture());
    }

    public void mockGetCategories() {
        JSONArray channels = new JSONArray();
        channels.put("60b77ba0026b3534ca5a61af");
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "60b77ba0026b3534ca5a61ae")
                .put("name", "default")
                .put("server", "5e2fbd8770dd077d03df505")
                .put("channels", channels));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", data);
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response4.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor4.getValue();
            callback.completed(response4);
            return null;
        }).when(restClient).getServerCategories(anyString(), anyString(), callbackCaptor4.capture());
    }

    public void mockGetChannels() {
        JSONArray members = new JSONArray();
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "60b77ba0026b3534ca5a61af")
                .put("name", "testChannel")
                .put("type", "text")
                .put("privileged", false)
                .put("category", "60b77ba0026b3534ca5a61ae")
                .put("members", members))
                .put(new JSONObject()
                        .put("id", "60b77ba0026b3534ca5a61dd")
                        .put("name", "audioChannel")
                        .put("type", "audio")
                        .put("privileged", false)
                        .put("category", "60b77ba0026b3534ca5a61ae")
                        .put("members", members))
                .put(new JSONObject()
                        .put("id", "60b77ba0026423ad521awd2")
                        .put("name", "audioChannel")
                        .put("type", "audio")
                        .put("privileged", false)
                        .put("category", "60b77ba0026b3534ca5a61ae")
                        .put("members", members));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", data);
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response5.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor5.getValue();
            callback.completed(response5);
            return null;
        }).when(restClient).getCategoryChannels(anyString(), anyString(), anyString(), callbackCaptor5.capture());
    }


    public void loginInit(String name2, String pw) throws InterruptedException {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        doCallRealMethod().when(privateChatWebSocket).handleMessage(any());
        doCallRealMethod().when(privateChatWebSocket).setBuilder(any());
        doCallRealMethod().when(privateChatWebSocket).setPrivateViewController(any());
        doCallRealMethod().when(serverChatWebSocket).handleMessage(any());
        doCallRealMethod().when(serverChatWebSocket).setBuilder(any());
        doCallRealMethod().when(serverChatWebSocket).setServerViewController(any());
        mockGetServers();
        mockGetServerUser();
        mockGetCategories();
        mockGetChannels();

        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("name", name2)
                .put("password", pw)
                .put("data", new JSONObject().put("userKey", "c3a981d1-d0a2-47fd-ad60-46c7754d9271"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            String name = (String) invocation.getArguments()[0];
            String password = (String) invocation.getArguments()[1];
            System.out.println(name);
            System.out.println(password);
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            return null;
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        clickOn("#loginButton");

        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"Gustav\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);


        message = "{\"channel\":\"private\",\"to\":\"Mr. Poopybutthole\",\"message\":\"Hallo\",\"from\":\"Allyria Dayne\",\"timestamp\":1623805070036}\"";
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateChatWebSocket.handleMessage(jsonObject);
    }

    @Test
    public void showServerTest() throws InterruptedException {
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        loginInit(testUserOneName, testUserOnePw);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        String message = new JSONObject().put("action", "userArrived").put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "Natasha Yar").put("online", true)).toString();
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        message = new JSONObject().put("action", "userExited").put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "Natasha Yar")).toString();
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);

        MenuButton serverNameText = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer Team Bit Shift", serverNameText.getText());

        message = new JSONObject().put("action", "serverDeleted").put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "TestServer Team Bit Shift")).toString();
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void showServerUsersTest() throws InterruptedException {
        loginInit(testUserOneName, testUserOnePw);

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        String message = new JSONObject().put("action", "userJoined").put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "Natasha Yar").put("online", true)).toString();
        JsonObject jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);

        app.getBuilder().buildServerUser(app.getBuilder().getCurrentServer(), "Test", "1234", false);
        app.getBuilder().buildServerUser(app.getBuilder().getCurrentServer(), "Test1", "12234", true);

        ScrollPane scrollPaneUserBox = lookup("#scrollPaneUserBox").query();
        ListView<User> onlineUserList = (ListView<User>) scrollPaneUserBox.lookup("#onlineUsers");
        ListView<User> offlineUserList = (ListView<User>) scrollPaneUserBox.lookup("#offlineUsers");
        app.getHomeViewController().getServerController().showOnlineOfflineUsers();
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(2, onlineUserList.getItems().size());
        Assert.assertEquals(1, offlineUserList.getItems().size());


        message = new JSONObject().put("action", "userExited").put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testUserOneName)).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
    }

    @Test
    public void logoutMultiLogin() throws InterruptedException {
        loginInit(testUserOneName, testUserOnePw);

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        String message = "{\"action\":\"userLeft\",\"data\":{\"id\":\"" + "00" + "\",\"name\":\"" + "Peter" + "\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Accord - Login", stage.getTitle());
    }


    @Test
    public void categoryViewTest() throws InterruptedException {
        loginInit(testUserOneName, testUserOnePw);

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Channel> channels = lookup("#channelList").queryListView();
        app.getBuilder().getCurrentServer().getCategories().get(0).withChannel(new ServerChannel().setName("PARTEY").setType("text"));
        Assert.assertEquals(app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().size(), channels.getItems().size());


        String message = new JSONObject().put("action", "userLeft").put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testUserOneName)).toString();
        JsonObject jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
    }

    @Test
    public void onNewMessageIconCounterTest() throws InterruptedException {
        doCallRealMethod().when(serverChatWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverChatWebSocket).handleMessage(any());
        doCallRealMethod().when(serverChatWebSocket).setBuilder(any());
        serverChatWebSocket.setBuilder(builder);
        loginInit(testUserOneName, testUserOnePw);
        builder.setPlaySound(false);
        builder.setShowNotifications(true);
        builder.setDoNotDisturb(false);

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        ServerChannel channel = app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().get(0);

        JSONObject message = new JSONObject().put("channel", channel.getId()).put("timestamp", 9257980).put("text", "Ey").put("from", "Copei").put("id", "5e2fbd8770dd077d03df505");
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverChatWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        Label counter = lookup("#notificationCounter_" + channel.getId()).query();
        Circle background = lookup("#notificationCounterBackground_" + channel.getId()).query();
        Circle foreground = lookup("#notificationCounterForeground_" + channel.getId()).query();

        Assert.assertEquals("1", counter.getText());
        Assert.assertTrue(background.isVisible());
        Assert.assertTrue(foreground.isVisible());

        message = new JSONObject().put("channel", channel.getId()).put("timestamp", 9257980).put("text", "Du Lappen!").put("from", "Copei").put("id", "5e2fbd8770dd077d03df505");
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverChatWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        Label counterSecondTime = lookup("#notificationCounter_" + channel.getId()).query();
        Circle backgroundSecondTime = lookup("#notificationCounterBackground_" + channel.getId()).query();
        Circle foregroundSecondTime = lookup("#notificationCounterForeground_" + channel.getId()).query();
        Assert.assertEquals("2", counterSecondTime.getText());
        Assert.assertTrue(backgroundSecondTime.isVisible());
        Assert.assertTrue(foregroundSecondTime.isVisible());

        ListView<User> channelList = lookup("#scrollPaneCategories").lookup("#categoryVbox").lookup("#channelList").query();
        doubleClickOn(channelList.lookup("#" + channel.getId()));

        Assert.assertFalse(lookup("#notificationCounter_" + "c3a981d1-d0a2-47fd-ad60-46c7754d9271").queryAll().contains(counterSecondTime));
        Assert.assertFalse(lookup("#notificationCounterBackground_" + "c3a981d1-d0a2-47fd-ad60-46c7754d9271").queryAll().contains(backgroundSecondTime));
        Assert.assertFalse(lookup("#notificationCounterForeground_" + "c3a981d1-d0a2-47fd-ad60-46c7754d9271").queryAll().contains(foregroundSecondTime));
    }


    @Test
    public void audioStreamTest() throws InterruptedException {
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        serverSystemWebSocket.setBuilder(builder);

        loginInit(testUserOneName, testUserOnePw);
        builder.getPersonalUser().setId("60ace8f1c77d3f78988b275a");

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        try {
            doAnswer((Answer<Void>) invocation -> {
                DatagramPacket mockPacket = invocation.getArgument(0);

                byte[] data = new byte[1024];
                JSONObject obj1 = new JSONObject().put("channel", "60b77ba0026b3534ca5a61dd")
                        .put("name", builder.getPersonalUser().getName());


                // set 255 with jsonObject - sendData is automatically init with zeros
                byte[] jsonData = new byte[255];
                byte[] objData = new byte[0];
                objData = obj1.toString().getBytes(StandardCharsets.UTF_8);

                // set every byte new which is from jsonObject and let the rest be still 0
                for (int i = 0; i < objData.length; i++) {
                    Arrays.fill(jsonData, i, i + 1, objData[i]);
                }

                // put both byteArrays in one
                byte[] sendData = new byte[1279];
                System.arraycopy(jsonData, 0, sendData, 0, jsonData.length);
                System.arraycopy(data, 0, sendData, jsonData.length, data.length);

                mockPacket.setData(sendData);
                return null;
            }).when(mockAudioSocket).receive(any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        doubleClickOn("#60b77ba0026b3534ca5a61dd");
        WaitForAsyncUtils.waitForFxEvents();

        String message = new JSONObject().put("action", "audioJoined").put("data", new JSONObject().put("id", "60ace8f1c77d3f78988b275a").put("category", "60b77ba0026b3534ca5a61ae").put("channel", "60b77ba0026b3534ca5a61dd")).toString();
        JsonObject jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        ServerChannel audioChannel = app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().get(1);
        Assert.assertEquals(audioChannel.getAudioMember().size(), 1);

        User newUser = new User().setName("Hans").setId("60ace8f1c77d3f78988bawdw");
        builder.getPersonalUser().withUser(newUser);
        builder.getCurrentServer().withUser(newUser);

        message = new JSONObject().put("action", "audioJoined").put("data", new JSONObject().put("id", "60ace8f1c77d3f78988bawdw").put("category", "60b77ba0026b3534ca5a61ae").put("channel", "60b77ba0026b3534ca5a61dd")).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(audioChannel.getAudioMember().size(), 2);

        message = new JSONObject().put("action", "audioLeft").put("data", new JSONObject().put("id", "60ace8f1c77d3f78988bawdw").put("category", "60b77ba0026b3534ca5a61ae").put("channel", "60b77ba0026b3534ca5a61dd")).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(audioChannel.getAudioMember().size(), 1);

        doubleClickOn("#60b77ba0026423ad521awd2");
        WaitForAsyncUtils.waitForFxEvents();

        message = new JSONObject().put("action", "audioLeft").put("data", new JSONObject().put("id", "60ace8f1c77d3f78988b275a").put("category", "60b77ba0026b3534ca5a61ae").put("channel", "60b77ba0026b3534ca5a61dd")).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        ServerChannel audioChannel2 = app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().get(2);

        message = new JSONObject().put("action", "audioJoined").put("data", new JSONObject().put("id", "60ace8f1c77d3f78988b275a").put("category", "60b77ba0026b3534ca5a61ae").put("channel", "60b77ba0026423ad521awd2")).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(audioChannel2.getAudioMember().size(), 1);

        clickOn("#homeButton");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#button_disconnectAudio");
        WaitForAsyncUtils.waitForFxEvents();

        message = new JSONObject().put("action", "audioLeft").put("data", new JSONObject().put("id", "60ace8f1c77d3f78988b275a").put("category", "60b77ba0026b3534ca5a61ae").put("channel", "60b77ba0026423ad521awd2")).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(audioChannel2.getAudioMember().size(), 0);
    }
}
