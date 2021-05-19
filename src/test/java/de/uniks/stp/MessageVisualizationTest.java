package de.uniks.stp;

import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class MessageVisualizationTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private String userKey;

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


    @Test()
    public void showServerMessage() throws InterruptedException {
        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("Accord - Main", stage.getTitle());
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        Circle addButton = lookup("#addServer").query();
        clickOn(addButton);
        TextField serverName = lookup("#serverName").query();
        serverName.setText("newServer");
        clickOn("#createServer");
        Thread.sleep(500);
        clickOn(serverList.lookup("#server"));
        Label serverNameText = lookup("#serverName").query();
        Assert.assertEquals(serverNameText.getText(), serverList.getItems().get(0).getName());
        Thread.sleep(500);
        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Hallo");
        clickOn("#sendButton");
        Label messageLabel = lookup("#messageLabel").query();
        Assert.assertEquals(" Hallo ", messageLabel.getText());
    }

    @Test
    public void showPrivateMessage() throws InterruptedException {
        RestClient restClient = new RestClient();
        CheckBox tempBox = lookup("#loginAsTempUser").query();
        tempBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
        Assert.assertEquals("Accord - Main", stage.getTitle());
        restClient.login("Peter Lustig 4", "1234", response -> {
            this.userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        });
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<User> userList = lookup("#scrollPaneUserBox").lookup("#onlineUsers").query();
        doubleClickOn(userList.lookup("#user"));
        Thread.sleep(500);
        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Hallo");
        clickOn("#sendButton");
        Label messageLabel = lookup("#messageLabel").query();
        Assert.assertEquals(" Hallo ", messageLabel.getText());
    }
}
