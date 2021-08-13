package de.uniks.stp.net.websockets;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.serverview.ServerMessageTest;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.glassfish.json.JsonUtil;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WebSocketTest extends ApplicationTest {
    private final String testServerName = "TestServer Team Bit Shift";
    private final String testServerId = "5e2fbd8770dd077d03df505";
    private final String testUserMainName = "Hendry Bracken";
    private final String testUserMainPw = "stp2021pw";
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
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor3;

    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
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

    public Session getSession() {
        Session session = new Session() {
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
                    public void sendText(String text) throws IOException {

                    }

                    @Override
                    public void sendBinary(ByteBuffer data) throws IOException {

                    }

                    @Override
                    public void sendText(String partialMessage, boolean isLast) throws IOException {

                    }

                    @Override
                    public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {

                    }

                    @Override
                    public OutputStream getSendStream() throws IOException {
                        return null;
                    }

                    @Override
                    public Writer getSendWriter() throws IOException {
                        return null;
                    }

                    @Override
                    public void sendObject(Object data) throws IOException, EncodeException {

                    }

                    @Override
                    public void setBatchingAllowed(boolean allowed) throws IOException {

                    }

                    @Override
                    public boolean getBatchingAllowed() {
                        return false;
                    }

                    @Override
                    public void flushBatch() throws IOException {

                    }

                    @Override
                    public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {

                    }

                    @Override
                    public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {

                    }
                };
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public void close() throws IOException {

            }

            @Override
            public void close(CloseReason closeReason) throws IOException {

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
        return session;
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

    @Test
    public void testPrivateWebSocketTest() throws InterruptedException, IOException {
        doCallRealMethod().when(privateChatWebSocket).stop();
        doCallRealMethod().when(privateChatWebSocket).startNoopTimer();
        doCallRealMethod().when(privateChatWebSocket).sendMessage(any());
        doCallRealMethod().when(privateChatWebSocket).showNoConAlert();
        doCallRealMethod().when(privateChatWebSocket).onOpen(any(), any());
        doCallRealMethod().when(privateChatWebSocket).handleMessage(any()); // TODO isblocked aufrufen //fehlt noch
        doCallRealMethod().when(privateChatWebSocket).getPrivateViewController();
        Assert.assertNull(privateChatWebSocket.getPrivateViewController());
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
    public void testServerSystemWebSocketTest() throws InterruptedException, IOException {
        doCallRealMethod().when(serverSystemWebSocket).onOpen(any(),any()); //TODO auch noch onMessage
        doCallRealMethod().when(serverSystemWebSocket).onClose(any(),any());
        doCallRealMethod().when(serverSystemWebSocket).startNoopTimer();
        doCallRealMethod().when(serverSystemWebSocket).stop();
        serverSystemWebSocket.startNoopTimer();
        serverSystemWebSocket.onOpen(getSession(), getEndpoint());
        WaitForAsyncUtils.waitForFxEvents();
        serverSystemWebSocket.onClose(getSession(), getCloseReason());
    }

    @Test
    public void testServerSystemWebSocketStopTest() throws IOException {
        doCallRealMethod().when(serverSystemWebSocket).onOpen(any(),any());
        doCallRealMethod().when(serverSystemWebSocket).stop();
        doCallRealMethod().when(serverSystemWebSocket).startNoopTimer();

        serverSystemWebSocket.startNoopTimer();
        serverSystemWebSocket.onOpen(getSession(), getEndpoint());
        serverSystemWebSocket.stop();
    }

    @Test
    public void testServerChatWebSocketTest() throws InterruptedException, IOException {
        doCallRealMethod().when(serverChatWebSocket).setBuilder(any());
        doCallRealMethod().when(serverChatWebSocket).getBuilder();
        doCallRealMethod().when(serverChatWebSocket).handleMessage(any());
        doCallRealMethod().when(serverChatWebSocket).sendMessage(any());
        doCallRealMethod().when(serverChatWebSocket).onOpen(any(),any()); //TODO auch noch onMessage
        doCallRealMethod().when(serverChatWebSocket).onClose(any(),any());
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
        doCallRealMethod().when(serverChatWebSocket).onOpen(any(),any());
        doCallRealMethod().when(serverChatWebSocket).stop();
        doCallRealMethod().when(serverChatWebSocket).startNoopTimer();

        serverChatWebSocket.startNoopTimer();
        serverChatWebSocket.onOpen(getSession(), getEndpoint());
        serverChatWebSocket.stop();
    }

    @Test
    public void testPrivateSystemWebSocketTest() throws InterruptedException,IOException {
        doCallRealMethod().when(privateSystemWebSocketClient).onOpen(any(),any()); //TODO zusätzlich onMessage
        doCallRealMethod().when(privateSystemWebSocketClient).onClose(any(),any());
        doCallRealMethod().when(privateSystemWebSocketClient).sendMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).onOpen(any(),any());
        doCallRealMethod().when(privateSystemWebSocketClient).stop();
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any()); //TODO updateServerUsers durch updateUserDescription
        doCallRealMethod().when(privateSystemWebSocketClient).startNoopTimer();

        privateSystemWebSocketClient.startNoopTimer();
        privateSystemWebSocketClient.onOpen(getSession(), getEndpoint());
        WaitForAsyncUtils.waitForFxEvents();
        privateSystemWebSocketClient.onClose(getSession(), getCloseReason());
    }

    @Test
    public void testPrivateSystemWebSocketStopTest() throws IOException {
        doCallRealMethod().when(privateSystemWebSocketClient).onOpen(any(),any());
        doCallRealMethod().when(privateSystemWebSocketClient).stop();
        doCallRealMethod().when(privateSystemWebSocketClient).startNoopTimer();

        privateSystemWebSocketClient.startNoopTimer();
        privateSystemWebSocketClient.onOpen(getSession(), getEndpoint());
        privateSystemWebSocketClient.stop();
    }
}
