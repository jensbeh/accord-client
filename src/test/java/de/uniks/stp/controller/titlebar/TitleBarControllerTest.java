package de.uniks.stp.controller.titlebar;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.home.HomeViewController;
import de.uniks.stp.controller.settings.Spotify.SpotifyConnection;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)           // TODO important
public class TitleBarControllerTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;

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
    private SpotifyConnection spotifyConnection;


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
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        builder.setLoadUserData(false);
        mockApp.getBuilder().setSpotifyShow(false);
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().setSpotifyRefresh(null);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(HomeViewController.class);
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
        doCallRealMethod().when(spotifyConnection).init(any());
        doCallRealMethod().when(spotifyConnection).stopDescriptionScheduler();
        doCallRealMethod().when(spotifyConnection).updateUserDescriptionScheduler();
        doCallRealMethod().when(spotifyConnection).personalUserListener(any(), any(), any(), any(), any());
        doCallRealMethod().when(spotifyConnection).showSpotifyPopupView(any(), any(), any());
        doCallRealMethod().when(spotifyConnection).updateValuesUser(any());
        when(spotifyConnection.getCurrentlyPlayingSongAlbumID()).thenReturn("resources/de/uniks/stp/icons/Spotify_Icon_RGB_Green.png");

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
    }

    @Test
    public void titleBarButtonTest() throws InterruptedException {
        loginInit();

        HBox logoAndLabelBox = lookup("#titleLogoAndLabel").query();
        HBox titleBarSpaceBox = lookup("#titleBarSpace").query();
        Button minButton = lookup("#Button_minTitleBar").query();
        Button maxButton = lookup("#Button_maxTitleBar").query();
        Button closeButton = lookup("#Button_closeTitleBar").query();

        Stage stage = (Stage) (minButton.getScene().getWindow());

        // move stage
        drag(titleBarSpaceBox).dropTo(logoAndLabelBox);
        WaitForAsyncUtils.waitForFxEvents();

        // click min
        clickOn(minButton);
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> stage.setIconified(false));
        WaitForAsyncUtils.waitForFxEvents();

        // make stage a bit bigger
        drag(stage.getX() + stage.getWidth() - 1, stage.getY() + stage.getHeight() - 1).dropTo(stage.getX() + stage.getWidth() + 20, stage.getY() + stage.getHeight() + 20);

        // click max -> fullscreen
        clickOn(maxButton);
        WaitForAsyncUtils.waitForFxEvents();

        // click max -> normal
        clickOn(maxButton);
        WaitForAsyncUtils.waitForFxEvents();

        // click close
        clickOn(closeButton);
        WaitForAsyncUtils.waitForFxEvents();
    }
}
