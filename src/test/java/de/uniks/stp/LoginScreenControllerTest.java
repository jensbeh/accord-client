package de.uniks.stp;

import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.net.RestClient;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import util.Constants;

import java.io.File;
import java.util.Base64;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class LoginScreenControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    // main user
    private final String testUserName = "Hendry Bracken";
    private final String testUserPw = "stp2021pw";
    private final String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
    // optional user
    private String testUserOneName;
    private String testUserOnePw;

    @Mock
    private RestClient restClient;

    @Mock
    private HttpResponse<JsonNode> response;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        this.stage = stage;
        app = mockApp;
        app.setRestClient(restClient);
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @InjectMocks
    StageManager mockApp = new StageManager();

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(LoginScreenController.class);
    }

    public void mockLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("userKey", userKey));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockTempLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("name", "Test User").put("password", "testPassword"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).loginTemp(callbackCaptor.capture());
    }

    public void mockLoginFailure() {
        JSONObject jsonString = new JSONObject()
                .put("status", "failure")
                .put("message", "Invalid credentials")
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockSignInFailure() {
        JSONObject jsonString = new JSONObject()
                .put("status", "failure")
                .put("message", "Name already taken")
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).signIn(anyString(), anyString(), callbackCaptor.capture());
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
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });

        mockLoginFailure();
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
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });

        mockSignInFailure();
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

        Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only usernameField is empty
        passwordField.setText("123");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only passwordField is empty
        usernameTextField.setText("peter");
        passwordField.setText("");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Field is empty!", errorLabel.getText());
    }

    @Test
    public void tempLoginTest() {
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

        /*TODO: this is needed because two rest request will be done, but only the first will success,
        *  the second one needs to be done after the first one*/
        /*TODO: if two "when(response.getBody())" are open, the last one is valid*/
        mockLogin();
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
                        String firstLine = scanner.next();
                        Assert.assertEquals("", firstLine);
                    }
                    if (i == 1) {
                        String secondLine = scanner.next();
                        Assert.assertEquals("", secondLine);
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
                    i++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while reading!");
            e.printStackTrace();
        }
    }

    @Test
    public void noConnectionTest() {
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(() -> passwordField.setText("123"));
        Platform.runLater(() -> usernameTextField.setText("peter"));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);

        LoginScreenController.noConnectionTest = true;
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
        LoginScreenController.noConnectionTest = false;
    }

    /**
     * decode password
     */
    public static String decode(String str) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(str);
        return new String(bytes);
    }
}
