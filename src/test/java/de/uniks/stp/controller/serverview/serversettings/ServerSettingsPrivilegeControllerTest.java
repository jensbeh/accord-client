package de.uniks.stp.controller.serverview.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerSettingsPrivilegeControllerTest extends ApplicationTest {

    private final ArrayList<User> privileged = new ArrayList<>();
    @InjectMocks
    StageManager mockApp = new StageManager();
    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;
    @Mock
    private PrivateChatWebSocket privateChatWebSocket;
    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;
    @Mock
    private ServerChatWebSocket serverChatWebSocket;
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
    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(ServerSettingsPrivilegeControllerTest.class);
    }

    @Override
    public void start(Stage stage) {
        //start application
        builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        StageManager app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        builder.setLoadUserData(false);
        mockApp.getBuilder().setSpotifyShow(false);
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().setSpotifyRefresh(null);

        app.start(stage);
        stage.centerOnScreen();
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
                .put("name", "Peter")
                .put("online", true).put("description", "");
        members.put(member);
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject()
                        .put("id", "5e2fbd8770dd077d03df505")
                        .put("name", "JOIdk")
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
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "5e2fbd8770dd077d03df600")
                .put("name", "default")
                .put("server", "5e2fbd8770dd077d03df505")
                .put("channels", new JSONArray().put("60b77ba0026b3534ca5a61af")));
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
        JSONArray audioMembers = new JSONArray();
        JSONArray data = new JSONArray();
        data.put(new JSONObject()
                .put("id", "60b77ba0026b3534ca5a61af")
                .put("name", "testChannel")
                .put("type", "text")
                .put("privileged", false)
                .put("category", "60b77ba0026b3534ca5a61ae")
                .put("members", members)
                .put("audioMembers", audioMembers))
            .put(new JSONObject()
                    .put("id", "60b77ba0026b3534ca5a61ag")
                    .put("name", "testChannel2")
                    .put("type", "text")
                    .put("privileged", false)
                    .put("category", "60b77ba0026b3534ca5a61ae")
                    .put("members", members)
                    .put("audioMembers", audioMembers));
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
    }

    @Test
    public void openServerSettingsPrivilegeTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");
        clickOn("#privilegeBtn");

        Server currentServer = builder.getCurrentServer();
        RadioButton privilegeOn = lookup("#Privilege_On_Button").query();
        RadioButton privilegeOff = lookup("#Privilege_Off_Button").query();
        HBox privilegeOnView = lookup("#Privilege_On").query();

        ComboBox<Categories> categoryChoice = lookup("#Category").query();
        ComboBox<ServerChannel> channelChoice = lookup("#Channels").query();

        Assert.assertEquals(categoryChoice.getItems(), currentServer.getCategories());

        clickOn(categoryChoice);
        interact(() -> categoryChoice.getSelectionModel().select(0));
        Assert.assertEquals(channelChoice.getItems(), currentServer.getCategories().get(0).getChannel());

        // choose for the second channel, because the first channel counts as default channel, which is not allowed to be edited
        clickOn(channelChoice);
        interact(() -> channelChoice.getSelectionModel().select(1));

        clickOn(privilegeOn);
        Assert.assertTrue(privilegeOn.isSelected());
        Assert.assertFalse(privilegeOff.isSelected());
        Assert.assertTrue(privilegeOnView.getChildren().isEmpty());

        for (User user : currentServer.getUser()) {
            if (user.getId().equals(currentServer.getOwner())) {
                privileged.add(user);
            }
        }

        User test = new User().setName("Test").setId("1");
        currentServer.withUser(test);

        clickOn("#Change_Privilege");
        Assert.assertTrue(currentServer.getCategories().get(0).getChannel().get(1).isPrivilege());
        Assert.assertFalse(privilegeOnView.getChildren().isEmpty());
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(1).getPrivilegedUsers());

        ComboBox<String> addMenu = lookup("#Add_User_to_Privilege").query();
        ComboBox<String> removeMenu = lookup("#Remove_User_from_Privilege").query();

        clickOn(addMenu);
        interact(() -> addMenu.getSelectionModel().select(0));
        for (User user : currentServer.getUser()) {
            if (user.getName().equals(addMenu.getSelectionModel().getSelectedItem())) {
                privileged.add(user);
            }
        }
        clickOn("#User_to_Privilege");
        WaitForAsyncUtils.waitForFxEvents();
        System.out.println(currentServer.getCategories().get(0).getChannel());
        Assert.assertEquals(currentServer.getUser().size() - 2, addMenu.getItems().size());
        Assert.assertEquals(currentServer.getCategories().get(0).getChannel().get(1).getPrivilegedUsers().size(), removeMenu.getItems().size());
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(1).getPrivilegedUsers());

        clickOn(removeMenu);
        interact(() -> removeMenu.getSelectionModel().select(0));
        for (User user : currentServer.getUser()) {
            if (user.getName().equals(removeMenu.getSelectionModel().getSelectedItem())) {
                privileged.remove(user);
            }
        }
        clickOn("#User_from_Privilege");
        Assert.assertEquals(currentServer.getUser().size() - 1, addMenu.getItems().size());
        Assert.assertEquals(currentServer.getCategories().get(0).getChannel().get(1).getPrivilegedUsers().size(), removeMenu.getItems().size());
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(1).getPrivilegedUsers());


        clickOn(removeMenu);
        interact(() -> removeMenu.getSelectionModel().select(0));
        for (User user : currentServer.getUser()) {
            if (user.getName().equals(removeMenu.getSelectionModel().getSelectedItem())) {
                privileged.remove(user);
            }
        }
        clickOn("#User_from_Privilege");
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(1).getPrivilegedUsers());
        Assert.assertTrue(privilegeOnView.getChildren().isEmpty());
        Assert.assertTrue(privilegeOff.isSelected());
        Assert.assertFalse(privilegeOn.isSelected());
        Assert.assertFalse(currentServer.getCategories().get(0).getChannel().get(1).isPrivilege());


        clickOn(privilegeOn);
        clickOn("#Change_Privilege");

        clickOn(privilegeOff);
        Assert.assertTrue(privilegeOff.isSelected());
        Assert.assertFalse(privilegeOn.isSelected());
        Assert.assertTrue(privilegeOnView.getChildren().isEmpty());
        Assert.assertTrue(currentServer.getCategories().get(0).getChannel().get(1).isPrivilege());

        clickOn("#Change_Privilege");
        Assert.assertFalse(currentServer.getCategories().get(0).getChannel().get(1).isPrivilege());
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(1).getPrivilegedUsers());
    }
}
