package de.uniks.stp.net.websockets;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.serverview.ServerMessageTest;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.scene.control.*;
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
import javax.websocket.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WebSocketTest extends ApplicationTest {
    @InjectMocks
    StageManager mockApp = new StageManager();
    private Stage stage;
    private StageManager app;
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
    private HttpResponse<JsonNode> response3;
    @Mock
    private HttpResponse<JsonNode> response2;
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

    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(ServerMessageTest.class);
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
        app.setBuilder(builder);
        app.setRestClient(restClient);

        builder.setLoadUserData(false);
        mockApp.getBuilder().setSpotifyShow(false);
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().setSpotifyRefresh(null);

        app.start(stage);
        this.stage.centerOnScreen();

    }

    @Test
    public void testPrivateChatWebSocketTest() throws IOException {
        doCallRealMethod().when(privateChatWebSocket).stop();
        doCallRealMethod().when(privateChatWebSocket).setBuilder(any());
        doCallRealMethod().when(privateChatWebSocket).startNoopTimer();
        doCallRealMethod().when(privateChatWebSocket).sendMessage(any());
        doCallRealMethod().when(privateChatWebSocket).showNoConAlert();
        doCallRealMethod().when(privateChatWebSocket).onOpen(any(), any());
        doCallRealMethod().when(privateChatWebSocket).getPrivateViewController();
        Assert.assertNull(privateChatWebSocket.getPrivateViewController());
        WaitForAsyncUtils.waitForFxEvents();
        privateChatWebSocket.setBuilder(builder);
        privateChatWebSocket.showNoConAlert();
        WaitForAsyncUtils.waitForFxEvents();
        String result;
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
                Assert.assertEquals("No Connection Error", result);
                Platform.runLater(((Stage) s)::close);
                break;
            }
        }
        WaitForAsyncUtils.waitForFxEvents();
        privateChatWebSocket.startNoopTimer();

        privateChatWebSocket.onOpen(getSession(), getEndpoint());
        WaitForAsyncUtils.waitForFxEvents();
        privateChatWebSocket.stop();
    }

    @Test
    public void testPrivateWebSocketIsBlockedTest() throws InterruptedException {
        doCallRealMethod().when(privateChatWebSocket).handleMessage(any());

        loginInit();

        WaitForAsyncUtils.waitForFxEvents();
        ListView<User> userList = lookup("#onlineUsers").query();
        User testUser = userList.getItems().get(0);
        Label name = lookup("#" + testUser.getId()).query();
        doubleClickOn(name);
        WaitForAsyncUtils.waitForFxEvents();

        rightClickOn();
        ContextMenu contextMenu = name.getContextMenu();
        interact(() -> contextMenu.getItems().get(0).fire());
        WaitForAsyncUtils.waitForFxEvents();

        String message = new JSONObject().put("channel", "private").put("from", testUser.getName()).put("message", "Test").toString();
        JsonObject jsonObject = (JsonObject) JsonUtil.toJson(message);
        privateChatWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testServerSystemWebSocketTest() {
        doCallRealMethod().when(serverSystemWebSocket).onOpen(any(), any());
        doCallRealMethod().when(serverSystemWebSocket).onClose(any(), any());
        doCallRealMethod().when(serverSystemWebSocket).startNoopTimer();
        serverSystemWebSocket.startNoopTimer();
        serverSystemWebSocket.onOpen(getSession(), getEndpoint());
        WaitForAsyncUtils.waitForFxEvents();
        serverSystemWebSocket.onClose(getSession(), getCloseReason());
    }

    @Test
    public void testServerSystemWebSocketStopTest() throws IOException {
        doCallRealMethod().when(serverSystemWebSocket).onOpen(any(), any());
        doCallRealMethod().when(serverSystemWebSocket).stop();
        doCallRealMethod().when(serverSystemWebSocket).startNoopTimer();

        serverSystemWebSocket.startNoopTimer();
        serverSystemWebSocket.onOpen(getSession(), getEndpoint());
        serverSystemWebSocket.stop();
    }

    @Test
    public void testServerChatWebSocketTest() throws IOException {
        doCallRealMethod().when(serverChatWebSocket).setBuilder(any());
        doCallRealMethod().when(serverChatWebSocket).getBuilder();
        doCallRealMethod().when(serverChatWebSocket).handleMessage(any());
        doCallRealMethod().when(serverChatWebSocket).sendMessage(any());
        doCallRealMethod().when(serverChatWebSocket).onOpen(any(), any());
        doCallRealMethod().when(serverChatWebSocket).onClose(any(), any());
        doCallRealMethod().when(serverChatWebSocket).stop();
        doCallRealMethod().when(serverChatWebSocket).startNoopTimer();

        serverChatWebSocket.setBuilder(builder);
        Assert.assertNotNull(serverChatWebSocket.getBuilder());
        serverChatWebSocket.sendMessage("noop");

        String message = new JSONObject().put("action", "info").put("data", new JSONObject().put("message", "This is not your username.")).toString();
        JsonObject jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverChatWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        message = new JSONObject().put("action", "info").put("data", new JSONObject().put("message", "Fail")).toString();
        jsonObject = (JsonObject) JsonUtil.toJson(message);
        serverChatWebSocket.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        serverChatWebSocket.startNoopTimer();
        serverChatWebSocket.onOpen(getSession(), getEndpoint());
        WaitForAsyncUtils.waitForFxEvents();
        serverChatWebSocket.onClose(getSession(), getCloseReason());
    }

    @Test
    public void testServerChatWebSocketStopTest() throws IOException {
        doCallRealMethod().when(serverChatWebSocket).onOpen(any(), any());
        doCallRealMethod().when(serverChatWebSocket).stop();
        doCallRealMethod().when(serverChatWebSocket).startNoopTimer();

        serverChatWebSocket.startNoopTimer();
        serverChatWebSocket.onOpen(getSession(), getEndpoint());
        serverChatWebSocket.stop();
    }

    @Test
    public void testPrivateSystemWebSocketTest() throws InterruptedException, IOException {
        doCallRealMethod().when(privateSystemWebSocketClient).onOpen(any(), any());
        doCallRealMethod().when(privateSystemWebSocketClient).onClose(any(), any());
        doCallRealMethod().when(privateSystemWebSocketClient).sendMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).onOpen(any(), any());
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).startNoopTimer();
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());

        privateSystemWebSocketClient.startNoopTimer();
        privateSystemWebSocketClient.onOpen(getSession(), getEndpoint());
        WaitForAsyncUtils.waitForFxEvents();
        privateSystemWebSocketClient.onClose(getSession(), getCloseReason());


        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#server");
        User u1 = new User();
        builder.getCurrentServer().withUser(u1).getUser().get(0).setId("tmp").setName("TMP").setStatus(true);
        builder.getPersonalUser().setId("tmp");
        privateSystemWebSocketClient.setBuilder(builder);

        String message = new JSONObject().put("action", "userDescriptionChanged").put("data", new JSONObject().put("id", "tmp").put("description", "Test Description")).toString();
        JsonObject jsonObject = (JsonObject) JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testPrivateSystemWebSocketStopTest() throws IOException {
        doCallRealMethod().when(privateSystemWebSocketClient).onOpen(any(), any());
        doCallRealMethod().when(privateSystemWebSocketClient).stop();
        doCallRealMethod().when(privateSystemWebSocketClient).startNoopTimer();

        privateSystemWebSocketClient.startNoopTimer();
        privateSystemWebSocketClient.onOpen(getSession(), getEndpoint());
        privateSystemWebSocketClient.stop();
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
                .put("description", "Test")
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


    public Session getSession() {
        return new Session() {
            @Override
            public WebSocketContainer getContainer() {
                return null;
            }

            @Override
            public void addMessageHandler(MessageHandler handler) throws IllegalStateException {

            }

            @Override
            public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) {

            }

            @Override
            public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) {

            }

            @Override
            public Set<MessageHandler> getMessageHandlers() {
                return null;
            }

            @Override
            public void removeMessageHandler(MessageHandler handler) {

            }

            @Override
            public String getProtocolVersion() {
                return null;
            }

            @Override
            public String getNegotiatedSubprotocol() {
                return null;
            }

            @Override
            public List<Extension> getNegotiatedExtensions() {
                return null;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public long getMaxIdleTimeout() {
                return 0;
            }

            @Override
            public void setMaxIdleTimeout(long milliseconds) {

            }

            @Override
            public void setMaxBinaryMessageBufferSize(int length) {

            }

            @Override
            public int getMaxBinaryMessageBufferSize() {
                return 0;
            }

            @Override
            public void setMaxTextMessageBufferSize(int length) {

            }

            @Override
            public int getMaxTextMessageBufferSize() {
                return 0;
            }

            @Override
            public RemoteEndpoint.Async getAsyncRemote() {
                return null;
            }

            @Override
            public RemoteEndpoint.Basic getBasicRemote() {
                return new RemoteEndpoint.Basic() {
                    @Override
                    public void sendText(String text) {

                    }

                    @Override
                    public void sendBinary(ByteBuffer data) {

                    }

                    @Override
                    public void sendText(String partialMessage, boolean isLast) {

                    }

                    @Override
                    public void sendBinary(ByteBuffer partialByte, boolean isLast) {

                    }

                    @Override
                    public OutputStream getSendStream() {
                        return null;
                    }

                    @Override
                    public Writer getSendWriter() {
                        return null;
                    }

                    @Override
                    public void sendObject(Object data) {

                    }

                    @Override
                    public void setBatchingAllowed(boolean allowed) {

                    }

                    @Override
                    public boolean getBatchingAllowed() {
                        return false;
                    }

                    @Override
                    public void flushBatch() {

                    }

                    @Override
                    public void sendPing(ByteBuffer applicationData) throws IllegalArgumentException {

                    }

                    @Override
                    public void sendPong(ByteBuffer applicationData) throws IllegalArgumentException {

                    }
                };
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public void close() {

            }

            @Override
            public void close(CloseReason closeReason) {

            }

            @Override
            public URI getRequestURI() {
                return null;
            }

            @Override
            public Map<String, List<String>> getRequestParameterMap() {
                return null;
            }

            @Override
            public String getQueryString() {
                return null;
            }

            @Override
            public Map<String, String> getPathParameters() {
                return null;
            }

            @Override
            public Map<String, Object> getUserProperties() {
                return null;
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public Set<Session> getOpenSessions() {
                return null;
            }
        };
    }

    public EndpointConfig getEndpoint() {
        return new EndpointConfig() {
            @Override
            public List<Class<? extends Encoder>> getEncoders() {
                return null;
            }

            @Override
            public List<Class<? extends Decoder>> getDecoders() {
                return null;
            }

            @Override
            public Map<String, Object> getUserProperties() {
                return null;
            }
        };
    }

    public CloseReason getCloseReason() {
        return new CloseReason(new CloseReason.CloseCode() {
            /**
             * Returns the code number, for example the integer '1000' for normal closure.
             *
             * @return the code number
             */
            @Override
            public int getCode() {
                return 1006;
            }
        }, "no Connection");
    }
}
