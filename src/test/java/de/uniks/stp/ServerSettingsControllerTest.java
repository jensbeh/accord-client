package de.uniks.stp;

import de.uniks.stp.model.Server;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.service.query.PointQuery;
import org.testfx.util.NodeQueryUtils;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ServerSettingsControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
    }

    @Override
    public void start(Stage stage) {
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
    public void openServerSettingsTest() throws InterruptedException {
        login();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#server"));
        clickOn("#serverMenuButton");
        //"hillbillyEngineering
        moveBy(0,25);
        write("\n");
        Assert.assertNotEquals(1,this.listTargetWindows().size());
        String serverSettingsTitle ="";
        for (Object object : this.listTargetWindows()){
            if (!((Stage) object).getTitle().equals("Accord - Main")){
                serverSettingsTitle = ((Stage) object).getTitle();
                break;
            }
        }
        Assert.assertNotEquals("",serverSettingsTitle);
        closeCurrentWindow();   // TODO talk with maurice about logging out in Test
        clickOn("#logoutButton");
    }
}
