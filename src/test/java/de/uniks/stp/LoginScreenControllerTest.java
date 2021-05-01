package de.uniks.stp;

import de.uniks.stp.net.RestClient;
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
import static org.mockito.Mockito.*;


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
    public void signInTest () {
        //TODO: Here TestFX
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("null", errorLabel.getText());

        /*restMock.signIn("bla", "fasel", response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).signIn(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());*/
    }

    @Test
    public void logInTest () {
        //TODO: Here TestFX
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        Label errorLabel = lookup("#errorLabel").query();
        //Assert.assertEquals("null", errorLabel.getText());

        /*restMock.login("bla", "fasel", response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).login(anyString(), anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());*/
    }

    @Test
    public void emptyFieldTest () {
        //usernameField and passwordField are both empty
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("Field is empty!", errorLabel.getText());
        clickOn("#loginButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only usernameField is empty
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());
        clickOn("#loginButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());

        //only passwordField is empty
        passwordField.setText("");
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        rememberBox.setSelected(true);
        clickOn("#signinButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());
        clickOn("#loginButton");
        Assert.assertEquals("Field is empty!", errorLabel.getText());
    }
}
