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
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.atomic.AtomicReference;

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

    @BeforeEach
    public void setup () {
        MockitoAnnotations.openMocks(this);
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
        Thread.sleep(1000);
        Assert.assertEquals("Name already taken", errorLabel.getText());
    }

    @Test
    public void logInTest () {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("success", errorLabel.getText());

        /*restMock.signIn("bla", "fasel", response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).login(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());*/

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
    public void tempLoginTest() {
        PasswordField passwordField = lookup("#passwordTextField").query();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        Platform.runLater(()->passwordField.setText(""));
        Platform.runLater(()->usernameTextField.setText(""));
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);
        clickOn("#signinButton");
        clickOn("#loginButton");
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("success", errorLabel.getText());
    }
}
