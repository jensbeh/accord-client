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
        //Can be used if modified or deleted if not needed. Up to you
//        loginInit();
//        WaitForAsyncUtils.waitForFxEvents();
//        Thread.sleep(2000);
//
//        Circle addServer = lookup("#addServer").query();
//        clickOn(addServer);
//        TextField serverName = (TextField) lookup("#serverName").query();
//        serverName.setText("TestServer");
//        clickOn("#createServer");
//
//        restMock.postServer("c653b568-d987-4331-8d62-26ae617847bf", "TestServer");
//        JSONObject data = new JSONObject().accumulate("id", "5e2ffbd8770dd077d03df505").accumulate("name", "TestServer");
//        JSONObject jsonObj = new JSONObject().accumulate("status","success").accumulate("message", "password").append("data", data);
//        when(res.getBody()).thenReturn(new JsonNode(jsonObj.toString()));
//        verify(restMock).postServer(anyString(), anyString());
//
//        restMock.getServerUsers("5e2ffbd8770dd077d03df505", "c653b568-d987-4331-8d62-26ae617847bf", response -> {});
//
//        JSONArray categories = new JSONArray().put("5e2ffbdabg75dd078d03df600");
//        JSONObject member = new JSONObject().accumulate("id", "5e2ffbdabg75dd077d03df505").accumulate("name", "Spock").accumulate("online", true);
//        JSONArray members = new JSONArray().put(member);
//        JSONObject data2 = new JSONObject().accumulate("id", "5e2ffbd8770dd077d03df505").accumulate("name", "TestServer").accumulate("categories", categories).accumulate("members", members);
//        JSONObject jsonObj2 = new JSONObject().accumulate("status","success").accumulate("message", "password").accumulate("data", data2);
//        when(res.getBody()).thenReturn(new JsonNode(jsonObj2.toString()));
//        verify(restMock).getServerUsers(anyString(), anyString(), callbackCaptor.capture());
//        Callback<JsonNode> callback = callbackCaptor.getValue();
//        callback.completed(res);
//
//        Assert.assertEquals(jsonObj2.toString(), res.getBody().toString());
    }
}
