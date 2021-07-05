package de.uniks.stp.controller.settings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.*;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SettingsControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @Mock
    private RestClient restClient;

    @Mock
    private HttpResponse<JsonNode> response;


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

        builder.setLoadUserData(false);

        app.start(stage);
        stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(SettingsControllerTest.class);
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

    @Test
    public void changeThemeLogin() {
        VBox root = lookup("#root").query();
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button themeButton = lookup("#button_Theme").query();
        clickOn(themeButton);
        ComboBox<String> comboBox_themeSelect = lookup("#comboBox_themeSelect").query();

        clickOn(comboBox_themeSelect);
        clickOn("Bright");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("ffffff", root.getBackground().getFills().get(0).getFill().toString().substring(2,8));

        clickOn(comboBox_themeSelect);
        clickOn("Dark");
        Assert.assertEquals("36393f", root.getBackground().getFills().get(0).getFill().toString().substring(2,8));
    }

    @Test
    public void changeLanguageLogin() {
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button languageButton = lookup("#button_Language").query();
        clickOn(languageButton);
        Label label_langSelect = lookup("#label_langSelect").query();
        ComboBox<String> comboBox_langSelect = lookup("#comboBox_langSelect").query();

        clickOn(comboBox_langSelect);
        clickOn("Deutsch");
        Assert.assertEquals("Sprache", languageButton.getText());
        Assert.assertEquals("Sprache ausw\u00e4hlen:", label_langSelect.getText());

        clickOn(comboBox_langSelect);
        clickOn("English");
        Assert.assertEquals("Language", languageButton.getText());
        Assert.assertEquals("Select Language:", label_langSelect.getText());
    }

    @Test
    public void changeLanguageHomeScreen() throws InterruptedException {
        loginInit();

        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button languageButton = lookup("#button_Language").query();
        clickOn(languageButton);
        Label label_langSelect = lookup("#label_langSelect").query();
        ComboBox<String> comboBox_langSelect = lookup("#comboBox_langSelect").query();

        clickOn(comboBox_langSelect);
        clickOn("Deutsch");
        Assert.assertEquals("Sprache", languageButton.getText());
        Assert.assertEquals("Sprache ausw\u00e4hlen:", label_langSelect.getText());

        clickOn(comboBox_langSelect);
        clickOn("English");
        Assert.assertEquals("Language", languageButton.getText());
        Assert.assertEquals("Select Language:", label_langSelect.getText());

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
    }

    @Test
    public void doNotDisturbTest() throws InterruptedException {
        loginInit();
        clickOn("#settingsButton");
        clickOn("#button_DnD");

        CheckBox doNotDisturb = lookup("#doNotDisturbSelected").query();
        CheckBox showNotifications = lookup("#ShowNotifications").query();
        CheckBox playSound = lookup("#playSound").query();

        Assert.assertEquals(app.getBuilder().isDoNotDisturb(), doNotDisturb.isSelected());
        if (doNotDisturb.isSelected()) {
            clickOn(doNotDisturb);
        }
        clickOn(showNotifications);
        Assert.assertEquals(app.getBuilder().isShowNotifications(), showNotifications.isSelected());
        clickOn(playSound);
        Assert.assertEquals(app.getBuilder().isPlaySound(), playSound.isSelected());
        clickOn(doNotDisturb);
        Assert.assertTrue(showNotifications.isDisabled());
        Assert.assertTrue(playSound.isDisabled());
        clickOn(doNotDisturb);
        if (!showNotifications.isSelected()) {
            clickOn(showNotifications);
        }
        clickOn(doNotDisturb);

        //volume slider test
        Slider volume = lookup("#volume").query();
        volume.setValue(20.0);
        moveBy(65,-55);
        clickOn();

        clickOn("#settingsButton");
        clickOn("#button_DnD");
        Assert.assertEquals(20.0, volume.getValue(),0.001);
        volume.setValue(0.0);
    }

    @Test
    public void notificationTest() throws InterruptedException {
        loginInit();
        clickOn("#settingsButton");
        clickOn("#button_CN");

        ComboBox<String> customSoundComboBox = lookup("#comboBox").query();
        Button deleteButton = lookup("#delete").query();

        clickOn(customSoundComboBox);
        clickOn(customSoundComboBox.getItems().get(0));

        clickOn(deleteButton);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#settingsButton");
        clickOn("#button_DnD");
    }
}
