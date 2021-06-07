package de.uniks.stp;

import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;

public class ServerSettingsPrivilegeControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private static String testUserMainName;
    private static String testUserMainPw;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserOne_UserKey;
    private static String testServerId;
    private ArrayList<User> privileged = new ArrayList<>();
    private Server currentServer;

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

    //@Test
    public void openServerSettingsPrivilegeTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);





        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();

        for (Server server : serverListView.getItems()) {
            if (server.getId().equals(testServerId)) {
                currentServer = server;
            }
        }

        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");
        clickOn("#privilege");

        RadioButton privilegeOn = lookup("#Privilege_On_Button").query();
        RadioButton privilegeOff = lookup("#Privilege_Off_Button").query();
        HBox privilegeOnView = lookup("#Privilege_On").query();

        ComboBox<Categories> categoryChoice = lookup("#Category").query();
        ComboBox<Channel> channelChoice = lookup("#Channels").query();

        Assert.assertEquals(categoryChoice.getItems(), currentServer.getCategories());

        clickOn(categoryChoice);
        interact(() -> {
            categoryChoice.getSelectionModel().select(0);
        });
        Assert.assertEquals(channelChoice.getItems(), currentServer.getCategories().get(0).getChannel());

        clickOn(channelChoice);
        interact(() -> {
            channelChoice.getSelectionModel().select(0);
        });

        clickOn(privilegeOn);
        Assert.assertTrue(privilegeOn.isSelected());
        Assert.assertFalse(privilegeOff.isSelected());
        Assert.assertTrue(privilegeOnView.getChildren().isEmpty());

        for (User user : currentServer.getUser()) {
            if (user.getId().equals(currentServer.getOwner())) {
                privileged.add(user);
            }
        }

        User test = new User().setName("Test").setId("1");
        currentServer.withUser(test);

        clickOn("#Change_Privilege");
        Assert.assertTrue(currentServer.getCategories().get(0).getChannel().get(0).isPrivilege());
        Assert.assertFalse(privilegeOnView.getChildren().isEmpty());
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(0).getPrivilegedUsers());

        ComboBox<String> addMenu = lookup("#Add_User_to_Privilege").query();
        ComboBox<String> removeMenu = lookup("#Remove_User_from_Privilege").query();

        clickOn(addMenu);
        interact(() -> {
            addMenu.getSelectionModel().select(0);
        });
        for (User user : currentServer.getUser()) {
            if (user.getName().equals(addMenu.getSelectionModel().getSelectedItem())) {
                privileged.add(user);
            }
        }
        clickOn("#User_to_Privilege");
        WaitForAsyncUtils.waitForFxEvents();
        System.out.println(currentServer.getCategories().get(0).getChannel());
        Assert.assertEquals(currentServer.getUser().size() - 2, addMenu.getItems().size());
        Assert.assertEquals(currentServer.getCategories().get(0).getChannel().get(0).getPrivilegedUsers().size(), removeMenu.getItems().size()); //TODO fails here
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(0).getPrivilegedUsers());

        clickOn(removeMenu);
        interact(() -> {
            removeMenu.getSelectionModel().select(0);
        });
        for (User user : currentServer.getUser()) {
            if (user.getName().equals(removeMenu.getSelectionModel().getSelectedItem())) {
                privileged.remove(user);
            }
        }
        clickOn("#User_from_Privilege");
        Assert.assertEquals(currentServer.getUser().size() - 1, addMenu.getItems().size());
        Assert.assertEquals(currentServer.getCategories().get(0).getChannel().get(0).getPrivilegedUsers().size(), removeMenu.getItems().size());
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(0).getPrivilegedUsers());


        clickOn(removeMenu);
        interact(() -> {
            removeMenu.getSelectionModel().select(0);
        });
        for (User user : currentServer.getUser()) {
            if (user.getName().equals(removeMenu.getSelectionModel().getSelectedItem())) {
                privileged.remove(user);
            }
        }
        clickOn("#User_from_Privilege");
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(0).getPrivilegedUsers());
        Assert.assertTrue(privilegeOnView.getChildren().isEmpty());
        Assert.assertTrue(privilegeOff.isSelected());
        Assert.assertFalse(privilegeOn.isSelected());
        Assert.assertFalse(currentServer.getCategories().get(0).getChannel().get(0).isPrivilege());


        clickOn(privilegeOn);
        clickOn("#Change_Privilege");

        clickOn(privilegeOff);
        Assert.assertTrue(privilegeOff.isSelected());
        Assert.assertFalse(privilegeOn.isSelected());
        Assert.assertTrue(privilegeOnView.getChildren().isEmpty());
        Assert.assertTrue(currentServer.getCategories().get(0).getChannel().get(0).isPrivilege());

        clickOn("#Change_Privilege");
        Assert.assertFalse(currentServer.getCategories().get(0).getChannel().get(0).isPrivilege());
        Assert.assertEquals(privileged, currentServer.getCategories().get(0).getChannel().get(0).getPrivilegedUsers());

    }
}
