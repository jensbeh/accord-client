package de.uniks.stp;

import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class HomeViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private String userKey;
    private String testUserName;
    private String testUserPw;
    private String testUserOneName;
    private String testUserOnePw;
    private String testUserKeyOne;
    private String testUserTwoName;
    private String testUserTwoPw;
    private String testUserKeyTwo;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
        restClient = new RestClient();
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

    //@Test
    public void personalUserTest() throws InterruptedException {
        loginInit();

        Label personalUserName = lookup("#currentUserBox").lookup("#userName").query();

        Assert.assertEquals(testUserName, personalUserName.getText());


    }

    //@Test
    public void serverBoxTest() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverNameInput = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverNameInput.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();

        ObservableList<Server> itemList = serverListView.getItems();
        String serverName = "";
        for (Server server : itemList) {
            if (server.getName().equals("TestServer Team Bit Shift")) {
                serverName = "TestServer Team Bit Shift";
                break;
            }
        }
        Assert.assertEquals("TestServer Team Bit Shift", serverName);


    }

    //@Test
    public void userBoxTest() throws InterruptedException {
        loginInit();

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        restClient.login(testUserOneName, testUserOnePw, response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        ObservableList<User> itemList = userList.getItems();
        String userName = "";
        for (User user : itemList) {
            if (user.getName().equals(testUserOneName)) {
                userName = user.getName();
                break;
            }
        }
        Assert.assertEquals(testUserOneName, userName);

        restClient.logout(userKey, response -> {
        });
        Thread.sleep(2000);

        itemList = userList.getItems();
        userName = "";
        for (User user : itemList) {
            if (user.getName().equals(testUserOneName)) {
                userName = user.getName();
                break;
            }
        }
        Assert.assertEquals("", userName);


    }

    //@Test
    public void getServersTest() {
        restMock.getServers("bla", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).getServers(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    //@Test
    public void getUsersTest() {
        restMock.getUsers("bla", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).getUsers(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    //@Test
    public void privateChatTest() throws InterruptedException {
        loginInit();

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        restClient.login(testUserOneName, testUserOnePw, response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        User testUserOne = userList.getItems().get(0);
        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        Thread.sleep(500);
        ListView<Channel> privateChatList = lookup("#privateChatList").query();
        Assert.assertEquals(testUserOne.getName(), privateChatList.getItems().get(0).getName());

        restClient.logout(userKey, response -> {
        });


    }

    //@Test()
    public void logout() throws InterruptedException {
        loginInit();

        Assert.assertEquals("Accord - Main", stage.getTitle());


        // Clicking logout...
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        // TODO: enable Assert when logout click is back
        //Assert.assertEquals("Accord - Login", stage.getTitle());

        restMock.logout("c653b568-d987-4331-8d62-26ae617847bf", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).logout(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    //@Test
    public void logoutMultiLogin() throws InterruptedException {
        loginInit();

        restClient.login(testUserName, testUserPw, response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Assert.assertEquals("Accord - Login", stage.getTitle());
        restClient.logout(userKey, response -> {
        });
    }

    //@Test
    public void openExistingChat() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            this.testUserKeyOne = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserTwoName = body.getObject().getJSONObject("data").getString("name");
            testUserTwoPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

        restClient.login(testUserTwoName, testUserTwoPw, response -> {
            JsonNode body = response.getBody();
            this.testUserKeyTwo = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        loginInit();

        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();

        Thread.sleep(500);

        // Use the first two Users in Online-User-List as test Users
        User testUserOne = userList.getItems().get(0);
        User testUserTwo = userList.getItems().get(1);

        doubleClickOn(userList.lookup("#" + testUserOne.getId()));
        Thread.sleep(2000);

        doubleClickOn(userList.lookup("#" + testUserTwo.getId()));
        Thread.sleep(2000);

        Assert.assertEquals(testUserTwo.getName(), PrivateViewController.getSelectedChat().getName());

        ListView<Channel> privateChatList = lookup("#privateChatList").query();
        clickOn(privateChatList.lookup("#" + testUserOne.getId()));
        Thread.sleep(2000);

        Assert.assertEquals(testUserOne.getName(), PrivateViewController.getSelectedChat().getName());

        //Additional test if opened private chat is colored
        VBox privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        Assert.assertEquals("-fx-background-color: #666666; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183", privateChatCell.getStyle());

        //Additional test when homeButton is clicked and opened chat is the same
        //Clicking homeButton will load the view - same like clicking on server and back to home
        clickOn("#homeButton");
        Thread.sleep(2000);
        privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        Assert.assertEquals("-fx-background-color: #666666; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183", privateChatCell.getStyle());

        restClient.logout(testUserKeyOne, response -> {
        });

        restClient.logout(testUserKeyTwo, response -> {
        });


    }
}