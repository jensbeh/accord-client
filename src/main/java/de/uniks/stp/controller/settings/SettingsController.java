package de.uniks.stp.controller.settings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The class SettingsController controls the view in Settings
 */
public class SettingsController {
    private final ModelBuilder builder;
    private final Parent view;
    private Pane root;
    private VBox settingsItems;
    private VBox settingsContainer;
    private List<Button> itemList;
    private Button languageButton;
    private Button themeButton;

    private SubSetting subController;

    public SettingsController(ModelBuilder builder, Parent view) {
        this.builder = builder;
        this.view = view;
    }

    public void init() {
        //init view
        root = (Pane) view.lookup("#root");
        this.settingsItems = (VBox) view.lookup("#settingsItems");
        this.settingsItems.getChildren().clear();
        this.settingsContainer = (VBox) view.lookup("#settingsContainer");
        this.settingsContainer.getChildren().clear();

        this.itemList = new ArrayList<>();

        // add categories
        languageButton = addItem("Language");
        addAction(languageButton, "Language");



        if (builder.getPersonalUser() != null) {
            Button notificationsButton = addItem("Notifications");
            notificationsButton.setText("Notification");
            addAction(notificationsButton, "Notifications");
        }

        themeButton = addItem("Theme");
        themeButton.setText("Dark/Bright - Mode");
        addAction(themeButton, "Theme");

        onLanguageChanged(); // needs to be called because new buttons added
    }

    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        if (subController != null) {
            subController.stop();
            subController = null;
        }

        for (Button b : this.itemList) {
            b.setOnAction(null);
        }
    }

    /**
     * create a new button and add into list (view)
     *
     * @param buttonName the button name to set the id
     * @return the new button
     */
    public Button addItem(String buttonName) {
        Button button = new Button();
        button.setPrefWidth(198);
        button.setPrefHeight(32);
        button.setId("button_" + buttonName);

        this.itemList.add(button);
        this.settingsItems.getChildren().add(button);

        return button;
    }

    /**
     * add action for a button / functionality
     *
     * @param button   the given button to add action
     * @param viewName the fxml sub name
     */
    public void addAction(Button button, String viewName) {
        button.setOnAction(event -> openSettings(viewName));
    }

    /**
     * load / open the sub setting on the right field
     *
     * @param fxmlName the fxml sub name
     */
    public void openSettings(String fxmlName) {
        // stop current subController
        if (subController != null) {
            subController.stop();
        }

        // clear old and load new subSetting view
        try {
            this.settingsContainer.getChildren().clear();
            Parent settingsField = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/settings/Settings_" + fxmlName + ".fxml")), StageManager.getLangBundle());

            switch (fxmlName) {
                case "Language":
                    subController = new LanguageController(settingsField);
                    subController.setup();
                    subController.init();
                    break;
                case "Notifications":
                    subController = new NotificationsController(settingsField, builder);
                    subController.init();
                    break;
                case "Theme":
                    subController = new ThemeController(settingsField, builder);
                    subController.init();
                    break;
            }
            this.settingsContainer.getChildren().add(settingsField);
        } catch (Exception e) {
            System.err.println("Error on showing Settings Field Screen");
            e.printStackTrace();
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        languageButton.setText(lang.getString("button.Language"));
        themeButton.setText(lang.getString("button.DB_mode"));
        for (Button button: itemList) {
            button.getId();
            switch (button.getId()) {
                case "button_Notifications":
                    button.setText(lang.getString("button.notifications"));
                    break;
                case "button_CN":
                    button.setText(lang.getString("button.custom_notification"));
                    break;

            }
        }
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/SettingsView.css")).toExternalForm());
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/SettingsView.css")).toExternalForm());
    }
}

