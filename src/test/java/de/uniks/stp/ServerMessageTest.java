package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.ServerViewController;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServerMessageTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;

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

    @Test
    public void testSendAllMessage() throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);
        Platform.runLater(() -> Assert.assertEquals("Accord - Main", stage.getTitle()));
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverListView.lookup("#server"));
        Thread.sleep(2000);
        TextField messageField = lookup("#messageTextField").query();
        messageField.setText("Okay!");
        Thread.sleep(500);
        clickOn("#sendButton");
        Thread.sleep(500);

        ListView<Message> privateChatMessageList = lookup("#messageListView").query();
        Label messageLabel = (Label) privateChatMessageList.lookup("#messageLabel");
        Label userNameLabel = (Label) privateChatMessageList.lookup("#userNameLabel");
        Assert.assertEquals(" Okay! ", messageLabel.getText());
        Assert.assertEquals("peter", userNameLabel.getText());


        //System.out.println("" + app.getBuilder().getPersonalUser().getServer().get(0)
        //        .getCategories().get(0).getChannel().get(0).getMessage().size());

    }
}
