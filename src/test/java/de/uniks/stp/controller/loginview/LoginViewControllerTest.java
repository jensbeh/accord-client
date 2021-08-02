package de.uniks.stp.controller.loginview;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.login.LoginViewController;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import de.uniks.stp.util.Constants;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
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

import java.io.File;
import java.util.Base64;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class LoginViewControllerTest extends ApplicationTest {

    // main user
    private final String testUserName = "Hendry Bracken";
    private final String testUserPw = "stp2021pw";
    private final String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
    @InjectMocks
    StageManager mockApp = new StageManager();
    private Stage stage;
    private StageManager app;
    // optional user
    private String testUserOneName;
    private String testUserOnePw;
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
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor2;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor3;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor4;
    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(LoginViewController.class);
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
        mockApp.getBuilder().setSpotifyShow(false);
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().setSpotifyRefresh(null);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    public void mockLogin() {
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

    public void mockTempLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("name", "Test User").put("password", "testPassword"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response2.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor2.getValue();
            callback.completed(response2);
            return null;
        }).when(restClient).loginTemp(callbackCaptor2.capture());
    }

    public void mockLoginFailure() {
        JSONObject jsonString = new JSONObject()
                .put("status", "failure")
                .put("message", "Invalid credentials")
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response3.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor3.getValue();
            callback.completed(response3);
            return null;
        }).when(restClient).login(anyString(), anyString(), callbackCaptor3.capture());
    }

    public void mockSignInFailure() {
        JSONObject jsonString = new JSONObject()
                .put("status", "failure")
                .put("message", "Name already taken")
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response4.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor4.getValue();
            callback.completed(response4);
            return null;
        }).when(restClient).signIn(anyString(), anyString(), callbackCaptor4.capture());
    }

    public void loginInit(boolean rememberMe) {
        mockLogin();

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserPw);
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(rememberMe);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void clearCheckBoxes() {
        CheckBox tempUser = lookup("#loginAsTempUser").query();
        tempUser.setSelected(false);
    }

    @Test()
    public void logInTest() throws InterruptedException {
        mockLogin();

        Assert.assertEquals("success", response.getBody().getObject().getString("status"));
        Assert.assertEquals("", response.getBody().getObject().getString("message"));
        Assert.assertEquals(userKey, response.getBody().getObject().getJSONObject("data").getString("userKey"));

        // Actual Test
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserPw);
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Accord - Main", stage.getTitle());
    }

    @Test()
    public void logInFailTest() {
        mockTempLogin();
        mockLoginFailure();

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });

        //wrong password
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserOneName);
        String wrongUsername = "abc" + testUserOnePw;
        PasswordField passwordField = lookup("#passwordTextField").query();
        String wrongPassword = testUserOnePw + "abc";
        passwordField.setText(wrongPassword);
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();


        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Invalid credentials", errorLabel.getText());

        //wrong username
        usernameTextField.setText(wrongUsername);
        passwordField.setText(testUserOnePw);

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Invalid credentials", errorLabel.getText());

        //both wrong
        usernameTextField.setText(wrongUsername);
        passwordField.setText(wrongPassword);

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Invalid credentials", errorLabel.getText());
    }

    @Test
    public void signInTest() {
        mockTempLogin();
        mockSignInFailure();

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserOneName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserOnePw);

        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);

        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();

        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Name already taken", errorLabel.getText());
    }

    @Test
    public void emptyFieldTest() {
        //usernameField and passwordField are both empty
        Label errorLabel = lookup("#errorLabel").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        PasswordField passwordField = lookup("#passwordTextField").query();
        Platform.runLater(() -> {
            usernameTextField.setText("");
            passwordField.setText("");
        });

        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);

        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();

        //Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        //Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only usernameField is empty
        passwordField.setText("123");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();
        //Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        //Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only passwordField is empty
        usernameTextField.setText("peter");
        passwordField.setText("");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();
        //Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        //Assert.assertEquals("Field is empty!", errorLabel.getText());
    }

    @Test
    public void tempLoginTest() {
        mockLogin();
        mockTempLogin();

        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(() -> {
            usernameTextField.setText("");
            passwordField.setText("");
        });
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);

        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Accord - Main", stage.getTitle());
    }

    @Test
    public void tempSignInTest() {
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(() -> {
            usernameTextField.setText("");
            passwordField.setText("");
        });
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(false);
        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);

        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();

        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Click on Login", errorLabel.getText());
    }

    @Test
    public void rememberMeNotTest() {
        loginInit(false);

        Assert.assertEquals("Accord - Main", stage.getTitle());
        WaitForAsyncUtils.waitForFxEvents();

        //Check if file with username and password is empty
        AppDirs appDirs = AppDirsFactory.getInstance();
        Constants.APPDIR_ACCORD_PATH = appDirs.getUserConfigDir("Accord", null, null);

        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;
        File f = new File(path_to_config + Constants.USERDATA_FILE);
        try {
            if (f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNext()) {
                    if (i == 0) {
                        String firstLine = scanner.nextLine();
                        Assert.assertEquals("", firstLine);
                    }
                    if (i == 1) {
                        String secondLine = scanner.nextLine();
                        Assert.assertEquals("", secondLine);
                    }
                    if (i == 2) {
                        Assert.assertFalse(Boolean.parseBoolean(scanner.nextLine()));
                    }
                    if (i == 3) {
                        Assert.assertFalse(Boolean.parseBoolean(scanner.nextLine()));
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while reading!");
            e.printStackTrace();
        }
    }

    @Test
    public void rememberMeTest() {
        clearCheckBoxes();
        loginInit(true);

        Assert.assertEquals("Accord - Main", stage.getTitle());
        WaitForAsyncUtils.waitForFxEvents();

        //Check if file with username and password were saved
        AppDirs appDirs = AppDirsFactory.getInstance();
        Constants.APPDIR_ACCORD_PATH = appDirs.getUserConfigDir("Accord", null, null);

        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;
        File f = new File(path_to_config + Constants.USERDATA_FILE);
        try {
            if (f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNextLine()) {
                    if (i == 0) {
                        String firstLine = scanner.nextLine();
                        Assert.assertEquals(testUserName, firstLine);
                    }
                    if (i == 1) {
                        String secondLine = scanner.nextLine();
                        Assert.assertEquals(testUserPw, decode(secondLine));
                    }
                    if (i == 2) {
                        Assert.assertTrue(Boolean.parseBoolean(scanner.nextLine()));
                    }
                    if (i == 3) {
                        Assert.assertFalse(Boolean.parseBoolean(scanner.nextLine()));
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while reading!");
            e.printStackTrace();
        }
    }

    //@Test
    public void noConnectionTest() {
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(() -> passwordField.setText("123"));
        Platform.runLater(() -> usernameTextField.setText("peter"));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);


        app.getLoginViewController().setNoConnectionTest(true);
        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();
        Label noConnectionTest = lookup("#connectionLabel").query();
        Assert.assertEquals("No connection - \nPlease check your connection and try again", noConnectionTest.getText());

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("No connection - \nPlease check your connection and try again", noConnectionTest.getText());

        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("No connection - \nPlease check your connection and try again", noConnectionTest.getText());
        app.getLoginViewController().setNoConnectionTest(false);
    }

    /**
     * decode password
     */
    public String decode(String str) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(str);
        return new String(bytes);
    }
}
