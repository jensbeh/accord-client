package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
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
    private RestClient restClient;

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
    }

    @Test
    public void userBoxTest() throws InterruptedException {
        RestClient restClient = new RestClient();
        restClient.login("Peter Lustig 2", "1234", response -> {
        });
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        ObservableList<User> itemList = userList.getItems();
        String userName = "";
        for (User user : itemList) {
            if (user.getName().equals("Peter Lustig 2")) {
                userName = "Peter Lustig 2";
                break;
            }
        }
        Assert.assertEquals("Peter Lustig 2", userName);
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
}