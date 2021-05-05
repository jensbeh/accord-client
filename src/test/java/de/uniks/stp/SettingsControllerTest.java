package de.uniks.stp;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

public class SettingsControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @Test
    public void changeLanguage() {
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
