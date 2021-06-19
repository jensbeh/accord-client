package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.subcontroller.ServerSettingsController;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.*;
import javafx.application.Platform;
import javafx.scene.control.*;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerSettingsControllerTest extends ApplicationTest {

    private Stage stage;
    private final String testUserName = "Hendry Bracken";
    private final String testServerId = "5e2fbd8770dd077d03df505";
    private final String testServerName = "TestServer Team Bit Shift";

    @Mock
    private RestClient restClient;

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
    private HttpResponse<JsonNode> response6;

    @Mock
    private HttpResponse<JsonNode> response7;

    @Mock
    private HttpResponse<JsonNode> response8;

    @Mock
    private HttpResponse<JsonNode> response9;

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

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor6;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor7;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor8;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor9;

    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;

    @InjectMocks
    StageManager mockApp = new StageManager();
    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketCLient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        StageManager app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(ServerSettingsController.class);
    }

    public void mockLogin() {
        String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("userKey", userKey));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            return null;
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockPostServer() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testServerName));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response2.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor2.getValue();
            callback.completed(response2);
            return null;
        }).when(restClient).postServer(anyString(), anyString(), callbackCaptor2.capture());
    }

    public void mockGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testServerName)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response3.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor3.getValue();
            callback.completed(response3);
            return null;
        }).when(restClient).getServers(anyString(), callbackCaptor3.capture());
    }

    public void mockGetServerUsers() {
        String[] categories = new String[1];
        categories[0] = "5e2fbd8770dd077d03df600";
        String testServerOwner = "5e2iof875dd077d03df505";
        JSONArray members = new JSONArray().put(new JSONObject().put("id", testServerOwner).put("name", testUserName).put("online", true));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", testServerId).put("name", testServerName).put("owner", testServerOwner).put("categories", categories).put("members", members));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response4.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor4.getValue();
            callback.completed(response4);
            return null;
        }).when(restClient).getServerUsers(anyString(), anyString(), callbackCaptor4.capture());
    }

    public void mockGetServerCategories() {
        String[] channels = new String[1];
        channels[0] = "60adc8aec77d3f78988b57a0";
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df600").put("name", "default")
                        .put("server", "5e2fbd8770dd077d03df505").put("channels", channels)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response5.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor5.getValue();
            callback.completed(response5);
            return null;
        }).when(restClient).getServerCategories(anyString(), anyString(), callbackCaptor5.capture());
    }

    public void mockGetCategoryChannels() {
        String[] members = new String[0];
        String[] audioMembers = new String[0];
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "60adc8aec77d3f78988b57a0").put("name", "general").put("type", "text")
                        .put("privileged", false).put("category", "5e2fbd8770dd077d03df600").put("members", members).put("audioMembers", audioMembers)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response6.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor6.getValue();
            callback.completed(response6);
            mockGetCategoryChannels();
            return null;
        }).when(restClient).getCategoryChannels(anyString(), anyString(), anyString(), callbackCaptor6.capture());
    }

    public void mockGetServersEmpty() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response7.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor7.getValue();
            callback.completed(response7);
            return null;
        }).when(restClient).getServers(anyString(), callbackCaptor7.capture());
    }

    public void mockGetChannelMessages() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response8.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor8.getValue();
            callback.completed(response8);
            return null;
        }).when(restClient).getChannelMessages(anyLong(), anyString(), anyString(), anyString(), anyString(), callbackCaptor8.capture());
    }

    public void mockPutServer() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", testServerId).put("name", "TestServer Team Bit Shift Renamed"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response9.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor9.getValue();
            callback.completed(response9);
            return null;
        }).when(restClient).putServer(anyString(), anyString(), anyString(), callbackCaptor9.capture());
    }

    public void loginInit(boolean emptyServers) throws InterruptedException {
        mockPostServer();
        if (!emptyServers)
            mockGetServers();
        else
            mockGetServersEmpty();
        mockGetServerUsers();
        mockGetServerCategories();
        mockGetCategoryChannels();
        mockGetChannelMessages();
        mockPutServer();

        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        String testUserPw = "stp2021pw";
        passwordField.setText(testUserPw);
        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void openServerSettingsTest() throws InterruptedException {
        loginInit(false);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(2, this.listTargetWindows().size());
        String serverSettingsTitle;
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                serverSettingsTitle = ((Stage) object).getTitle();
                Assert.assertNotEquals("", serverSettingsTitle);
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
    }

    @Test
    public void clickOnOwnerOverview() throws InterruptedException {
        loginInit(false);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#server"));
        WaitForAsyncUtils.waitForFxEvents();

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

    @Test
    public void changeServerNameAndDeleteServer() throws InterruptedException {
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());

        serverSystemWebSocket.setBuilder(builder);
        loginInit(false);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#server"));
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        System.out.println("ServerList: " + serverList.getItems().toString());

        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        Assert.assertNotEquals(1, this.listTargetWindows().size());
        MenuButton menuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer Team Bit Shift", menuButton.getText());

        //change ServerName
        TextField serverNameField = lookup("#nameText").query();
        serverNameField.setText("TestServer Team Bit Shift Renamed");
        clickOn("#changeName");
        String serverIdChangedName = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("TestServer Team Bit Shift Renamed")) {
                serverIdChangedName = server.getId();
            }
        }
        Assert.assertEquals(testServerId, serverIdChangedName);

        JSONObject message = new JSONObject().put("action", "serverUpdated").put("data", new JSONObject()
                .put("id", testServerId)
                .put("name", "TestServer Team Bit Shift Renamed"));
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverSystemWebSocket.handleMessage(jsonObject);

        //delete Server
        clickOn("#deleteServer");
        String result;
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
                Assert.assertEquals("ServerSettings", result);
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        //check all changed Names
        menuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer Team Bit Shift Renamed", menuButton.getText());

        //click on delete Button (Alert)
        moveBy(-50, -105);
        clickOn();

        message = new JSONObject().put("action", "serverDeleted").put("data", new JSONObject()
                .put("id", testServerId)
                .put("name", "TestServer Team Bit Shift Renamed"));
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverSystemWebSocket.handleMessage(jsonObject);

        //check if server doesn't exist anymore
        String serverId = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("TestServer Team Bit Shift Renamed")) {
                serverId = server.getId();
            }
        }
        Assert.assertEquals("", serverId);
        System.out.println("ServerList: " + serverList.getItems().toString());
    }
}
