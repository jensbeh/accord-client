package de.uniks.stp.controller.settings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
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
    private Button audioButton;
    private Button generalButton;
    private Button connectionButton;
    private Button blockedButton;

    private SubSetting subController;
    private TitleBarController titleBarController;
    private Button selectedButton;
    private Stage stage;

    public SettingsController(ModelBuilder builder, Parent view) {
        this.builder = builder;
        this.view = view;
    }

    public void init(Stage stage) {
        this.stage = stage;
        //init view
        // create titleBar
        HBox titleBarBox = (HBox) view.lookup("#titleBarBox");
        Parent titleBarView = null;
        try {
            titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBarView.fxml")), builder.getStageManager().getLangBundle());
        } catch (IOException e) {
            e.printStackTrace();
        }
        titleBarBox.getChildren().add(titleBarView);
        titleBarController = new TitleBarController(stage, titleBarView, builder);
        titleBarController.init();
        titleBarController.setTheme();
        titleBarController.setMaximizable(false);
        titleBarController.setTitle(builder.getStageManager().getLangBundle().getString("window_title_settings"));
        stage.setTitle(builder.getStageManager().getLangBundle().getString("window_title_settings"));

        root = (Pane) view.lookup("#root");
        this.settingsItems = (VBox) view.lookup("#settingsItems");
        this.settingsItems.getChildren().clear();
        this.settingsContainer = (VBox) view.lookup("#settingsContainer");
        this.settingsContainer.getChildren().clear();

        this.itemList = new ArrayList<>();

        // add categories
        generalButton = addItem("General");
        generalButton.setText("General");
        addAction(generalButton, "General");

        if (builder.getPersonalUser() != null) {
            Button notificationsButton = addItem("Notifications");
            notificationsButton.setText("Notification");
            addAction(notificationsButton, "Notifications");
        }

        if (builder.getPersonalUser() != null) {
            connectionButton = addItem("Connection");
            connectionButton.setText("Connection");
            addAction(connectionButton, "Connection");
        }

        audioButton = addItem("Audio");
        audioButton.setText("Audio Settings");
        addAction(audioButton, "Audio");

        if (builder.getPersonalUser() != null) {
            blockedButton = addItem("Blocked");
            blockedButton.setText("Blocked");
            addAction(blockedButton, "Blocked");
        }

        onLanguageChanged(); // needs to be called because new buttons added

        openSettings("General");
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
        button.getStyleClass().add("settingsButton");

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
            Parent settingsField = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/settings/Settings_" + fxmlName + ".fxml")), builder.getStageManager().getLangBundle());
            if (subController != null) {
                subController.stop();
            }
            switch (fxmlName) {
                case "General":
                    subController = new GeneralController(settingsField, builder);
                    subController.setup();
                    this.settingsContainer.getChildren().add(settingsField);
                    subController.init();
                    break;
                case "Notifications":
                    subController = new NotificationsController(settingsField, builder);
                    this.settingsContainer.getChildren().add(settingsField);
                    subController.init();
                    break;
                case "Connection":
                    subController = new ConnectionController(settingsField, builder);
                    this.settingsContainer.getChildren().add(settingsField);
                    subController.init();
                    break;
                case "Audio":
                    subController = new AudioController(settingsField, builder);
                    this.settingsContainer.getChildren().add(settingsField);
                    subController.init();
                    break;
                case "Blocked":
                    subController = new BlockController(settingsField, builder);
                    this.settingsContainer.getChildren().add(settingsField);
                    subController.init();
            }
            selectedButton((Button) view.lookup("#button_" + fxmlName));
        } catch (Exception e) {
            System.err.println("Error on showing Settings Field Screen");
            e.printStackTrace();
        }
    }

    private void selectedButton(Button b) {
        if (selectedButton != null) {
            selectedButton.setStyle("");
        }
        selectedButton = b;
        if (!builder.getTheme().equals("Bright")) {
            selectedButton.setStyle("-fx-background-color: #ff9999;-fx-text-fill: Black;");
        } else {
            selectedButton.setStyle("-fx-background-color: #7da6df;-fx-text-fill: Black;");
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = builder.getStageManager().getLangBundle();
        generalButton.setText(lang.getString("button.settings_general"));
        audioButton.setText(lang.getString("button.settings_audio"));
        titleBarController.setTitle(builder.getStageManager().getLangBundle().getString("window_title_settings"));
        stage.setTitle(builder.getStageManager().getLangBundle().getString("window_title_settings"));

        if (connectionButton != null) {
            connectionButton.setText(lang.getString("button.settings_connection"));
        }
        if (blockedButton != null) {
            blockedButton.setText(lang.getString("button.settings_blocked"));
        }
        for (Button button : itemList) {
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
        if (selectedButton != null) {
            selectedButton(selectedButton);
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/SettingsView.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/SettingsView.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }
}

