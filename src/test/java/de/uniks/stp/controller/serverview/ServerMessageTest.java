package de.uniks.stp.controller.serverview;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.*;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerMessageTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;
    private final String testServerName = "TestServer Team Bit Shift";
    private final String testServerId = "5e2fbd8770dd077d03df505";
    private final String testUserMainName = "Hendry Bracken";
    private final String testUserMainPw = "stp2021pw";

    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;


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
        //start application
        builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        builder.setLoadUserData(false);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(ServerMessageTest.class);
    }

    public void mockLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("userKey", testUserMainPw));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            mockGetServers();
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
        JSONArray members = new JSONArray().put(new JSONObject().put("id", testServerOwner).put("name", testUserMainName).put("online", true));
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

    public void mockUpdateMessage() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "5e2fbd8770dd077d03dr458").put("channel", "60adc8aec77d3f78988b57a0").put("timestamp", "1616935874361")
                        .put("from", "Hendry Bracken").put("text", "Okay?"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response9.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor9.getValue();
            callback.completed(response9);
            return null;
        }).when(restClient).updateMessage(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), callbackCaptor9.capture());
    }

    public void mockGetChannelMessagesURL() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response10.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor8.getValue();
            callback.completed(response10);
            return null;
        }).when(restClient).getChannelMessages(anyLong(), anyString(), anyString(), anyString(), anyString(), callbackCaptor10.capture());
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
        mockUpdateMessage();

        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserMainName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserMainPw);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testSendAllMessage() throws InterruptedException {
        doCallRealMethod().when(serverChatWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverChatWebSocket).handleMessage(any());
        doCallRealMethod().when(serverChatWebSocket).setBuilder(any());
        doCallRealMethod().when(serverChatWebSocket).setChatViewController(any());
        serverChatWebSocket.setBuilder(builder);
        doCallRealMethod().when(serverSystemWebSocket).setChatViewController(any());
        doCallRealMethod().when(serverSystemWebSocket).setServerViewController(any());
        doCallRealMethod().when(serverSystemWebSocket).handleMessage(any());
        doCallRealMethod().when(serverSystemWebSocket).setBuilder(any());
        serverSystemWebSocket.setBuilder(builder);

        String messageIdA = "5e2fbd8770dd077d03dr458A";
        String messageIdB = "5e2fbd8770dd077d03dr458B";
        String messageIdC = "5e2fbd8770dd077d03dr458C";
        String messageIdD = "5e2fbd8770dd077d03dr458D";
        loginInit(true);

        Platform.runLater(() -> Assert.assertEquals("Accord - Main", stage.getTitle()));

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

        ServerChannel channel = app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().get(0);
        ListView<User> channelList = lookup("#scrollPaneCategories").lookup("#categoryVbox").lookup("#channelList").query();
        doubleClickOn(channelList.lookup("#" + channel.getId()));

        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#sendButton");

        JSONObject message = new JSONObject().put("channel", channel.getId()).put("timestamp", 9257980).put("text", "Okay!").put("from", testUserMainName).put("id", messageIdA);
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverChatWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        ListView<Message> privateChatMessageList = lookup("#messageListView").query();

        ScrollPane messageScrollPane = (ScrollPane) lookup("#messageScrollPane");
        VBox container = (VBox) messageScrollPane.getContent().lookup("#messageVBox");
        Assert.assertEquals(1, container.getChildren().size());

        messageField.setText("Okay");
        write("\n");

        message = new JSONObject().put("channel", channel.getId()).put("timestamp", 9257999).put("text", "Okay").put("from", testUserMainName).put("id", messageIdB);
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverChatWebSocket.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        boolean msgArrived = false;
        for (int i = 0; i < privateChatMessageList.getItems().size(); i++) {
            if (privateChatMessageList.getItems().get(i).getMessage().equals("Okay")) {
                msgArrived = true;
            }
        }
        Assert.assertTrue(msgArrived);

        privateChatMessageList.getSelectionModel().select(0);
        rightClickOn(privateChatMessageList);
        ContextMenu contextMenu = lookup("#messageListView").queryListView().getContextMenu();
        Assert.assertEquals(3, contextMenu.getItems().size());
        Assert.assertTrue(contextMenu.getItems().get(0).isVisible());
        Assert.assertTrue(contextMenu.getItems().get(1).isVisible());
        Assert.assertTrue(contextMenu.getItems().get(2).isVisible());

        interact(() -> contextMenu.getItems().get(0).fire());
        clickOn(messageField);

        write("\n");
        message = new JSONObject().put("channel", channel.getId()).put("timestamp", 9257999).put("text", privateChatMessageList.getItems().get(0).getMessage()).put("from", testUserMainName).put("id", messageIdC);
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverChatWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(privateChatMessageList.getItems().get(2).getMessage(), privateChatMessageList.getItems().get(0).getMessage());

        String text = "Hier ein langer Text zum Testen: Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
                "labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
                "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, " +
                "consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam " +
                "voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus " +
                "est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
                "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
                "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        messageField.setText(text);
        write("\n");
        message = new JSONObject().put("channel", channel.getId()).put("timestamp", 9257999).put("text", text).put("from", testUserMainName).put("id", messageIdD);
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverChatWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();
        privateChatMessageList.getSelectionModel().select(3);
        rightClickOn(privateChatMessageList);
        interact(() -> contextMenu.getItems().get(2).fire());
        Label msg = lookup("#deleteWarning").query();
        Assert.assertEquals(msg.getText(), "Are you sure you want to delete " + "\n" + "the following message:");
        Button no = lookup("#chooseCancel").query();
        Assert.assertEquals(no.getText(), "NO");
        Button yes = lookup("#chooseDelete").query();
        Assert.assertEquals(yes.getText(), "YES");
        ScrollPane pane = lookup("#deleteMsgScroll").query();
        EmojiTextFlow emojiTextFlow = (EmojiTextFlow) pane.getContent();
        StringBuilder sb = new StringBuilder();
        for (Node node : emojiTextFlow.getChildren()) {
            if (node instanceof Text) {
                sb.append(((Text) node).getText());
            }
        }

        msgArrived = false;
        for (int i = 0; i < privateChatMessageList.getItems().size(); i++) {
            if (privateChatMessageList.getItems().get(i).getMessage().equals(text)) {
                msgArrived = true;
            }
        }
        Assert.assertTrue(msgArrived);

        String fullText = sb.toString();
        fullText = fullText.replace("\n", "");
        Assert.assertEquals(fullText, text);
        clickOn("#chooseDelete");

        String message2 = new JSONObject().put("action", "messageDeleted").put("data", new JSONObject().put("id", messageIdD).put("category", "5e2fbd8770dd077d03df600").put("channel", channel.getId())).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message2);
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        msgArrived = false;
        for (int i = 0; i < privateChatMessageList.getItems().size(); i++) {
            if (privateChatMessageList.getItems().get(i).getMessage().equals(text)) {
                msgArrived = true;
            }
        }
        Assert.assertFalse(msgArrived);

        Button send = lookup("#sendButton").query();
        Assert.assertEquals(send.getText(), "send");

        privateChatMessageList.getSelectionModel().select(0);
        rightClickOn(privateChatMessageList);
        interact(() -> contextMenu.getItems().get(1).fire());
        Button edit = lookup("#editButton").query();
        Assert.assertEquals(edit.getText(), "edit");
        Button abort = lookup("#abortButton").query();
        Assert.assertEquals(abort.getText(), "abort");

        HBox messageBox = lookup("#messageBox").query();
        Assert.assertTrue(messageBox.getChildren().contains(edit));
        Assert.assertTrue(messageBox.getChildren().contains(abort));
        Assert.assertFalse(messageBox.getChildren().contains(send));

        messageField.setText("Okay?");
        clickOn(messageField);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();

        message = new JSONObject().put("action", "messageUpdated").put("data", new JSONObject().put("id", messageIdA)
                .put("channel", "60adc8aec77d3f78988b57a0").put("timestamp", "1616935874361")
                .put("from", "Hendry Bracken").put("text", "Okay?"));
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message.toString());
        serverSystemWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Okay?", privateChatMessageList.getItems().get(0).getMessage());
        Assert.assertFalse(messageBox.getChildren().contains(edit));
        Assert.assertFalse(messageBox.getChildren().contains(abort));
        Assert.assertTrue(messageBox.getChildren().contains(send));

        Thread.sleep(2000);

        //Emoji List test
        Platform.runLater(() -> clickOn("#emojiButton"));
        moveBy(-290, -150);
        clickOn();
        //Assert.assertEquals(":ng:", messageField.getText());
    }
}
