package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.WebSocketClient;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.websocket.CloseReason;
import java.io.IOException;

public class PrivateViewControllerTest extends ApplicationTest {

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

    public void login() {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Peter Lustig");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        clickOn("#loginButton");
    }


    @Test
    public void noConnectionOnWebSocketTest() throws InterruptedException {
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        WebSocketClient ws = app.getBuilder().getPrivateChatWebSocketCLient();
        ws.onClose(ws.getSession(), new CloseReason(new CloseReason.CloseCode() {
            /**
             * Returns the code number, for example the integer '1000' for normal closure.
             *
             * @return the code number
             */
            @Override
            public int getCode() {
                return 1006;
            }
        }, "no Connection"));
        Thread.sleep(2000);
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
            }
        }
        Assert.assertEquals("No Connection Error", result);
    }

    @Test
    public void chatPartnerIsOffline() throws InterruptedException, IOException {
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        WebSocketClient ws = app.getBuilder().getPrivateChatWebSocketCLient();
        ws.sendMessage(new JSONObject().put("channel","private").put("to","-").put("message","Test").toString());
        Thread.sleep(2000);
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
            }
        }
        Assert.assertEquals("Chat Error", result);
    }

    @Test
    public void invalidUsername() throws InterruptedException, IOException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("+");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1");
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Thread.sleep(2000);
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
            }
        }
        Assert.assertEquals("Username Error", result);
    }
}
