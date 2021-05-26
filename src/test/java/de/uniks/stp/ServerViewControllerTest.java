package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.*;
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
    private RestClient restClient;
    private static String testUserMainName;
    private static String testUserMainPw;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserOne_UserKey;
    private static String testServerId;

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
        this.restClient = new RestClient();
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

    public void loginInitWithTemp() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserMainName = body.getObject().getJSONObject("data").getString("name");
            testUserMainPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserMainName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserMainPw);

        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }

    public void loginInit(String name, String password) throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(name);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(password);

        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }

    public void getServerId() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            testUserOne_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        JsonNode body = restClient.postServer(testUserOne_UserKey, "TestServer Team Bit Shift");
        testServerId = body.getObject().getJSONObject("data").getString("id");

        restClient.logout(testUserOne_UserKey, response -> {
        });
    }

    @Test
    public void showServerTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        MenuButton serverNameText = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer Team Bit Shift", serverNameText.getText());

        clickOn("#logoutButton");
        Thread.sleep(2000);
    }

    @Test
    public void showServerUsersTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        app.getBuilder().buildServerUser("Test", "1234", false);
        app.getBuilder().buildServerUser("Test1", "12234", true);

        ScrollPane scrollPaneUserBox = lookup("#scrollPaneUserBox").query();
        ListView<User> onlineUserList = (ListView<User>) scrollPaneUserBox.lookup("#onlineUsers");
        ListView<User> offlineUserList = (ListView<User>) scrollPaneUserBox.lookup("#offlineUsers");
        app.getHomeViewController().getServerController().showOnlineOfflineUsers();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Assert.assertNotEquals(0, onlineUserList.getItems().size());
        Assert.assertNotEquals(0, offlineUserList.getItems().size());

        clickOn("#logoutButton");
        Thread.sleep(2000);
    }

    @Test
    public void logoutMultiLogin() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        restClient.login(testUserOneName, testUserOnePw, response -> {
        });

        Thread.sleep(2000);
        Assert.assertEquals("Accord - Login", stage.getTitle());

        restClient.logout(testUserOne_UserKey, response -> {
        });
    }


    @Test
    public void categoryViewTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#serverName_" + testServerId));
        Thread.sleep(2000);

        ListView channels = lookup("#channellist").queryListView();
        app.getBuilder().getCurrentServer().getCategories().get(0).withChannel(new Channel().setName("PARTEY"));
        Assert.assertEquals(app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().size(),channels.getItems().size());

        clickOn("#logoutButton");
        Thread.sleep(2000);
    }
}
