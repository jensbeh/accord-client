package de.uniks.stp;

import de.uniks.stp.net.RestClient;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit.ApplicationTest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServerViewControllerTest extends ApplicationTest {

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

    public void loginInit() {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Peter Lustig");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");

    }

    @Test
    public void showServerTest(){

    }

    @Test
    public void showServerUsersTest() throws InterruptedException{

    }
}
