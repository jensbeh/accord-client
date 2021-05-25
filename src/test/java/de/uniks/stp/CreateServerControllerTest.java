package de.uniks.stp;

import de.uniks.stp.net.RestClient;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateServerControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private String testUserName;
    private String testUserPw;

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

    public void loginInit() throws InterruptedException {
        RestClient restClient = new RestClient();
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
        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }

    @Test
    public void createServerTest() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);
        Label errorLabel = lookup("#errorLabel").query();
        clickOn("#createServer");
        Assert.assertEquals("Error: Server name cannot be empty", errorLabel.getText());
        TextField serverName = lookup("#serverName").query();
        serverName.setText("TestServer Team Bit Shift");
        Assert.assertEquals("TestServer Team Bit Shift", serverName.getText());

        restMock.postServer("c653b568-d987-4331-8d62-26ae617847bf", "TestServer Team Bit Shift");
        JSONObject data = new JSONObject().accumulate("id", "5e2ffbd8770dd077d03df505").accumulate("name", "TestServer Team Bit Shift");
        JSONObject jsonObj = new JSONObject().accumulate("status", "success").accumulate("message", "password").append("data", data);
        when(res.getBody()).thenReturn(new JsonNode(jsonObj.toString()));
        verify(restMock).postServer(anyString(), anyString());
        Assert.assertEquals(jsonObj.toString(), res.getBody().toString());

        clickOn("#logoutButton");
    }

    @Test
    public void emptyTextField() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);
        Label errorLabel = lookup("#errorLabel").query();
        clickOn("#createServer");
        Assert.assertEquals("Error: Server name cannot be empty", errorLabel.getText());

        clickOn("#logoutButton");
    }

    @Test
    public void showCreateServerTest() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        MenuButton serverNameText = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer Team Bit Shift", serverNameText.getText());

        clickOn("#logoutButton");
        Thread.sleep(2000);
    }

    @Test
    public void showNoConnectionToServerTest() throws InterruptedException {
        String message = "";
        when(restMock.postServer(anyString(), anyString())).thenThrow(new UnirestException("No route to host: connect"));
        try {
            restMock.postServer("c653b568-d987-4331-8d62-26ae617847bf", "TestServer");
        } catch (Exception e) {
            if (e.getMessage().equals("No route to host: connect")) {
                message = "No Connection - Please check your connection and try again";
            }
        }
        Assert.assertEquals("No Connection - Please check your connection and try again", message);
    }

}
