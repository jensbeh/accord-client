package de.uniks.stp;

import de.uniks.stp.controller.HomeViewController;
import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HomeViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private final String testServerName = "TestServer Team Bit Shift";
    // main user
    private final String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
    private final String testUserName = "Hendry Bracken";
    private final String testUserPw = "stp2021pw";

    @Mock
    private RestClient restClient;

    @Mock
    private HttpResponse<JsonNode> response;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @InjectMocks
    StageManager mockApp = new StageManager();

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
        app = mockApp;
        app.setRestClient(restClient);
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(HomeViewController.class);
    }

    public void mockLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("userKey", userKey));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                mockGetServers();
                return null;
            }
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockLogout() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "Logged out")
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).logout(anyString(), callbackCaptor.capture());
    }

    public void mockPostServerGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testServerName));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                mockGetServers(); // Mock Get Servers after Server created
                return null;
            }
        }).when(restClient).postServer(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockGetUsers(String id, String name) {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", id).put("name", name)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).getUsers(anyString(), callbackCaptor.capture());
    }

    public void mockGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testServerName)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).getServers(anyString(), callbackCaptor.capture());
    }

    public void loginInit() throws InterruptedException {
        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserPw);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void personalUserTest() throws InterruptedException {
        loginInit();

        Label personalUserName = lookup("#currentUserBox").lookup("#userName").query();

        Assert.assertEquals(testUserName, personalUserName.getText());
    }

    @Test
    public void serverBoxTest() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverNameInput = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverNameInput.setText("TestServer Team Bit Shift");
        mockPostServerGetServers();
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();

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
        /*TODO: WebSocket test user join with name Test User*/
        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        ObservableList<User> itemList = userList.getItems();
        String userName = "";
        for (User user : itemList) {
            if (user.getName().equals("Test User")) {
                userName = user.getName();
                break;
            }
        }
        Assert.assertEquals("Test User", userName);

        /*TODO: WebSocket test user left with name testUserOneName*/
        WaitForAsyncUtils.waitForFxEvents();

        itemList = userList.getItems();
        userName = "";
        for (User user : itemList) {
            if (user.getName().equals("Test User")) {
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
        Assert.assertNotEquals(0, response.getBody().getObject().getJSONArray("data").length());
    }

    @Test
    public void getUsersTest() {
        mockGetUsers("Test User", "5e2ffg75dd077d03df505");
        restClient.getUsers(userKey, response -> {
        });
        Assert.assertNotEquals(0, response.getBody().getObject().getJSONArray("data").length());
    }

    //@Test
    public void privateChatTest() throws InterruptedException {
        loginInit();
        /*TODO: WebSocket test user join with name Test User (and ID 5e2ffg75dd077d03df505)*/
        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        User testUserOne = userList.getItems().get(0);
        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        WaitForAsyncUtils.waitForFxEvents();
        ListView<PrivateChat> privateChatList = lookup("#privateChatList").query();
        Assert.assertEquals(testUserOne.getName(), privateChatList.getItems().get(0).getName());
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

    //@Test
    public void logoutMultiLogin() throws InterruptedException {
        loginInit();
        /*TODO: WebSocket user left with name testUserName (own User)*/
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Accord - Login", stage.getTitle());
        mockLogout();
        restClient.logout(userKey, response -> {
        });
    }

    //@Test
    public void openExistingChat() throws InterruptedException {
        loginInit();

        /*TODO: WebSocket test user join with name Test User (and ID 5e2ffg75dd077d03df505)*/
        /*TODO: WebSocket test user join with name Test User 2 (and ID 5940kf93ued390ir3ud84)*/
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