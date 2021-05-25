package de.uniks.stp;

import de.uniks.stp.model.Message;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class ServerMessageTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private static String testUserMainName;
    private static String testUserMainPw;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserOne_UserKey;

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

    public void loginInitWithTempUser() throws InterruptedException {
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

    @Test
    public void testSendAllMessage() throws InterruptedException {
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
        String serverId = body.getObject().getJSONObject("data").getString("id");

        restClient.logout(testUserOne_UserKey, response -> {
        });

        loginInit(testUserOneName, testUserOnePw);

        Platform.runLater(() -> Assert.assertEquals("Accord - Main", stage.getTitle()));
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);


        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#serverName_" + serverId));
        Thread.sleep(2000);

        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        Thread.sleep(2000);
        clickOn("#sendButton");
        Thread.sleep(2000);

        ListView<Message> privateChatMessageList = lookup("#messageListView").query();
        Label messageLabel = (Label) privateChatMessageList.lookup("#messageLabel");
        Label userNameLabel = (Label) privateChatMessageList.lookup("#userNameLabel");
        Assert.assertEquals(" Okay! ", messageLabel.getText());
        Assert.assertEquals(testUserOneName, userNameLabel.getText());

        Assert.assertEquals(1, privateChatMessageList.getItems().size());

        clickOn("#logoutButton");
        Thread.sleep(2000);
    }
}
