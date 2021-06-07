package de.uniks.stp;

import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class SnakeControllerTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private static String testUserMainName;
    private static String testUserMainPw;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "false");
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

    public void loginInit() throws InterruptedException {
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

    @Test
    public void openStartGameViewTest() throws InterruptedException {
        loginInit();

        // clicks 15 times on home
        Circle homeButton = lookup("#homeButton").query();
        for (int i = 0; i < 15; i++) {
            clickOn(homeButton);
        }

        // check if title is correct
        boolean found = false;
        for (Object object : this.listTargetWindows()) {
            if (((Stage) object).getTitle().equals("Snake")) {
                Stage snake = (Stage) object;
                found = true;
            }
        }
        if (!found) {
            Assert.fail();
        }

        // close start Snake view
        for (Object object : this.listTargetWindows()) {
            if (((Stage) object).getTitle().equals("Snake")) {
                Platform.runLater(((Stage) object)::close);
                Thread.sleep(2000);
                break;
            }
        }
    }
}
