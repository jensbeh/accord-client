package de.uniks.stp;

import de.uniks.stp.net.RestClient;
import javafx.scene.control.CheckBox;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HomeViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @Override
    public void start(Stage stage) {
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
    public void logout() throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(1000);
        clickOn("#logoutButton");

        restMock.logout("c653b568-d987-4331-8d62-26ae617847bf", response -> {});
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).logout(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }
}
