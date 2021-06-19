package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.subcontroller.ServerSettingsChannelController;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.net.*;
import javafx.application.Platform;
import javafx.scene.control.*;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doCallRealMethod;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerSettingsChannelControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
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

    @Mock
    private HttpResponse<JsonNode> response10;

    @Mock
    private HttpResponse<JsonNode> response11;

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

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor10;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor11;

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
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(ServerSettingsChannelController.class);
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

    public void mockUpdateChannel() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "60adc8aec77d3f78988b57a0")
                        .put("name", "TestChannel").put("type", "text").put("privileged", false).put("category", "5e2fbd8770dd077d03df600")
                        .put("members", new String[0]).put("audioMembers", new String[0]));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response9.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor9.getValue();
            callback.completed(response9);
            return null;
        }).when(restClient).updateChannel(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(),  callbackCaptor9.capture());
    }

    public void mockCreateChannel() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "60adc8aec77d3f78988b5XXX")
                        .put("name", "NewTestChannel").put("type", "text").put("privileged", false).put("category", "5e2fbd8770dd077d03df600")
                        .put("members", new String[0]).put("audioMembers", new String[0]));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response10.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor10.getValue();
            callback.completed(response10);
            return null;
        }).when(restClient).createChannel(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(),  callbackCaptor10.capture());
    }

    public void mockDeleteChannel() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "60adc8aec77d3f78988b5XXX")
                        .put("name", "NewTestChannel").put("type", "text").put("privileged", false).put("category", "5e2fbd8770dd077d03df600")
                        .put("members", new String[0]).put("audioMembers", new String[0]));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response11.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor11.getValue();
            callback.completed(response11);
            return null;
        }).when(restClient).deleteChannel(anyString(), anyString(), anyString(), anyString(), callbackCaptor11.capture());
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
        mockUpdateChannel();
        mockCreateChannel();
        mockDeleteChannel();

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
    public void openServerChannelSettingsTest() throws InterruptedException {
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());

        serverSystemWebSocket.setBuilder(builder);
        loginInit(false);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#server"));
        WaitForAsyncUtils.waitForFxEvents();

        Server currentServer = null;
        for (Server server : serverListView.getItems()) {
            if (server.getId().equals(testServerId)) {
                currentServer = server;
            }
        }

        Assert.assertEquals("Accord - Main", stage.getTitle());

        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

        doubleClickOn("#60adc8aec77d3f78988b57a0");

        clickOn("#serverMenuButton");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#ServerSettings");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#channel");
        WaitForAsyncUtils.waitForFxEvents();

        Label categoryLabel = lookup("#categoryLabel").query();
        ComboBox<Categories> categorySelector = lookup("#categorySelector").query();
        Label editChannelsLabel = lookup("#editChannelsLabel").query();
        ComboBox<ServerChannel> editChannelsSelector = lookup("#editChannelsSelector").query();
        TextField editChannelsTextField = lookup("#editChannelsTextField").query();
        Button channelChangeButton = lookup("#channelChangeButton").query();
        Button channelDeleteButton = lookup("#channelDeleteButton").query();
        Label createChannelLabel = lookup("#createChannelLabel").query();
        TextField createChannelTextField = lookup("#createChannelTextField").query();
        RadioButton channelTextRadioButton = lookup("#channelTextRadioButton").query();
        RadioButton channelVoiceRadioButton = lookup("#channelVoiceRadioButton").query();
        Button channelCreateButton = lookup("#channelCreateButton").query();

        Assert.assertEquals("Category", categoryLabel.getText());
        Assert.assertEquals("Edit Channels", editChannelsLabel.getText());
        Assert.assertEquals("Create Channel", createChannelLabel.getText());
        Assert.assertEquals("change", channelChangeButton.getText());
        Assert.assertEquals("Delete", channelDeleteButton.getText());
        Assert.assertEquals("create", channelCreateButton.getText());
        Assert.assertEquals("Text", channelTextRadioButton.getText());
        Assert.assertEquals("Voice", channelVoiceRadioButton.getText());

        // Test clicking Category selector
        clickOn(categorySelector);
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> categorySelector.getSelectionModel().select(0));

        // Test clicking Channel selector
        clickOn(editChannelsSelector);
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> editChannelsSelector.getSelectionModel().select(0));

        // Change Channel Name
        clickOn(editChannelsTextField);
        editChannelsTextField.setText("TestChannel");
        clickOn(channelChangeButton);

        JSONObject message = new JSONObject().put("action", "channelUpdated").put("data", new JSONObject()
                .put("id", "60adc8aec77d3f78988b57a0")
                .put("name", "TestChannel").put("type", "text").put("privileged", false).put("category", "5e2fbd8770dd077d03df600")
                .put("members", new String[0]).put("audioMembers", new String[0]));

        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverSystemWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();
        assert currentServer != null;
        Assert.assertEquals("TestChannel", currentServer.getCategories().get(0).getChannel().get(0).getName());
        Assert.assertEquals("", editChannelsTextField.getText());


        // Create Channel
        int channelSize = currentServer.getCategories().get(0).getChannel().size();
        clickOn(createChannelTextField);
        createChannelTextField.setText("NewTestChannel");
        clickOn(channelCreateButton);

        message = new JSONObject().put("action", "channelCreated").put("data", new JSONObject()
                .put("id", "60adc8aec77d3f78988b5XXX")
                .put("name", "NewTestChannel").put("type", "text").put("privileged", false).put("category", "5e2fbd8770dd077d03df600")
                .put("members", new String[0]).put("audioMembers", new String[0]));

        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverSystemWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channelSize + 1, currentServer.getCategories().get(0).getChannel().size());
        Assert.assertEquals("", createChannelTextField.getText());
        boolean found = false;
        for (ServerChannel channel : app.getBuilder().getCurrentServer().getCategories().get(0).getChannel()) {
            if (channel.getName().equals("NewTestChannel")) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);


        // Delete (created) Channel, rename first NewTestChannel to ByeChannel to not click on Server View
        channelSize = currentServer.getCategories().get(0).getChannel().size();
        editChannelsSelector.getItems().get(editChannelsSelector.getItems().size() - 1).setName("ByeChannel");
        clickOn(editChannelsSelector);
        clickOn("ByeChannel");
        clickOn(channelDeleteButton);

        message = new JSONObject().put("action", "channelDeleted").put("data", new JSONObject()
                .put("id", "60adc8aec77d3f78988b5XXX")
                .put("name", "NewTestChannel").put("type", "text").put("privileged", false).put("category", "5e2fbd8770dd077d03df600")
                .put("members", new String[0]).put("audioMembers", new String[0]));

        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverSystemWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channelSize - 1, currentServer.getCategories().get(0).getChannel().size());
        found = false;
        for (ServerChannel channel : app.getBuilder().getCurrentServer().getCategories().get(0).getChannel()) {
            if (channel.getName().equals("ByeChannel")) {
                found = true;
                break;
            }
        }
        Assert.assertFalse(found);

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
    }
}
