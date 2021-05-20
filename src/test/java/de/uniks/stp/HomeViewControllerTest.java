package de.uniks.stp;

import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    private String msg;

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

    public void login() {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Peter Lustig");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        clickOn("#loginButton");
    }

    @Test
    public void personalUserTest() throws InterruptedException {
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Label personalUserName = lookup("#currentUserBox").lookup("#userName").query();

        Assert.assertEquals("Peter Lustig", personalUserName.getText());
        clickOn("#logoutButton");
    }

    @Test
    public void serverBoxTest() throws InterruptedException {
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        ObservableList<Server> itemList = serverListView.getItems();
        String serverName = "";
        for (Server server : itemList) {
            if (server.getName().equals("Test 2")) {
                serverName = "Test 2";
                break;
            }
        }
        Assert.assertEquals("Test 2", serverName);
        clickOn("#logoutButton");
    }

    @Test
    public void userBoxTest() throws InterruptedException {
        RestClient restClient = new RestClient();
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        restClient.login("Peter Lustig 4", "1234", response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        ObservableList<User> itemList = userList.getItems();
        String userName = "";
        for (User user : itemList) {
            if (user.getName().equals("Peter Lustig 4")) {
                userName = "Peter Lustig 4";
                break;
            }
        }
        Assert.assertEquals("Peter Lustig 4", userName);
        Thread.sleep(2000);
        restClient.logout(userKey, response -> {});
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        itemList = userList.getItems();
        userName = "";
        for (User user : itemList) {
            if (user.getName().equals("Peter Lustig 4")) {
                userName = "Peter Lustig 4";
                break;
            }
        }
        Assert.assertEquals("", userName);
        clickOn("#logoutButton");
    }

    @Test
    public void getServersTest() {
        restMock.getServers("bla", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).getServers(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void getUsersTest() {
        restMock.getUsers("bla", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).getUsers(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void privateChatTest() throws InterruptedException {
        RestClient restClient = new RestClient();
        restClient.login("Peter Lustig 2", "1234", response -> {
        });
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        clickOn(userList.lookup("#user"));
        clickOn(userList.lookup("#user"));
        ListView<Channel> privateChatlist = lookup("#privateChatList").query();
        Assert.assertEquals(userList.getItems().get(0).getName(), privateChatlist.getItems().get(0).getName());
        clickOn("#logoutButton");
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
        Assert.assertEquals("Accord - Main", stage.getTitle());
        clickOn("#logoutButton");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Accord - Login", stage.getTitle());

        restMock.logout("c653b568-d987-4331-8d62-26ae617847bf", response -> {
        });
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).logout(anyString(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Assert.assertEquals("{}", res.getBody().toString());
    }

    @Test
    public void openExistingChat() throws InterruptedException {
        RestClient restClient = new RestClient();
        String testUserOneName = "Tuser1";
        String testUserTwoName = "Tuser2";

        restClient.login(testUserOneName, "1234", response -> {
        });
        restClient.login(testUserTwoName, "1234", response -> {
        });
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#homeButton");
        Thread.sleep(500);
        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();

        User testUserOne = new User();
        User testUserTwo = new User();
        for (User user : userList.getItems()) {
            if (user.getName().equals("Tuser1")) {
                testUserOne = user;
            }
            if (user.getName().equals("Tuser2")) {
                testUserTwo = user;
            }
        }

        clickOn(userList.lookup("#" + testUserOne.getId()));
        clickOn(userList.lookup("#" + testUserOne.getId()));
        Thread.sleep(500);
        clickOn(userList.lookup("#" + testUserTwo.getId()));
        clickOn(userList.lookup("#" + testUserTwo.getId()));

        Thread.sleep(500);

        Assert.assertEquals(testUserTwo.getName(), PrivateViewController.getSelectedChat().getName());

        Thread.sleep(500);

        ListView<Channel> privateChatList = lookup("#privateChatList").query();
        clickOn(privateChatList.lookup("#" + testUserOne.getId()));

        Assert.assertEquals(testUserOne.getName(), PrivateViewController.getSelectedChat().getName());

        //Additional test if opened private chat is colored
        VBox privateChatCell = lookup("#cell_" + testUserOne.getId()).query();
        Assert.assertEquals("-fx-background-color: #737373; -fx-border-size: 2px; -fx-border-color: #AAAAAA; -fx-pref-height: 65; -fx-max-width: 183", privateChatCell.getStyle());
    }
}