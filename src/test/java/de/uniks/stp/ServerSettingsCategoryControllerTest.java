package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.*;
import javafx.application.Platform;
import javafx.scene.control.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerSettingsCategoryControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private final String testServerName = "TestServer Team Bit Shift";
    private final String testServerId = "5e2fbd8770dd077d03df505";


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

    @Mock
    private HttpResponse<JsonNode> response7;

    @Mock
    private HttpResponse<JsonNode> response8;

    @Mock
    private HttpResponse<JsonNode> response9;

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
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor7;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor8;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor9;

    @InjectMocks
    StageManager mockApp = new StageManager();
    private ModelBuilder builder;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        //start application
        builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketCLient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(ServerSettingsCategoryControllerTest.class);
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
    public void openServerSettingsCategoryTest() throws InterruptedException {
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#serverName_5e2fbd8770dd077d03df505");
        WaitForAsyncUtils.waitForFxEvents();

        Server currentServer = null;
        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        for (Server server : serverListView.getItems()) {
            if (server.getName().equals("TestServer Team Bit Shift")) {
                currentServer = server;
            }
        }

        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");
        clickOn("#category");

        Label editCategoryLabel = lookup("#editCategoryLabel").query();
        Label createCategoryLabel = lookup("#createCategoryLabel").query();
        ComboBox<Categories> categoriesSelector = lookup("#editCategoriesSelector").query();
        TextField categoryNameTextField = lookup("#editCategoryNameTextField").query();
        Button changeCategoryNameButton = lookup("#changeCategoryNameButton").query();
        Button deleteCategoryButton = lookup("#deleteCategoryButton").query();
        TextField createCategoryNameTextField = lookup("#createCategoryNameTextField").query();
        Button createCategoryButton = lookup("#createCategoryButton").query();

        Assert.assertEquals("Edit Category", editCategoryLabel.getText());
        Assert.assertEquals("Create Category", createCategoryLabel.getText());
        Assert.assertEquals("change", changeCategoryNameButton.getText());
        Assert.assertEquals("Delete", deleteCategoryButton.getText());
        Assert.assertEquals("create", createCategoryButton.getText());


        // type in new name for channel
        createCategoryNameTextField.setText("NewCategory");
        // click on create

        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("name", "NewCategory").put("server", testServerId).put("channel", new JSONObject()).put("id", "5e2ffbd8770dd077d03df600"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response8.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor8.getValue();
            callback.completed(response8);
            return null;
        }).when(restClient).createCategory(anyString(), anyString(), anyString(), callbackCaptor8.capture());

        clickOn(createCategoryButton);
        WaitForAsyncUtils.waitForFxEvents();

        Categories newCategory = new Categories();
        for (Categories category : categoriesSelector.getItems()) {
            if (category.getName().equals("NewCategory")) {
                newCategory = category;
            }
        }
        Assert.assertTrue(createCategoryNameTextField.getText().equals(""));
        Assert.assertTrue(newCategory.getName().equals("NewCategory"));
        for (Categories categories : app.getBuilder().getCurrentServer().getCategories()) {
            if (categories.getId().equals(newCategory.getId())) {
                Assert.assertTrue(categories.getId().equals(newCategory.getId()));
            }
        }

        clickOn(categoriesSelector);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(newCategory.getName());
        WaitForAsyncUtils.waitForFxEvents();

        categoryNameTextField.setText("NewCategoryName");

        jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("name", "NewCategoryName").put("server", testServerId).put("channel", new JSONObject()).put("id", "5e2ffbd8770dd077d03df600"));
        jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response7.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor7.getValue();
            callback.completed(response7);
            return null;
        }).when(restClient).updateCategory(anyString(), anyString(), anyString(), anyString(), callbackCaptor7.capture());


        clickOn(changeCategoryNameButton);
        WaitForAsyncUtils.waitForFxEvents();

        for (Categories category : categoriesSelector.getItems()) {
            if (category.getId().equals(newCategory.getId())) {
                newCategory = category;
            }
        }

        Assert.assertTrue(categoryNameTextField.getText().equals(""));
        Assert.assertTrue(newCategory.getName().equals("NewCategoryName"));
        Assert.assertTrue(currentServer.getCategories().contains(newCategory));

        clickOn(categoriesSelector);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(newCategory.getName());
        WaitForAsyncUtils.waitForFxEvents();

        jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("name", "NewCategoryName").put("server", testServerId).put("channel", new JSONObject()).put("id", "5e2ffbd8770dd077d03df600"));
        jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response9.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor9.getValue();
            callback.completed(response9);
            return null;
        }).when(restClient).deleteCategory(anyString(), anyString(), anyString(), callbackCaptor9.capture());


        clickOn(deleteCategoryButton);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertFalse(builder.getCurrentServer().getCategories().contains(newCategory));

        Assert.assertFalse(categoriesSelector.getItems().contains(newCategory));

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
    }
}
