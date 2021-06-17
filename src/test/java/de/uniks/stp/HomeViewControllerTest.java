package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.HomeViewController;
import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.*;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
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

@RunWith(MockitoJUnitRunner.Silent.class)           // TODO important
public class HomeViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    // main user
    private final String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";

    @Mock
    private RestClient restClient;

    @Mock
    private HttpResponse<JsonNode> response;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @InjectMocks
    StageManager mockApp = new StageManager();

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
    private HttpResponse<JsonNode> response5;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor2;


    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketCLient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        app.setRestClient(restClient);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(HomeViewController.class);
    }

    public void mockLogout() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "Logged out")
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            return null;
        }).when(restClient).logout(anyString(), callbackCaptor.capture());
    }

    public void mockGetUsers(String id, String name) {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", id).put("name", name)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            return null;
        }).when(restClient).getUsers(anyString(), callbackCaptor.capture());
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

    public void loginInit() throws InterruptedException {
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

        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"Gustav\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);


        message = "{\"channel\":\"private\",\"to\":\"Mr. Poopybutthole\",\"message\":\"Hallo\",\"from\":\"Allyria Dayne\",\"timestamp\":1623805070036}\"";
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateChatWebSocket.handleMessage(jsonObject);
    }

    public void loginTestUser(String name, String id) {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
    }

    public void logoutTestUser(String name, String id) {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        String message = "{\"action\":\"userLeft\",\"data\":{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
    }

    @Test
    public void personalUserTest() throws InterruptedException {
        loginInit();

        Label personalUserName = lookup("#currentUserBox").lookup("#userName").query();

        Assert.assertEquals("Peter", personalUserName.getText());
    }

    @Test
    public void serverBoxTest() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverNameInput = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverNameInput.setText("TestServer2");
        JSONObject j = new JSONObject();
        j.put("status", "success");
        j.put("data", new JSONArray().put(new JSONObject().put("id", "5e2ffbd8770dd077d03df505").put("name", "TestServer2")));
        when(response5.getBody()).thenReturn(new JsonNode(j.toString()));
        doAnswer(invocation -> {
                    Callback<JsonNode> callback = callbackCaptor2.getValue();
                    callback.completed(response5);
                    //mockGetServerUser();
                    return null;
                }
        ).when(restClient).postServer(anyString(), anyString(), callbackCaptor.capture());


        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();

        ObservableList<Server> itemList = serverListView.getItems();
        String serverName = "";
        for (Server server : itemList) {
            if (server.getName().equals("TestServer2")) {
                serverName = "TestServer2";
                break;
            }
        }
        Assert.assertEquals("TestServer2", serverName);
    }

    @Test
    public void userBoxTest() throws InterruptedException {
        loginInit();
        loginTestUser("Gustav", "60c8b3fb44453702009c07b3");
        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        ObservableList<User> itemList = userList.getItems();
        String userName = "";
        for (User user : itemList) {
            if (user.getName().equals("Gustav")) {
                userName = user.getName();
                break;
            }
        }
        Assert.assertEquals("Gustav", userName);


        logoutTestUser("Gustav", "60c8b3fb44453702009c07b3");
        WaitForAsyncUtils.waitForFxEvents();
        itemList = userList.getItems();
        userName = "";
        for (User user : itemList) {
            if (user.getName().equals("Gustav")) {
                userName = user.getName();
                break;
            }
        }
        Assert.assertEquals("", userName);
    }

    @Test
    public void getServersTest() {
        mockGetServers();
        restClient.getServers(userKey, response -> {
        });
        Assert.assertNotEquals(0, response2.getBody().getObject().getJSONArray("data").length());
    }

    @Test
    public void getUsersTest() {
        mockGetUsers("Test User", "5e2ffg75dd077d03df505");
        restClient.getUsers(userKey, response -> {
        });
        Assert.assertNotEquals(0, response.getBody().getObject().getJSONArray("data").length());
    }

    @Test
    public void privateChatTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        User testUserOne = userList.getItems().get(0);
        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        WaitForAsyncUtils.waitForFxEvents();
        ListView<PrivateChat> privateChatList = lookup("#privateChatList").query();
        boolean res = false;
        for (PrivateChat chat : privateChatList.getItems()) {
            if (chat.getName().equals("Gustav")) {
                res = true;
                break;
            }
        }
        Assert.assertTrue(res);
    }

    @Test()
    public void logout() throws InterruptedException {
        loginInit();

        Assert.assertEquals("Accord - Main", stage.getTitle());
        // Clicking logout...
        mockLogout();
        clickOn("#logoutButton");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Accord - Login", stage.getTitle());
    }

    @Test
    public void logoutMultiLogin() throws InterruptedException {
        loginInit();
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        String message = "{\"action\":\"userLeft\",\"data\":{\"id\":\"" + "00" + "\",\"name\":\"" + "Peter" + "\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Accord - Login", stage.getTitle());
        mockLogout();
        restClient.logout(userKey, response -> {
        });
    }

    @Test
    public void openExistingChat() throws InterruptedException {
        loginInit();

        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"5e2ffg75dd077d03df505\",\"name\":\"Test User\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
        message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"5940kf93ued390ir3ud84\",\"name\":\"Test User 2\"}}";
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();

        // Use the first two Users in Online-User-List as test Users
        User testUserOne = userList.getItems().get(0);
        User testUserTwo = userList.getItems().get(1);

        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        WaitForAsyncUtils.waitForFxEvents();

        doubleClickOn(userList.lookup("#" + testUserTwo.getId()));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(testUserTwo.getName(), PrivateViewController.getSelectedChat().getName());

        ListView<PrivateChat> privateChatList = lookup("#privateChatList").query();
        clickOn(privateChatList.lookup("#" + testUserOne.getId()));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(testUserOne.getName(), PrivateViewController.getSelectedChat().getName());

        //Additional test if opened private chat is colored
        VBox privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        Assert.assertEquals("-fx-background-color: #666666; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183", privateChatCell.getStyle());

        //Additional test when homeButton is clicked and opened chat is the same
        //Clicking homeButton will load the view - same like clicking on server and back to home
        clickOn("#homeButton");
        WaitForAsyncUtils.waitForFxEvents();
        privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        Assert.assertEquals("-fx-background-color: #666666; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183", privateChatCell.getStyle());
    }
}