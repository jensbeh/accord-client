package de.uniks.stp.controller.settings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.homeview.privateview.PrivateMessageTest;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ThemeControllerTest extends ApplicationTest {
    private Stage stage;
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
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        //start application
        ModelBuilder builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        StageManager app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        builder.setLoadUserData(false);

        app.start(stage);
        stage.centerOnScreen();
    }

    @InjectMocks
    StageManager mockApp = new StageManager();

    @BeforeAll
    static void setup() throws IOException {
        MockitoAnnotations.openMocks(PrivateMessageTest.class);
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
                .put("name", "Peter")
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
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "5e2fbd8770dd077d03df600")
                .put("name", "default")
                .put("server", "5e2fbd8770dd077d03df505")
                .put("channels", new JSONArray().put("60b77ba0026b3534ca5a61af")));
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
        JSONArray audioMembers = new JSONArray();
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "60b77ba0026b3534ca5a61af")
                .put("name", "testChannel")
                .put("type", "text")
                .put("privileged", false)
                .put("category", "60b77ba0026b3534ca5a61ae")
                .put("members", members)
                .put("audioMembers", audioMembers));
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


    public void loginInit() throws InterruptedException {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        doCallRealMethod().when(privateChatWebSocket).handleMessage(any());
        doCallRealMethod().when(privateChatWebSocket).setBuilder(any());
        doCallRealMethod().when(privateChatWebSocket).setChatViewController(any());
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
    }

    @Test
    public void changeThemeLogin() {
        VBox root = lookup("#root").query();
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_General").query();
        clickOn(themeButton);
        VBox themeSelect = lookup("#brightMode").query();

        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("ffffff", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));

        themeSelect = lookup("#darkMode").query();
        clickOn(themeSelect);
        Assert.assertEquals("36393f", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
    }

    @Test
    public void changeThemeHomeView() throws InterruptedException {
        loginInit();
        HBox root = lookup("#root").query();
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_General").query();
        clickOn(themeButton);
        VBox themeSelect = lookup("#brightMode").query();

        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("ffffff", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
        themeSelect = lookup("#darkMode").query();
        clickOn(themeSelect);
        Assert.assertEquals("36393f", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
    }


    @Test
    public void changeThemePrivateView() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        ListView<User> userList = lookup("#onlineUsers").query();
        User testUserOne = userList.getItems().get(0);
        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        WaitForAsyncUtils.waitForFxEvents();

        VBox privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        doubleClickOn(privateChatCell);
        TextField textField = lookup("#messageTextField").query();
        String msg1 = "Moin Gusti altes Haus!";
        textField.setText(msg1);
        clickOn("#sendButton");

        JSONObject message = new JSONObject().put("channel", "private").put("timestamp", 942351123).put("message", msg1).put("from", "Peter").put("to", "Gustav");
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        privateChatWebSocket.handleMessage(jsonObject);

        HBox root = lookup("#root").query();
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_General").query();
        clickOn(themeButton);
        VBox themeSelect = lookup("#brightMode").query();

        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("ffffff", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));

        themeSelect = lookup("#darkMode").query();
        clickOn(themeSelect);
        Assert.assertEquals("36393f", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
    }

    @Test
    public void changeThemeServerView() throws InterruptedException {
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        String message = new JSONObject().put("action", "userArrived").put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "Natasha Yar").put("online", true)).toString();
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        serverSystemWebSocket.handleMessage(jsonObject);

        HBox root = lookup("#root").query();
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_General").query();
        clickOn(themeButton);
        VBox themeSelect = lookup("#brightMode").query();

        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("ffffff", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));

        themeSelect = lookup("#darkMode").query();
        clickOn(themeSelect);
        Assert.assertEquals("36393f", root.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
    }

    @Test
    public void changeThemeServerSettings() throws InterruptedException {
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_General").query();
        clickOn(themeButton);
        VBox themeSelect = lookup("#brightMode").query();

        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#deleteServer");
        WaitForAsyncUtils.waitForFxEvents();

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");

        clickOn("#channelBtn");
        WaitForAsyncUtils.waitForFxEvents();
        Pane rootSettings = lookup("#root").query();
        Assert.assertEquals("ffffff", rootSettings.getBackground().getFills().get(0).getFill().toString().substring(2, 8));

        clickOn("#categoryBtn");
        WaitForAsyncUtils.waitForFxEvents();
        rootSettings = lookup("#root").query();
        Assert.assertEquals("ffffff", rootSettings.getBackground().getFills().get(0).getFill().toString().substring(2, 8));

        clickOn("#privilegeBtn");
        clickOn("#privilegeBtn");
        ComboBox<Categories> categoryChoice = lookup("#Category").query();
        ComboBox<ServerChannel> channelChoice = lookup("#Channels").query();
        clickOn(categoryChoice);
        ComboBox<Categories> finalCategoryChoice = categoryChoice;
        interact(() -> finalCategoryChoice.getSelectionModel().select(0));
        Assert.assertEquals(channelChoice.getItems(), mockApp.getBuilder().getCurrentServer().getCategories().get(0).getChannel());

        clickOn(channelChoice);
        ComboBox<ServerChannel> finalChannelChoice = channelChoice;
        interact(() -> finalChannelChoice.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#Privilege_On_Button");
        clickOn("#Change_Privilege");
        rootSettings = lookup("#root").query();
        Assert.assertEquals("ffffff", rootSettings.getBackground().getFills().get(0).getFill().toString().substring(2, 8));

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        clickOn(themeButton);
        themeSelect = lookup("#darkMode").query();
        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#deleteServer");
        WaitForAsyncUtils.waitForFxEvents();

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#channelBtn");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#categoryBtn");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#privilegeBtn");
        clickOn("#privilegeBtn");
        categoryChoice = lookup("#Category").query();
        channelChoice = lookup("#Channels").query();
        clickOn(categoryChoice);
        interact(() -> finalCategoryChoice.getSelectionModel().select(0));
        clickOn(channelChoice);
        interact(() -> finalChannelChoice.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#Privilege_On_Button");
        clickOn("#Change_Privilege");
    }

    @Test
    public void inviteUserThemeTest() throws InterruptedException {
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_General").query();
        clickOn(themeButton);
        VBox themeSelect = lookup("#brightMode").query();
        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        clickOn("#serverMenuButton");
        clickOn("#InviteUsers");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#userLimitSelected");
        Pane rootSettings = lookup("#root").query();
        Assert.assertEquals("ffffff", rootSettings.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
        WaitForAsyncUtils.waitForFxEvents();
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        clickOn(themeButton);
        themeSelect = lookup("#darkMode").query();
        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#serverMenuButton");
        clickOn("#InviteUsers");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#userLimitSelected");
        rootSettings = lookup("#root").query();
        Assert.assertEquals("36393f", rootSettings.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
    }

    @Test
    public void createServerThemeTest() throws InterruptedException {
        loginInit();
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_General").query();
        clickOn(themeButton);
        VBox themeSelect = lookup("#brightMode").query();
        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);
        WaitForAsyncUtils.waitForFxEvents();
        TabPane tapPane = lookup("#tabView").query();
        tapPane.getSelectionModel().select(tapPane.getTabs().get(1));
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("ffffff", tapPane.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        WaitForAsyncUtils.waitForFxEvents();

        settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        clickOn(themeButton);
        themeSelect = lookup("#darkMode").query();
        clickOn(themeSelect);
        WaitForAsyncUtils.waitForFxEvents();
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        addServer = lookup("#addServer").query();
        clickOn(addServer);
        WaitForAsyncUtils.waitForFxEvents();
        tapPane = lookup("#tabView").query();
        tapPane.getSelectionModel().select(tapPane.getTabs().get(1));
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("202225", tapPane.getBackground().getFills().get(0).getFill().toString().substring(2, 8));
    }
}
