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
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import util.Constants;

import java.io.File;
import java.util.Base64;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class LoginScreenControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private String testUserName;
    private String testUserPw;
    private String testUserOneName;
    private String testUserOnePw;

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
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
        restClient = new RestClient();
    }

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    public void loginInit(boolean rememberMe) throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserName = body.getObject().getJSONObject("data").getString("name");
            testUserPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserPw);
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(rememberMe);

        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }

    @Test()
    public void logInTest() throws InterruptedException {
        loginInit(true);

        Assert.assertEquals("Accord - Main", stage.getTitle());

        clickOn("#logoutButton");
        Thread.sleep(2000);

        restMock.login("bla", "fasel", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).login(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test()
    public void logInFailTest() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

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
        Thread.sleep(500);

        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Invalid credentials", errorLabel.getText());

        //wrong username
        usernameTextField.setText(wrongUsername);
        passwordField.setText(testUserOnePw);

        clickOn("#loginButton");
        Thread.sleep(500);

        Assert.assertEquals("Invalid credentials", errorLabel.getText());

        //both wrong
        usernameTextField.setText(wrongUsername);
        passwordField.setText(wrongPassword);

        clickOn("#loginButton");
        Thread.sleep(500);

        Assert.assertEquals("Invalid credentials", errorLabel.getText());


        restMock.login("bla", "fasel", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).login(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void signInTest() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserOneName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserOnePw);

        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);

        clickOn("#signinButton");
        Thread.sleep(2000);

        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Name already taken", errorLabel.getText());

        restMock.signIn("bla", "fasel", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).signIn(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void emptyFieldTest() throws InterruptedException {
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
        Thread.sleep(2000);

        Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        Thread.sleep(2000);

        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only usernameField is empty
        passwordField.setText("123");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Thread.sleep(2000);
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        Thread.sleep(2000);
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only passwordField is empty
        usernameTextField.setText("peter");
        passwordField.setText("");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Thread.sleep(2000);
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        clickOn("#loginButton");
        Thread.sleep(2000);

        Assert.assertEquals("Field is empty!", errorLabel.getText());
    }

    @Test
    public void tempLoginTest() throws InterruptedException {
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
        Thread.sleep(2000);

        Assert.assertEquals("Accord - Main", stage.getTitle());

        clickOn("#logoutButton");
        Thread.sleep(2000);

        restMock.loginTemp(response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).loginTemp(callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void tempSignInTest() throws InterruptedException {
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
        Thread.sleep(2000);

        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Click on Login", errorLabel.getText());

        restMock.loginTemp(response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).loginTemp(callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void rememberMeNotTest() throws InterruptedException {
        loginInit(false);

        Assert.assertEquals("Accord - Main", stage.getTitle());

        clickOn("#logoutButton");
        Thread.sleep(2000);

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
    public void rememberMeTest() throws InterruptedException {
        loginInit(true);

        Assert.assertEquals("Accord - Main", stage.getTitle());

        clickOn("#logoutButton");
        Thread.sleep(2000);

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
    public void noConnectionTest() throws InterruptedException {
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(() -> passwordField.setText("123"));
        Platform.runLater(() -> usernameTextField.setText("peter"));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);

        LoginScreenController.noConnectionTest = true;
        clickOn("#signinButton");
        Thread.sleep(500);
        Label noConnectionTest = lookup("#connectionLabel").query();
        Assert.assertEquals("No connection - \nPlease check your connection and try again", noConnectionTest.getText());

        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("No connection - \nPlease check your connection and try again", noConnectionTest.getText());

        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
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
