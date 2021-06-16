package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HomeViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

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

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        ModelBuilder builder = new ModelBuilder();
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

    @InjectMocks
    StageManager mockApp = new StageManager();

    @BeforeAll
    static void setup() throws IOException {
        MockitoAnnotations.openMocks(MockingTest.class);
    }


    public void mockGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "TestServer Team Bit Shift")));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response2.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor2.getValue();
                callback.completed(response2);
                mockGetServerUser();
                return null;
            }
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
                        .put("name", "asdfasdf")
                        .put("owner", "60ad230ac77d3f78988b3e5b")
                        .put("categories", categories)
                        .put("members", members)
                );
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response3.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor3.getValue();
                callback.completed(response3);
                mockGetCategories();
                return null;
            }
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
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor4.getValue();
                callback.completed(response4);
                return null;
            }
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
                .put("members", members));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", data);
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response5.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor5.getValue();
                callback.completed(response5);
                return null;
            }
        }).when(restClient).getCategoryChannels(anyString(), anyString(), anyString(), callbackCaptor5.capture());
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
        mockGetServerUser();
        mockGetCategories();
        mockGetChannels();

        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("name", "Peter")
                .put("password", "1234")
                .put("data", new JSONObject().put("userKey", "c3a981d1-d0a2-47fd-ad60-46c7754d9271"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                String name = (String) invocation.getArguments()[0];
                String password = (String) invocation.getArguments()[1];
                System.out.println(name);
                System.out.println(password);
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
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
        Thread.sleep(1000);

    }

    public void loginTestUser(String name) {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        Random random = new Random();
        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"" + name + "\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
    }

    public void logoutTestUser(String name) {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        String message = "{\"action\":\"userLeft\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"" + name + "\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
    }

    private String userKey;
    private String testUserName;
    private String testUserPw;
    private String testUserOneName;
    private String testUserOnePw;
    private String testUserKeyOne;
    private String testUserTwoName;
    private String testUserTwoPw;
    private String testUserKeyTwo;

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    //@Test
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
        serverNameInput.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();

        ObservableList<Server> itemList = serverListView.getItems();
        String serverName = "";
        for (Server server : itemList) {
            if (server.getName().equals("TestServer Team Bit Shift")) {
                serverName = "TestServer Team Bit Shift";
                break;
            }
        }
        Assert.assertEquals("TestServer Team Bit Shift", serverName);


    }

    //@Test
    public void userBoxTest() throws InterruptedException {
        loginInit();

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        restClient.login(testUserOneName, testUserOnePw, response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        ObservableList<User> itemList = userList.getItems();
        String userName = "";
        for (User user : itemList) {
            if (user.getName().equals(testUserOneName)) {
                userName = user.getName();
                break;
            }
        }
        Assert.assertEquals(testUserOneName, userName);

        restClient.logout(userKey, response -> {
        });
        Thread.sleep(2000);

        itemList = userList.getItems();
        userName = "";
        for (User user : itemList) {
            if (user.getName().equals(testUserOneName)) {
                userName = user.getName();
                break;
            }
        }
        Assert.assertEquals("", userName);


    }

    //@Test
    public void getServersTest() {
        restMock.getServers("bla", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).getServers(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    //@Test
    public void getUsersTest() {
        restMock.getUsers("bla", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).getUsers(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    //@Test
    public void privateChatTest() throws InterruptedException {
        loginInit();

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        restClient.login(testUserOneName, testUserOnePw, response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        User testUserOne = userList.getItems().get(0);
        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        Thread.sleep(500);
        ListView<PrivateChat> privateChatList = lookup("#privateChatList").query();
        Assert.assertEquals(testUserOne.getName(), privateChatList.getItems().get(0).getName());

        restClient.logout(userKey, response -> {
        });


    }

    //@Test()
    public void logout() throws InterruptedException {
        loginInit();

        Assert.assertEquals("Accord - Main", stage.getTitle());


        // Clicking logout...
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        // TODO: enable Assert when logout click is back
        //Assert.assertEquals("Accord - Login", stage.getTitle());

        restMock.logout("c653b568-d987-4331-8d62-26ae617847bf", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).logout(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    //@Test
    public void logoutMultiLogin() throws InterruptedException {
        loginInit();

        restClient.login(testUserName, testUserPw, response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Assert.assertEquals("Accord - Login", stage.getTitle());
        restClient.logout(userKey, response -> {
        });
    }

    //@Test
    public void openExistingChat() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            this.testUserKeyOne = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserTwoName = body.getObject().getJSONObject("data").getString("name");
            testUserTwoPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

        restClient.login(testUserTwoName, testUserTwoPw, response -> {
            JsonNode body = response.getBody();
            this.testUserKeyTwo = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        loginInit();

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();

        Thread.sleep(500);

        // Use the first two Users in Online-User-List as test Users
        User testUserOne = userList.getItems().get(0);
        User testUserTwo = userList.getItems().get(1);

        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        Thread.sleep(2000);

        doubleClickOn(userList.lookup("#" + testUserTwo.getId()));
        Thread.sleep(2000);

        Assert.assertEquals(testUserTwo.getName(), PrivateViewController.getSelectedChat().getName());

        ListView<PrivateChat> privateChatList = lookup("#privateChatList").query();
        clickOn(privateChatList.lookup("#" + testUserOne.getId()));
        Thread.sleep(2000);

        Assert.assertEquals(testUserOne.getName(), PrivateViewController.getSelectedChat().getName());

        //Additional test if opened private chat is colored
        VBox privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        Assert.assertEquals("-fx-background-color: #666666; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183", privateChatCell.getStyle());

        //Additional test when homeButton is clicked and opened chat is the same
        //Clicking homeButton will load the view - same like clicking on server and back to home
        clickOn("#homeButton");
        Thread.sleep(2000);
        privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        Assert.assertEquals("-fx-background-color: #666666; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183", privateChatCell.getStyle());

        restClient.logout(testUserKeyOne, response -> {
        });

        restClient.logout(testUserKeyTwo, response -> {
        });


    }
}