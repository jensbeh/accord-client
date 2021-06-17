package de.uniks.stp;

import de.uniks.stp.controller.HomeViewController;
import de.uniks.stp.model.Server;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerSettingsControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private String testUserMainName;
    private String testUserMainPw;
    private final String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
    private String testUserOneName;
    private String testUserOnePw;
    private String testUserOne_UserKey;
    private String testServerId;
    private final String testServerName = "TestServer Team Bit Shift";

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
        System.setProperty("testfx.headless", "false");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
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

    /*TODO not needed -> remove?*/
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
                return null;
            }
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockTempLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("name", "Test User").put("password", "testPassword"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).loginTemp(callbackCaptor.capture());
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

    public void mockPostServer() {
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
                return null;
            }
        }).when(restClient).postServer(anyString(), anyString(), callbackCaptor.capture());
    }

    public void loginInit(String name, String password) {
        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(name);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(password);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void getServerId() {
        mockTempLogin();
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });

        mockLogin();
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            testUserOne_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });

        mockPostServer();
        restClient.postServer(testUserOne_UserKey, "TestServer Team Bit Shift", response -> {
        });
        testServerId = "ri9fdrSw0fj90";

        mockLogout();
        restClient.logout(testUserOne_UserKey, response -> {
        });
    }

    /*TODO: activate when WebSocket is being mocked AND fix test*/
    //@Test
    public void openServerSettingsTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(2, this.listTargetWindows().size());
        String serverSettingsTitle = "";
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                serverSettingsTitle = ((Stage) object).getTitle();
                Assert.assertNotEquals("", serverSettingsTitle);
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        Thread.sleep(2000);
    }

    /*TODO: activate when WebSocket is being mocked AND fix test*/
    //@Test
    public void clickOnOwnerOverview() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#server"));
        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        Assert.assertNotEquals(1, this.listTargetWindows().size());
        clickOn("#overview");
        clickOn("#deleteServer");
        Label serverNameLabel = lookup("#serverName").query();
        Button leaveButton = lookup("#deleteServer").query();
        Assert.assertEquals("TestServer Team Bit Shift", serverNameLabel.getText());
        Assert.assertEquals("Delete Server", leaveButton.getText());
    }

    /*TODO: activate when WebSocket is being mocked AND fix test*/
    //@Test
    public void changeServerNameAndDeleteServer() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);
        TextField serverName = lookup("#serverName").query();
        serverName.setText("TestServer");
        clickOn("#createServer");
        Assert.assertEquals("TestServer", serverName.getText());
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        System.out.println("ServerList: " + serverList.getItems().toString());
        String serverId = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("TestServer")) {
                serverId = server.getId();
            }
        }
        Thread.sleep(2000);
        clickOn(serverList.lookup("#serverName_" + serverId));
        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        Assert.assertNotEquals(1, this.listTargetWindows().size());
        MenuButton menuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer", menuButton.getText());

        //change ServerName
        TextField serverNameField = lookup("#nameText").query();
        serverNameField.setText("Test2");
        clickOn("#changeName");
        String serverIdChangedName = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("Test2")) {
                serverIdChangedName = server.getId();
            }
        }
        Assert.assertEquals(serverId, serverIdChangedName);

        //delete Server
        clickOn("#deleteServer");
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
                Assert.assertEquals("ServerSettings", result);
                Platform.runLater(((Stage) s)::close);
                Thread.sleep(2000);
                break;
            }
        }

        //check all changed Names
        menuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals("Test2", menuButton.getText());

        //click on delete Button (Alert)
        moveBy(-50, -105);
        clickOn();

        //check if server doesn't exist anymore
        serverId = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("TestServer")) {
                serverId = server.getId();
            }
        }
        Assert.assertEquals("", serverId);
        System.out.println("ServerList: " + serverList.getItems().toString());
    }
}
