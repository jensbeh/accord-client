package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.Silent.class)
public class InviteUsersControllerTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;

    @Mock
    private RestClient restClient;

    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;

    @Mock
    private HttpResponse<JsonNode> response;

    @Mock
    private HttpResponse<JsonNode> response2;

    @Mock
    private HttpResponse<JsonNode> response3;

    @Mock
    private HttpResponse<JsonNode> response4;

    @Mock
    private HttpResponse<JsonNode> response5;

    @Mock
    private HttpResponse<JsonNode> response6;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor2;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor3;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor4;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor5;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor6;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        Mockito.reset();
        //start application
        ModelBuilder builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketCLient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        app.setRestClient(restClient);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @InjectMocks
    StageManager mockApp = new StageManager();

    @BeforeAll
    static void setup() throws IOException {
        MockitoAnnotations.openMocks(PrivateMessageTest.class);
    }


    public void mockGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", "TestServer Team Bit Shift")));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response2.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor2.getValue();
            callback.completed(response2);
            mockGetServerUser();
            return null;
        }).when(restClient).getServers(anyString(), callbackCaptor2.capture());
    }

    public void mockGetServerUser() {
        JSONArray members = new JSONArray();
        JSONArray categories = new JSONArray();
        categories.put("60b77ba0026b3534ca5a61ae");
        JSONObject member = new JSONObject();
        member.put("id", "60ad230ac77d3f78988b3e5b")
                .put("name", "Peter Lustig")
                .put("online", true);
        members.put(member);
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject()
                        .put("id", "5e2fbd8770dd077d03df505")
                        .put("name", "asdfasdf")
                        .put("owner", "60ad230ac77d3f78988b3e5b")
                        .put("categories", categories)
                        .put("members", members)
                );
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response3.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor3.getValue();
            callback.completed(response3);
            mockGetCategories();
            return null;
        }).when(restClient).getServerUsers(anyString(), anyString(), callbackCaptor3.capture());
    }

    public void mockGetCategories() {
        JSONArray channels = new JSONArray();
        channels.put("60b77ba0026b3534ca5a61af");
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "60b77ba0026b3534ca5a61ae")
                .put("name", "default")
                .put("server", "5e2fbd8770dd077d03df505")
                .put("channels", channels));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", data);
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response4.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor4.getValue();
            callback.completed(response4);
            return null;
        }).when(restClient).getServerCategories(anyString(), anyString(), callbackCaptor4.capture());
    }

    public void mockGetChannels() {
        JSONArray members = new JSONArray();
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "60b77ba0026b3534ca5a61af")
                .put("name", "testChannel")
                .put("type", "text")
                .put("privileged", false)
                .put("category", "60b77ba0026b3534ca5a61ae")
                .put("members", members));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", data);
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response5.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor5.getValue();
            callback.completed(response5);
            return null;
        }).when(restClient).getCategoryChannels(anyString(), anyString(), anyString(), callbackCaptor5.capture());
    }


    public void loginInit() throws InterruptedException {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        doCallRealMethod().when(privateChatWebSocket).handleMessage(any());
        doCallRealMethod().when(privateChatWebSocket).setBuilder(any());
        doCallRealMethod().when(privateChatWebSocket).setPrivateViewController(any());
        doCallRealMethod().when(serverChatWebSocket).handleMessage(any());
        doCallRealMethod().when(serverChatWebSocket).setBuilder(any());
        doCallRealMethod().when(serverChatWebSocket).setServerViewController(any());
        mockGetServers();
        mockGetServerUser();
        mockGetCategories();
        mockGetChannels();

        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("name", "Peter")
                .put("password", "1234")
                .put("data", new JSONObject().put("userKey", "c3a981d1-d0a2-47fd-ad60-46c7754d9271"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            String name = (String) invocation.getArguments()[0];
            String password = (String) invocation.getArguments()[1];
            System.out.println(name);
            System.out.println(password);
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            return null;
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        clickOn("#loginButton");

        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"Gustav\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);


        message = "{\"channel\":\"private\",\"to\":\"Mr. Poopybutthole\",\"message\":\"Hallo\",\"from\":\"Allyria Dayne\",\"timestamp\":1623805070036}\"";
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateChatWebSocket.handleMessage(jsonObject);
    }

    @Test
    public void openInviteUsersTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();

        Circle addServer = lookup("#addServer").query();
        ListView<Server> serverList = lookup("#serverList").query();
        Circle c = lookup("#serverName_" + serverList.getItems().get(0).getId()).query();
        clickOn(c);

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(2, this.listTargetWindows().size());
        String serverSettingsTitle = "";
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                serverSettingsTitle = ((Stage) object).getTitle();
                Assert.assertNotEquals("", serverSettingsTitle);
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
    }

    @Test
    public void changeInviteUsersSubViewTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();

        Circle addServer = lookup("#addServer").query();
        ListView<Server> serverList = lookup("#serverList").query();
        Circle c = lookup("#serverName_" + serverList.getItems().get(0).getId()).query();
        clickOn(c);

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        RadioButton temp = lookup("#tempSelected").query();
        Assert.assertTrue(temp.isSelected());
        clickOn("#userLimitSelected");
        Assert.assertFalse(temp.isSelected());

        // check create and delete userLimitLink

        Label label = lookup("#userLimit").query();
        Assert.assertEquals("User Limit", label.getText());
        TextField userLimit = lookup("#maxUsers").query();
        userLimit.setText("1");
        int count = 0;
        count = Integer.parseInt(userLimit.getText());
        Assert.assertEquals(count, 1);


        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "00000000").put("link", "https://ac.uniks.de/api/servers/5e2ffbd8770dd077d03df505/invites/5e2ffbd8770dd077d445qs900").put("type", "count").put("max", 1).put("current", 0).put("server", serverList.getItems().get(0).getId()));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response6.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor6.getValue();
            callback.completed(response6);
            return null;
        }).when(restClient).createTempLink(anyString(), anyInt(), anyString(), anyString(), callbackCaptor6.capture());


        clickOn("#createLink");
        clickOn("#createLink");
        clickOn("#createLink");
        WaitForAsyncUtils.waitForFxEvents();

        TextField link = lookup("#linkTextField").query();
        ComboBox<List<String>> links = lookup("#LinkComboBox").query();
        String certainLink = "";
        for (String s : links.getItems().get(2)) {
            if (s.equals(link.getText())) {
                certainLink = s;
                break;
            }
        }
        Assert.assertNotEquals("", certainLink);

        clickOn(links);
        moveBy(0, 25);
        write("\n");
        clickOn("#deleteLink");
        System.out.println(links.getSelectionModel().getSelectedItem());
        String checkDel = "";
        for (List<String> s : links.getItems()) {
            if (s.get(0).equals(link.getText())) {
                checkDel = s.get(0);
                break;
            }
        }
        Assert.assertEquals("", checkDel);

        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();


        clickOn("#userLimitSelected");
        Assert.assertNotNull(links.getItems());

        clickOn(links);
        moveBy(0, 25);
        write("\n");
        clickOn("#deleteLink");
        System.out.println(links.getSelectionModel().getSelectedItem());
        Thread.sleep(500);
        String checkDelete = "";
        for (List<String> s : links.getItems()) {
            if (s.get(0).equals(link.getText())) {
                checkDelete = s.get(0);
                break;
            }
        }
        Assert.assertEquals("", checkDelete);

        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#logoutButton");
        Thread.sleep(2000);
    }


    //@Test
    public void generateAndDeleteTempLink() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#createLink");
        clickOn("#createLink");
        clickOn("#createLink");
        WaitForAsyncUtils.waitForFxEvents();
        TextField link = lookup("#linkTextField").query();
        ComboBox<String> links = lookup("#LinkComboBox").query();
        String certainLink = "";
        for (String s : links.getItems()) {
            if (s.equals(link.getText())) {
                certainLink = s;
                break;
            }
        }
        Assert.assertNotEquals("", certainLink);
        clickOn(links);
        moveBy(0, 25);
        write("\n");
        clickOn("#deleteLink");
        String checkDel = "";
        for (String s : links.getItems()) {
            if (s.equals(link.getText())) {
                checkDel = s;
                break;
            }
        }
        Assert.assertEquals("", checkDel);

        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");

        clickOn(links);
        moveBy(0, 25);
        write("\n");
        clickOn("#deleteLink");
        String checkDelete = "";
        for (String s : links.getItems()) {
            if (s.equals(link.getText())) {
                checkDelete = s;
                break;
            }
        }
        Assert.assertEquals("", checkDelete);
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#logoutButton");
        Thread.sleep(2000);
    }

    //@Test
    public void inviteUsersErrorMessagesTest() throws InterruptedException {
        loginInit();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(2, this.listTargetWindows().size());

        clickOn("#createLink");
        Thread.sleep(1000);
        TextField linkField = lookup("#linkTextField").query();
        String inviteLink = linkField.getText();

        String serverSettingsTitle = "";
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                serverSettingsTitle = ((Stage) object).getTitle();
                Assert.assertNotEquals("", serverSettingsTitle);
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        clickOn(addServer);
        TextField insertInviteLink = lookup("#inviteLink").query();
        insertInviteLink.setText(inviteLink);
        clickOn("#joinServer");
        Label errorLabel = lookup("#errorLabel").query();
        Thread.sleep(1000);
        Assert.assertEquals(errorLabel.getText(), "You already joined the server");

        StringBuilder sb = new StringBuilder(inviteLink);
        sb.deleteCharAt(45);
        insertInviteLink.setText(sb.toString());
        clickOn("#joinServer");
        Thread.sleep(1000);
        Assert.assertEquals(errorLabel.getText(), "Wrong server id or Invalid link");

        insertInviteLink.setText("rgasdgydfg");
        clickOn("#joinServer");
        Thread.sleep(1000);
        Assert.assertEquals(errorLabel.getText(), "Invalid link");

        insertInviteLink.setText("");
        clickOn("#joinServer");
        Thread.sleep(1000);
        Assert.assertEquals(errorLabel.getText(), "Insert invite link first");

        clickOn("#logoutButton");
        Thread.sleep(2000);
    }
}
