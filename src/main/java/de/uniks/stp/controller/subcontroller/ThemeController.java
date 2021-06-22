package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;

public class ThemeController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private ComboBox<String> themeSelector;

    public ThemeController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    @SuppressWarnings("unchecked")
    public void init() {
        this.themeSelector = (ComboBox<String>) view.lookup("#comboBox_themeSelect");
        this.themeSelector.getItems().add("Dark");
        this.themeSelector.getItems().add("Bright");
        themeSelector.setOnAction(this::onThemeChanged);
    }


    /**
     * when the user changes the language from the comboBox then switch application language and save into user local settings
     *
     * @param actionEvent the mouse click event
     */
    private void onThemeChanged(ActionEvent actionEvent) {
        // get selected language and change
        String selectedTheme = this.themeSelector.getValue();
        if (selectedTheme.equals("Bright")) {
            StageManager.setWhiteMode();
        } else {
            StageManager.setDarkMode();
        }
        builder.setTheme(selectedTheme);
    }

    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        themeSelector.setOnAction(null);
    }
}
