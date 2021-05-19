package de.uniks.stp;

import de.uniks.stp.net.RestClient;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.json.JSONObject;
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

import java.net.NoRouteToHostException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateServerControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
    }

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
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    public void loginInit() {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("TestUser Team Bit Shift");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("test123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
    }

    @Test
    public void createServerTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);
        Label errorLabel = lookup("#errorLabel").query();
        clickOn("#createServer");
        Assert.assertEquals("Error: Server name cannot be empty", errorLabel.getText());
        TextField serverName = (TextField) lookup("#serverName").query();
        serverName.setText("TestServer");
        Assert.assertEquals("TestServer", serverName.getText());

        restMock.postServer("c653b568-d987-4331-8d62-26ae617847bf", "TestServer");
        JSONObject data = new JSONObject().accumulate("id", "5e2ffbd8770dd077d03df505").accumulate("name", "TestServer");
        JSONObject jsonObj = new JSONObject().accumulate("status", "success").accumulate("message", "password").append("data", data);
        when(res.getBody()).thenReturn(new JsonNode(jsonObj.toString()));
        verify(restMock).postServer(anyString(), anyString());
        Assert.assertEquals(jsonObj.toString(), res.getBody().toString());

    }

    @Test
    public void showServerTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Circle addServer = lookup("#addServer").query();
        restMock.postServer("c653b568-d987-4331-8d62-26ae617847bf", "TestServer");
        when(restMock).thenThrow(new NoRouteToHostException());
        clickOn(addServer);
        Label errorLabel = lookup("#errorLabel").query();
        Assert.assertEquals("No internet Connection - Please check your connection and try again", errorLabel.getText());
    }

    @Test
    public void showNoConnectionToServerTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        app.getBuilder().setCurrentServer(app.getBuilder().getServers().get(app.getBuilder().getServers().size() - 1));
        app.getHomeViewController().onServerCreated();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Label serverNameText = lookup("#serverName").query();
        Assert.assertEquals("TestServer", serverNameText.getText());
    }

}
