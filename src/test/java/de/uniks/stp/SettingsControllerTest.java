package de.uniks.stp;

import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

public class SettingsControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @Test
    public void changeLanguageLogin() {
        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button languageButton = lookup("#button_Language").query();
        clickOn(languageButton);
        Label label_langSelect = lookup("#label_langSelect").query();
        ComboBox<String> comboBox_langSelect = lookup("#comboBox_langSelect").query();

        clickOn(comboBox_langSelect);
        clickOn("Deutsch");
        Assert.assertEquals("Sprache", languageButton.getText());
        Assert.assertEquals("Sprache ausw\u00e4hlen:", label_langSelect.getText());

        clickOn(comboBox_langSelect);
        clickOn("English");
        Assert.assertEquals("Language", languageButton.getText());
        Assert.assertEquals("Select Language:", label_langSelect.getText());
    }

    @Test
    public void changeLanguageHomeScreen() throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("123");
        CheckBox rememberBox = lookup("#rememberMeCheckbox").query();
        rememberBox.setSelected(true);
        clickOn("#loginButton");
        Thread.sleep(500);

        Button settingsButton = lookup("#settingsButton").query();
        clickOn(settingsButton);
        Button languageButton = lookup("#button_Language").query();
        clickOn(languageButton);
        Label label_langSelect = lookup("#label_langSelect").query();
        ComboBox<String> comboBox_langSelect = lookup("#comboBox_langSelect").query();

        clickOn(comboBox_langSelect);
        clickOn("Deutsch");
        Assert.assertEquals("Sprache", languageButton.getText());
        Assert.assertEquals("Sprache ausw\u00e4hlen:", label_langSelect.getText());

        clickOn(comboBox_langSelect);
        clickOn("English");
        Assert.assertEquals("Language", languageButton.getText());
        Assert.assertEquals("Select Language:", label_langSelect.getText());
    }
}
