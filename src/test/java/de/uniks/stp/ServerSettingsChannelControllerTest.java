package de.uniks.stp;

import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class ServerSettingsChannelControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserOne_UserKey;
    private static String testServerId;


    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
        this.restClient = new RestClient();
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
    public void openServerPrivilegeSettingsTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);
        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        Server server = serverListView.getItems().get(0);

        clickOn("#homeButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Assert.assertEquals("Accord - Main", stage.getTitle());

        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        clickOn("#serverMenuButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        clickOn("#ServerSettings");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        clickOn("#channel");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Label categoryLabel = lookup("#categoryLabel").query();
        ComboBox<String> categorySelector = lookup("#categorySelector").query();
        Label editChannelsLabel = lookup("#editChannelsLabel").query();
        ComboBox<String> editChannelsSelector = lookup("#editChannelsSelector").query();
        TextField editChannelsTextField = lookup("#editChannelsTextField").query();
        Button channelChangeButton = lookup("#channelChangeButton").query();
        Button channelDeleteButton = lookup("#channelDeleteButton").query();
        Label createChannelLabel = lookup("#createChannelLabel").query();
        TextField createChannelTextField = lookup("#createChannelTextField").query();
        RadioButton channelTextRadioButton = lookup("#channelTextRadioButton").query();
        RadioButton channelVoiceRadioButton = lookup("#channelVoiceRadioButton").query();
        Button channelCreateButton = lookup("#channelCreateButton").query();

        Assert.assertEquals("Category", categoryLabel.getText());
        Assert.assertEquals("Edit Channels", editChannelsLabel.getText());
        Assert.assertEquals("Create Channel", createChannelLabel.getText());
        Assert.assertEquals("change", channelChangeButton.getText());
        Assert.assertEquals("Delete", channelDeleteButton.getText());
        Assert.assertEquals("create", channelCreateButton.getText());
        Assert.assertEquals("Text", channelTextRadioButton.getText());
        Assert.assertEquals("Voice", channelVoiceRadioButton.getText());

        
    }
}
