package de.uniks.stp.other;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.home.HomeViewController;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SteamGameTest extends ApplicationTest {

    @InjectMocks
    StageManager mockApp = new StageManager();
    private Stage stage;
    private StageManager app;
    @Mock
    private RestClient restClient;
    @Mock
    private HttpResponse<JsonNode> response;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;
    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;

    @Mock
    private HttpResponse<JsonNode> response2;

    @Mock
    private HttpResponse<JsonNode> response3;

    @Mock
    private HttpResponse<JsonNode> response4;

    @Mock
    private HttpResponse<JsonNode> response5;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor2;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor3;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor4;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor5;


    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(HomeViewController.class);
    }

    @Override
    public void start(Stage stage) {
        //start application
        ModelBuilder builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        builder.setLoadUserData(false);
        mockApp.getBuilder().setSpotifyShow(false);
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().setSpotifyRefresh(null);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    public void mockGetUsers() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "60adc8aec77d3f78988b57a0");
        jsonObject.put("name", "Otto");
        jsonObject.put("description", "");
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(jsonObject));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response3.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor3.getValue();
            callback.completed(response3);
            return null;
        }).when(restClient).getUsers(anyString(), callbackCaptor3.capture());
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
            //mockGetServerUser();
            return null;
        }).when(restClient).getServers(anyString(), callbackCaptor2.capture());
    }

    public void loginInit(boolean writeMessage) throws InterruptedException {
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
        mockGetUsers();

        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("name", "Peter")
                .put("password", "1234")
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

        if (writeMessage) {
            String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"Gustav\"}}";
            JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
            privateSystemWebSocketClient.handleMessage(jsonObject);


            message = "{\"channel\":\"private\",\"to\":\"Mr. Poopybutthole\",\"message\":\"Hallo\",\"from\":\"Allyria Dayne\",\"timestamp\":1623805070036}\"";
            jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
            privateChatWebSocket.handleMessage(jsonObject);
        }
    }

    public void loginTestUser(String name, String id) {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
    }

    @Test
    public void AddGameTest() throws InterruptedException {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        loginInit(true);
        mockApp.getBuilder().getPersonalUser().setDescription("?Nice Rocket 2");
        loginTestUser("Gustav", "60c8b3fb44453702009c07b3");
        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userList = lookup("#onlineUsers").query();
        ObservableList<User> itemList = userList.getItems();
        String userName = "";
        for (User u : itemList) {
            if (u.getName().equals("Gustav")) {
                userName = u.getName();
                break;
            }
        }
        Assert.assertEquals("Gustav", userName);
        mockApp.getBuilder().getPersonalUser().setDescription("?Brumbrum");

        JSONObject js = new JSONObject().put("action", "userDescriptionChanged").put("data", new JSONObject().put("id", "60c8b3fb44453702009c07b3").put("description", "?The Binding of Isaac: Rebirth"));
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(js.toString());
        privateSystemWebSocketClient.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();
        String desc = "";
        for (User u : itemList) {
            if (u.getName().equals("Gustav")) {
                desc = u.getDescription();
                break;
            }
        }
        Assert.assertEquals("?The Binding of Isaac: Rebirth", desc);
        js = new JSONObject().put("action", "userDescriptionChanged").put("data", new JSONObject().put("id", "60c8b3fb44453702009c07b3").put("description", "?"));
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(js.toString());
        privateSystemWebSocketClient.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();
        desc = "";
        for (User u : itemList) {
            if (u.getName().equals("Gustav")) {
                desc = u.getDescription();
                break;
            }
        }
        Assert.assertEquals("?", desc);
    }

    @Test
    public void RestTest1() throws InterruptedException {
        JSONObject jsonString = new JSONObject().put("response", new JSONObject().put("players", new JSONArray().put(new JSONObject().put("gameextrainfo", "BigBoi"))));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response4.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor4.getValue();
            callback.completed(response4);
            return null;
        }).when(restClient).getCurrentGame(anyString(), callbackCaptor4.capture());

        loginInit(true);
        mockApp.getBuilder().setSteamShow(true);
        mockApp.getBuilder().getGame();
        WaitForAsyncUtils.waitForFxEvents();
        mockApp.getBuilder().setSteamShow(false);
    }

    @Test
    public void RestTest2() throws InterruptedException {
        JSONObject jsonString = new JSONObject().put("response", new JSONObject().put("players", new JSONArray().put(new JSONObject().put("Komm in die Gruppe", "BigBoi"))));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response5.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor5.getValue();
            callback.completed(response5);
            return null;
        }).when(restClient).getCurrentGame(anyString(), callbackCaptor5.capture());
        loginInit(true);
        mockApp.getBuilder().setSteamShow(true);
        mockApp.getBuilder().getGame();
        WaitForAsyncUtils.waitForFxEvents();
        mockApp.getBuilder().setSteamShow(false);
    }

}