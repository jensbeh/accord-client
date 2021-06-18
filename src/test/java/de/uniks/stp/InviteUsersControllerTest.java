package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.PrivateChatWebSocket;
import de.uniks.stp.net.PrivateSystemWebSocketClient;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.ServerChatWebSocket;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class InviteUsersControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private final String testServerName = "TestServer Team Bit Shift";
    private final String testUserName = "Hendry Bracken";
    private final String testUserPw = "stp2021pw";
    private final String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
    private final String testServerOwner = "5e2iof875dd077d03df505";
    private final String testServerId = "5e2fbd8770dd077d03df505";

    private String inviteLinkIds = "5e2fbd8770dd077d445qs900";
    private int inviteLinksCount = 0;
    private int inviteLinksCountDelete = 0;

    @Mock
    private RestClient restClient;

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

    @Mock
    private HttpResponse<JsonNode> response7;

    @Mock
    private HttpResponse<JsonNode> response8;

    @Mock
    private HttpResponse<JsonNode> response9;

    @Mock
    private HttpResponse<JsonNode> response10;

    @Mock
    private HttpResponse<JsonNode> response11;

    @Mock
    private HttpResponse<JsonNode> response12;

    @Mock
    private HttpResponse<JsonNode> response13;

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

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor7;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor8;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor9;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor10;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor11;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor12;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor13;

    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @InjectMocks
    StageManager mockApp = new StageManager();

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(LoginScreenController.class);
    }

    @Override
    public void start(Stage stage) {
        ModelBuilder builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketCLient(privateChatWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        app.setRestClient(restClient);
        app.start(stage);
        this.stage.centerOnScreen();
    }

    public void mockLogin() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("userKey", userKey));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor.getValue();
                callback.completed(response);
                return null;
            }
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void mockPostServer() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testServerName));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response2.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor2.getValue();
                callback.completed(response2);
                return null;
            }
        }).when(restClient).postServer(anyString(), anyString(), callbackCaptor2.capture());
    }

    public void mockGetServers() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray().put(new JSONObject().put("id", "5e2fbd8770dd077d03df505").put("name", testServerName)));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response3.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor3.getValue();
                callback.completed(response3);
                return null;
            }
        }).when(restClient).getServers(anyString(), callbackCaptor3.capture());
    }

    public void mockGetServerUsers() {
        String categories[] = new String[1];
        categories[0] = "5e2fbd8770dd077d03df600";
        JSONArray members = new JSONArray().put(new JSONObject().put("id", testServerOwner).put("name", testUserName).put("online", true));
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", testServerId).put("name", testServerName).put("owner", testServerOwner).put("categories", categories).put("members", members));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response4.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor4.getValue();
                callback.completed(response4);
                return null;
            }
        }).when(restClient).getServerUsers(anyString(), anyString(), callbackCaptor4.capture());
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
        when(response5.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor5.getValue();
                callback.completed(response5);
                return null;
            }
        }).when(restClient).getServerCategories(anyString(), anyString(), callbackCaptor5.capture());
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
        when(response6.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor6.getValue();
                callback.completed(response6);
                mockGetCategoryChannels();
                return null;
            }
        }).when(restClient).getCategoryChannels(anyString(), anyString(), anyString(), callbackCaptor6.capture());
    }

    public void mockGetServersEmpty() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response7.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor7.getValue();
                callback.completed(response7);
                return null;
            }
        }).when(restClient).getServers(anyString(), callbackCaptor7.capture());
    }

    public void mockGetChannelMessages() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response8.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor8.getValue();
                callback.completed(response8);
                return null;
            }
        }).when(restClient).getChannelMessages(anyLong(), anyString(), anyString(), anyString(), anyString(), callbackCaptor8.capture());
    }

    public void mockJoinServer() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "Successfully arrived at server")
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response9.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor9.getValue();
                callback.completed(response9);
                return null;
            }
        }).when(restClient).joinServer(anyString(), anyString(), anyString(), anyString(), anyString(), callbackCaptor9.capture());
    }

    public void mockCreateTempLink() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", inviteLinkIds + inviteLinksCount)
                        .put("link", "https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + inviteLinksCount)
                        .put("type", "count").put("max", 10).put("current", inviteLinksCount++)
                        .put("server", testServerId));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response10.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor10.getValue();
                callback.completed(response10);
                mockCreateTempLink();
                return null;
            }
        }).when(restClient).createTempLink(anyString(), anyInt(), anyString(), anyString(), callbackCaptor10.capture());
    }

    public void mockGetInvLinks() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONArray()
                        .put(new JSONObject()
                                .put("id", inviteLinkIds + "0")
                                .put("link", "https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + "0")
                                .put("type", "count").put("max", 10).put("current", "0")
                                .put("server", testServerId))
                        .put(new JSONObject()
                                .put("id", inviteLinkIds + "1")
                                .put("link", "https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + "1")
                                .put("type", "count").put("max", 10).put("current", "1")
                                .put("server", testServerId))
                        .put(new JSONObject()
                                .put("id", inviteLinkIds + "2")
                                .put("link", "https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + "2")
                                .put("type", "count").put("max", 10).put("current", "2")
                                .put("server", testServerId)));

        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response11.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor11.getValue();
                callback.completed(response11);
                mockGetInvLinks();
                return null;
            }
        }).when(restClient).getInvLinks(anyString(), anyString(), callbackCaptor11.capture());
    }

    public void mockDeleteInvLink() {
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("id", inviteLinkIds + inviteLinksCountDelete)
                        .put("link", "https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + inviteLinksCountDelete)
                        .put("type", "count").put("max", 10).put("current", inviteLinksCountDelete++)
                        .put("server", testServerId));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response12.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor12.getValue();
                callback.completed(response12);
                mockDeleteInvLink();
                return null;
            }
        }).when(restClient).deleteInvLink(anyString(), anyString(), anyString(), callbackCaptor12.capture());
    }

    private void mockJoinServerError(String message) {
        JSONObject jsonString = new JSONObject()
                .put("status", "failure")
                .put("message", message)
                .put("data", new JSONObject());
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response13.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Callback<JsonNode> callback = callbackCaptor13.getValue();
                callback.completed(response13);
                return null;
            }
        }).when(restClient).joinServer(anyString(), anyString(), anyString(), anyString(), anyString(), callbackCaptor13.capture());
    }

    public void loginInit(boolean emptyServers) throws InterruptedException {
        mockPostServer();
        if (!emptyServers)
            mockGetServers();
        else
            mockGetServersEmpty();
        mockGetServerUsers();
        mockGetServerCategories();
        mockGetCategoryChannels();
        mockGetChannelMessages();
        mockJoinServer();
        mockCreateTempLink();
        mockGetInvLinks();
        mockDeleteInvLink();

        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserPw);
        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void openInviteUsersTest() throws InterruptedException {
        loginInit(false);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + testServerId));
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
        loginInit(false);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + testServerId));
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
        clickOn("https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + "0 | 10");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#deleteLink");
        System.out.println(links.getSelectionModel().getSelectedItem());
        WaitForAsyncUtils.waitForFxEvents();
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

        clickOn("Select Link...");
        moveBy(0, 25);
        clickOn("https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + "0 | 10");
        clickOn("#deleteLink");
        System.out.println(links.getSelectionModel().getSelectedItem());
        WaitForAsyncUtils.waitForFxEvents();
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
    }

    @Test
    public void generateAndDeleteTempLink() throws InterruptedException {
        loginInit(false);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

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
        clickOn("https://ac.uniks.de/api/servers/" + testServerId + "/invites/" + inviteLinkIds + "0");
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
        clickOn("#logoutButton");
    }

    @Test
    public void inviteUsersErrorMessagesTest() throws InterruptedException {
        loginInit(false);

        Circle addServer = lookup("#addServer").query();

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(2, this.listTargetWindows().size());

        clickOn("#createLink");
        WaitForAsyncUtils.waitForFxEvents();
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
        mockJoinServerError("You already joined the server");
        clickOn("#joinServer");
        Label errorLabel = lookup("#errorLabel").query();
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(errorLabel.getText(), "You already joined the server");

        StringBuilder sb = new StringBuilder(inviteLink);
        sb.deleteCharAt(45);
        insertInviteLink.setText(sb.toString());
        mockJoinServerError("Wrong server id or Invalid link");
        clickOn("#joinServer");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(errorLabel.getText(), "Wrong server id or Invalid link");

        insertInviteLink.setText("KkDs0K3Dak");
        clickOn("#joinServer");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(errorLabel.getText(), "Invalid link");

        insertInviteLink.setText("");
        clickOn("#joinServer");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(errorLabel.getText(), "Insert invite link first");

        clickOn("#logoutButton");
    }
}
