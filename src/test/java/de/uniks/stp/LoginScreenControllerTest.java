package de.uniks.stp;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class LoginScreenControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @Override
    public void start (Stage stage) {
        //start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Before
    public void setup () {
        MockitoAnnotations.openMocks(this);
    }

    @Test()
    public void logInTest () throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
        Platform.runLater(() -> Assert.assertEquals("Accord - Main", stage.getTitle()));
        WaitForAsyncUtils.waitForFxEvents();

        restMock.login("bla", "fasel", response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).login(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test()
    public void logInFailTest () throws InterruptedException {
        //wrong password
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("223");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Invalid credentials", errorLabel.getText());
        //wrong username
        usernameTextField.setText("peeter");
        passwordField.setText("123");
        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("Invalid credentials", errorLabel.getText());
        //both wrong
        usernameTextField.setText("peeter");
        passwordField.setText("1234");
        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("Invalid credentials", errorLabel.getText());


        restMock.login("bla", "fasel", response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).login(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void signInTest () throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Label errorLabel = lookup("#errorLabel").query();
        Thread.sleep(500);
        Assert.assertEquals("Name already taken", errorLabel.getText());

        restMock.signIn("bla", "fasel", response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).signIn(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void emptyFieldTest () {
        //usernameField and passwordField are both empty
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(()->passwordField.setText(""));
        Platform.runLater(()->usernameTextField.setText(""));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Field is empty!", errorLabel.getText());
        clickOn("#loginButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only usernameField is empty
        passwordField.setText("123");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());
        clickOn("#loginButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only passwordField is empty
        Platform.runLater(()->passwordField.setText(""));
        usernameTextField.setText("peter");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());
        clickOn("#loginButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());
    }

    @Test
    public void tempLoginTest() throws InterruptedException {
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(()->passwordField.setText(""));
        Platform.runLater(()->usernameTextField.setText(""));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("Accord - Main", stage.getTitle());

        restMock.loginTemp(response -> {});
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
        Platform.runLater(()->passwordField.setText(""));
        Platform.runLater(()->usernameTextField.setText(""));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(false);
        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);
        clickOn("#signinButton");
        Thread.sleep(500);
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Click on Login", errorLabel.getText());

        restMock.loginTemp(response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).loginTemp(callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void rememberMeNotTest() throws InterruptedException {
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(()->passwordField.setText("123"));
        Platform.runLater(()->usernameTextField.setText("peter"));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(false);
        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("Accord - Main", stage.getTitle());

        //Check if file with username and password is empty
        File f = new File("saves/user.txt");
        try {
            if(f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNext()) {
                    if (i == 0) {
                        String  firstLine = scanner.next();
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
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(()->passwordField.setText("123"));
        Platform.runLater(()->usernameTextField.setText("peter"));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("Accord - Main", stage.getTitle());

        //Check if file with username and password were saved
        File f = new File("saves/user.txt");
        try {
            if(f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNext()) {
                    if (i == 0) {
                        String  firstLine = scanner.next();
                        Assert.assertEquals("peter", firstLine);
                    }
                    if (i == 1) {
                        String secondLine = scanner.next();
                        Assert.assertEquals("123", secondLine);
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while reading!");
            e.printStackTrace();
        }
    }
}
