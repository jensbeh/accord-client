package de.uniks.stp;

import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
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

public class ServerViewControllerTest extends ApplicationTest {

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
        clickOn("#loginButton");

    }

    @Test
    public void showServerTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#server"));
        Label serverNameText = lookup("#serverName").query();
        Assert.assertEquals(serverNameText.getText(), serverList.getItems().get(0).getName());
    }

    @Test
    public void showServerUsersTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#server"));
        app.getBuilder().buildServerUser("Test", "1234", false);
        app.getBuilder().buildServerUser("Test1", "12234", true);
        ListView<User> onlineUserList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        ListView<User> offlineUserList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        Assert.assertNotEquals(0, onlineUserList.getItems().size());
        Assert.assertNotEquals(0, offlineUserList.getItems().size());
    }

    @Test
    public void logoutMultiLogin() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        RestClient restClient = new RestClient();
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#server"));
        String testUserOneName = "TestUser Team Bit Shift";
        restClient.login(testUserOneName, "test123", response -> {
        });
        Thread.sleep(1000);
        Assert.assertEquals("Accord - Login", stage.getTitle());
    }
}
