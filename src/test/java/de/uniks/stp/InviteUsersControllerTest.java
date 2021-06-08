package de.uniks.stp;

import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

public class InviteUsersControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private static String testUserMainName;
    private static String testUserMainPw;


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

    //@Test
    public void openInviteUsersTest() throws InterruptedException {
        loginInitWithTempUser();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(2, this.listTargetWindows().size());
        String serverSettingsTitle = "";
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                serverSettingsTitle = ((Stage) object).getTitle();
                Assert.assertNotEquals("", serverSettingsTitle);
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }

        Thread.sleep(2000);
    }

    @Test
    public void changeInviteUsersSubViewTest() throws InterruptedException {
        loginInitWithTempUser();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        RadioButton temp = lookup("#tempSelected").query();
        Assert.assertTrue(temp.isSelected());
        clickOn("#userLimitSelected");
        Assert.assertFalse(temp.isSelected());
        // check create and delete userLimitLink
        Label label = lookup("#userLimit").query();
        Assert.assertEquals("User Limit", label.getText());
        TextField userLimit = lookup("#maxUsers").query();
        userLimit.setText("1");
        int count = 0;
        count = Integer.parseInt(userLimit.getText());
        Assert.assertEquals(count, 1);
        clickOn("#createLink");
        WaitForAsyncUtils.waitForFxEvents();
        TextField link = lookup("#linkTextField").query();
        ComboBox<List<String>> links = lookup("#LinkComboBox").query();
        String certainLink = "";
        for (String s : links.getItems().get(0)) {
            if (s.equals(link.getText())) {
                certainLink = s;
                break;
            }
        }
        Assert.assertNotEquals("", certainLink);

        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#userLimitSelected");
        Platform.runLater(() -> links.getSelectionModel().select(0));

        clickOn(links);
        moveBy(0, 25);
        write("\n");
        clickOn("#deleteLink");
        String checkDel = "";
        for (List<String> s : links.getItems()) {
            if (s.get(0).equals(link.getText())) {
                checkDel = s.get(0);
                break;
            }
        }
        Assert.assertEquals("", checkDel);
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        Thread.sleep(2000);
    }


    //@Test
    public void generateAndDeleteTempLink() throws InterruptedException {
        loginInitWithTempUser();

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 50);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#createLink");
        WaitForAsyncUtils.waitForFxEvents();
        TextField link = lookup("#linkTextField").query();
        ComboBox<String> links = lookup("#LinkComboBox").query();
        String certainLink = "";
        for (String s : links.getItems()) {
            if (s.equals(link.getText())) {
                certainLink = s;
                break;
            }
        }
        Assert.assertNotEquals("", certainLink);
        clickOn(links);
        moveBy(0, 25);
        write("\n");
        clickOn("#deleteLink");
        String checkDel = "";
        for (String s : links.getItems()) {
            if (s.equals(link.getText())) {
                checkDel = s;
                break;
            }
        }
        Assert.assertEquals("", checkDel);
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        clickOn("#logoutButton");
        Thread.sleep(2000);
    }
}
