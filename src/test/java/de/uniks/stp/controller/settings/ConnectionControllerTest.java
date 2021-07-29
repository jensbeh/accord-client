package de.uniks.stp.controller.settings;

import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.player.GetInformationAboutUsersCurrentPlaybackRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.Spotify.SpotifyConnection;
import de.uniks.stp.controller.settings.spotifyTest.TestUtil;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.JsonObject;

import static de.uniks.stp.controller.settings.spotifyTest.ITest.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConnectionControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @Mock
    private RestClient restClient;

    @Mock
    private HttpResponse<JsonNode> response;

    @Mock
    private HttpResponse<JsonNode> response2;

    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor2;


    @InjectMocks
    StageManager mockApp = new StageManager();

    public ConnectionControllerTest() throws Exception {
    }

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
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);
        SpotifyConnection spotifyConnection = new SpotifyConnection(mockApp.getBuilder());
        mockApp.getBuilder().getSpotifyConnection().setSpotifyApi(SPOTIFY_API);
        mockApp.getBuilder().getSpotifyConnection().setAuthorizationCodePKCERequest(authRequest);
        mockApp.getBuilder().getSpotifyConnection().setAuthorizationCodePKCERefreshRequest(authRefresh);
        mockApp.getBuilder().getSpotifyConnection().setGetInformationAboutUsersCurrentPlaybackRequest(getInformationAboutUsersCurrentPlaybackRequest);
        mockApp.getBuilder().getSpotifyConnection().setGetTrackRequest(getTrackRequest);
        mockApp.getBuilder().getSpotifyConnection().setGetAlbumRequest(getAlbumRequest);

        builder.setLoadUserData(false);

        app.start(stage);
        stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(de.uniks.stp.controller.settings.SettingsControllerTest.class);
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

    public void loginInit() throws InterruptedException {
        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        String testUserMainName = "Hendry Bracken";
        usernameTextField.setText(testUserMainName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        String testUserMainPw = "stp2021pw";
        passwordField.setText(testUserMainPw);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
    }


    private final AuthorizationCodePKCERequest authRequest = SPOTIFY_API.authorizationCodePKCE(AUTHORIZATION_CODE, CODE_VERIFIER)
            .setHttpManager(TestUtil.MockedHttpManager.returningJson("de/uniks/stp/spotifyJson/AuthCode.json")).build();

    private final AuthorizationCodePKCERefreshRequest authRefresh = SPOTIFY_API.authorizationCodePKCERefresh()
            .setHttpManager(TestUtil.MockedHttpManager.returningJson("de/uniks/stp/spotifyJson/AuthorizationCodeRefresh.json")).build();

    private final GetInformationAboutUsersCurrentPlaybackRequest getInformationAboutUsersCurrentPlaybackRequest = SPOTIFY_API
            .getInformationAboutUsersCurrentPlayback()
            .setHttpManager(
                    TestUtil.MockedHttpManager.returningJson(
                            "de/uniks/stp/spotifyJson/GetInformationAboutUsersCurrentPlaybackRequest.json"))
            .market(MARKET)
            .additionalTypes(ADDITIONAL_TYPES)
            .build();

    private final GetTrackRequest getTrackRequest = SPOTIFY_API
            .getTrack(ID_TRACK)
            .setHttpManager(
                    TestUtil.MockedHttpManager.returningJson(
                            "de/uniks/stp/spotifyJson/GetTrackRequest.json"))
            .market(MARKET)
            .build();

    private final GetAlbumRequest getAlbumRequest = SPOTIFY_API.getAlbum(ID_ALBUM)
            .setHttpManager(
                    TestUtil.MockedHttpManager.returningJson(
                            "de/uniks/stp/spotifyJson/GetAlbumRequest.json"))
            .market(MARKET)
            .build();


    @Test
    public void ToggleTest() throws InterruptedException {
        loginInit();
        mockApp.getBuilder().setSpotifyToken("test");
        mockApp.getBuilder().setSteamToken("test");
        clickOn("#settingsButton");
        clickOn("#button_Connection");
        clickOn("#spotifyToggleStackPane");
        clickOn("#steamToggleStackPane");
        clickOn("#spotifyToggleStackPane");
        clickOn("#steamToggleStackPane");
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().saveSettings();
    }

    @Test
    public void SpotifyPopUpTest() throws InterruptedException {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        mockApp.getBuilder().setSpotifyRefresh("b0KuPuLw77Z0hQhCsK-GTHoEx_kethtn357V7iqwEpCTIsLgqbBC_vQBTGC6M5rINl0FrqHK-D3cbOsMOlfyVKuQPvpyGcLcxAoLOTpYXc28nVwB7iBq2oKj9G9lHkFOUKn");
        mockApp.getBuilder().saveSettings();
        loginInit();
        mockApp.getBuilder().setSpotifyShow(true);
        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"Gustav\",\"description\":\"i.scdn.co\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
        
        message = "{\"action\":\"userDescriptionChanged\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"description\":\"#Against The Current - lullaby#https://i.scdn.co/image/ab67616d00004851186660bbf3b0dd9a5195e182\"}}";
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);
        clickOn("#settingsButton");
        clickOn("#button_Connection");
        clickOn("#spotify");
        WaitForAsyncUtils.waitForFxEvents();
        for (Window window : this.listTargetWindows()) {
            Stage s = (Stage) window;
            if (s.getTitle().equals("Spotify Login")) {
                WebView webview = (WebView) s.getScene().getRoot();
                System.out.println(webview.getEngine());
                Platform.runLater(() -> {
                    webview.getEngine().load("http://localhost:8888/callback/code=testCode");
                });
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        for (Window window : this.listTargetWindows()) {
            Stage s = (Stage) window;
            if (s.getTitle().equals("Accord - Settings")) {
                Platform.runLater(s::close);
                break;
            }
        }

        clickOn("#descriptionbox");
        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userList = lookup("#onlineUsers").query();
        User testUserOne = userList.getItems().get(0);
        clickOn(userList.lookup("#" + testUserOne.getId()));
        WaitForAsyncUtils.waitForFxEvents();

        mockApp.getBuilder().setSpotifyShow(false);
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().setSpotifyRefresh(null);
        mockApp.getBuilder().saveSettings();
    }


    @Test
    public void SteamProfilesLinkTest() throws InterruptedException {
        loginInit();
        String backupSteamToken = mockApp.getBuilder().getSteamToken();
        clickOn("#settingsButton");
        clickOn("#button_Connection");
        clickOn("#steam");
        WaitForAsyncUtils.waitForFxEvents();
        for (Window window : this.listTargetWindows()) {
            Stage s = (Stage) window;
            if (s.getTitle().equals("Steam Login")) {
                WebView webview = (WebView) s.getScene().getRoot();
                System.out.println(webview.getEngine());
                Platform.runLater(() -> {
                    webview.getEngine().load("https://steamcommunity.com/profiles/1234");
                });
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        Assert.assertEquals("1234", mockApp.getBuilder().getSteamToken());
        mockApp.getBuilder().setSteamToken(backupSteamToken);
        mockApp.getBuilder().setSteamShow(false);
        mockApp.getBuilder().saveSettings();
    }

    @Test
    public void SteamVanityLinkTest() throws InterruptedException {
        JSONObject jsonString = new JSONObject().put("response", new JSONObject().put("success", 1).put("steamid", "1234"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response2.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor2.getValue();
            callback.completed(response2);
            return null;
        }).when(restClient).resolveVanityID(anyString(), callbackCaptor2.capture());


        loginInit();
        String backupSteamToken = mockApp.getBuilder().getSteamToken();
        clickOn("#settingsButton");
        clickOn("#button_Connection");
        clickOn("#steam");
        WaitForAsyncUtils.waitForFxEvents();
        for (Window window : this.listTargetWindows()) {
            Stage s = (Stage) window;
            if (s.getTitle().equals("Steam Login")) {
                WebView webview = (WebView) s.getScene().getRoot();
                System.out.println(webview.getEngine());
                Platform.runLater(() -> {
                    webview.getEngine().load("https://steamcommunity.com/id/Hungriger_Hugo");
                });
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        Assert.assertEquals("1234", mockApp.getBuilder().getSteamToken());
        mockApp.getBuilder().setSteamToken(backupSteamToken);
        mockApp.getBuilder().setSteamShow(false);
        mockApp.getBuilder().saveSettings();
    }
}


