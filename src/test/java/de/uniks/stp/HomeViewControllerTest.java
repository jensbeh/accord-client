package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;


public class HomeViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @Override
    public void start(Stage stage) {
        //start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }

    public void login() {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Peter Lustig");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        clickOn("#loginButton");
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
        ListView<Channel> privateChatlist = lookup("#privateChatScrollpane").lookup("#privateChatList").query();
        Assert.assertEquals(userList.getItems().get(0).getName(), privateChatlist.getItems().get(0).getName());
    }
}
