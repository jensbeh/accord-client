package de.uniks.stp;

import de.uniks.stp.controller.HomeViewController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerMessageTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;
    private final String testServerName = "TestServer Team Bit Shift";
    private final String testServerId = "5e2fbd8770dd077d03df505";
    private final String testServerOwner = "5e2iof875dd077d03df505";
    private final String testUserMainName = "Hendry Bracken";
    private final String testUserMainPw = "stp2021pw";
    private final String testUserMain_UserKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserOne_UserKey;

    @Mock
    private RestClient restClient;

    @Mock
    private HttpResponse<JsonNode> response;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @InjectMocks
    StageManager mockApp = new StageManager();

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        this.stage = stage;
        app = mockApp;
        app.setRestClient(restClient);
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(HomeViewController.class);
    }

    public void mockLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("userKey", testUserMainPw));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                mockGetServers();
                return null;
            }
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", testServerId).put("name", testServerName)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                mockGetServerUsers();
                return null;
            }
        }).when(restClient).getServers(anyString(), callbackCaptor.capture());
    }

    public void mockGetServerUsers() {
        String categories[] = new String[1];
        categories[0] = "5e2fbd8770dd077d03df600";
        JSONArray members = new JSONArray().put(new JSONObject().put("id", testServerOwner).put("id", testUserMainName).put("online", true));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", testServerId).put("name", testServerName).put("owner", testServerOwner).put("categories", categories).put("members", members));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                mockGetServerCategories();
                return null;
            }
        }).when(restClient).getServerUsers(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockGetServerCategories() {
        String[] channels = new String[1];
        channels[0] = "60adc8aec77d3f78988b57a0";
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df600").put("name", "default")
                        .put("server", "5e2fbd8770dd077d03df505").put("channels", channels)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                mockGetCategoryChannels();
                return null;
            }
        }).when(restClient).getServerCategories(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockGetCategoryChannels() {
        String[] members = new String[0];
        String[] audioMembers = new String[0];
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "60adc8aec77d3f78988b57a0").put("name", "general").put("type", "text")
                        .put("privileged", false).put("category", "5e2fbd8770dd077d03df600").put("members", members).put("audioMembers", audioMembers)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                mockGetCategoryChannels();
                return null;
            }
        }).when(restClient).getCategoryChannels(anyString(), anyString(), anyString(), callbackCaptor.capture());
    }

    /*public void loginInitWithTempUser() throws InterruptedException {
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
    }*/

    public void loginInit(String name, String password) throws InterruptedException {
        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(name);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(password);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testSendAllMessage() throws InterruptedException {
        /*restClient.loginTemp(response -> {
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

        restClient.postServer(testUserOne_UserKey, "TestServer Team Bit Shift", response -> {
                    //String serverId = response.getBody().getObject().getJSONObject("data").getString("id");
        });
        String serverId = "ri9fdrSw0fj90";

        restClient.logout(testUserOne_UserKey, response -> {
        });*/

        loginInit(testUserMainName, testUserMainPw);

        Platform.runLater(() -> Assert.assertEquals("Accord - Main", stage.getTitle()));

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

        Thread.sleep(1000);

        ServerChannel channel = app.getBuilder().getCurrentServer().getCategories().get(0).getChannel().get(0);
        ListView<User> channelList = lookup("#scrollPaneCategories").lookup("#categoryVbox").lookup("#channellist").query();
        doubleClickOn(channelList.lookup("#" + channel.getId()));
/*
        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        Thread.sleep(2000);
        clickOn("#sendButton");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd.MM - HH:mm");
        String time = dtf2.format(LocalDateTime.now());
        Thread.sleep(2000);

        ListView<Message> privateChatMessageList = lookup("#messageListView").query();
        Label messageLabel = (Label) privateChatMessageList.lookup("#messageLabel");
        Label userNameLabel = (Label) privateChatMessageList.lookup("#userNameLabel");
        Assert.assertEquals(" Okay! ", messageLabel.getText());
        Assert.assertEquals(time + " " + testUserOneName, userNameLabel.getText());

        Assert.assertEquals(1, privateChatMessageList.getItems().size());


        messageField.setText("Okay");
        write("\n");
        boolean msgArrived = false;
        for (int i = 0; i < privateChatMessageList.getItems().size(); i++) {
            if (privateChatMessageList.getItems().get(i).getMessage().equals("Okay")) {
                msgArrived = true;
            }
        }
        Assert.assertTrue(msgArrived);


        Thread.sleep(2000);*/
    }
}
