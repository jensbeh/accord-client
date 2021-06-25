package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.subcontroller.CustomNotificationsController;
import de.uniks.stp.controller.subcontroller.DoNotDisturbController;
import de.uniks.stp.controller.subcontroller.LanguageController;
import de.uniks.stp.controller.subcontroller.SubSetting;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * The class SettingsController controls the view in Settings
 */
public class SettingsController {
    private final ModelBuilder builder;
    private final Parent view;
    private VBox settingsItems;
    private VBox settingsContainer;
    private List<Button> itemList;
    private static Button languageButton;

    private SubSetting subController;

    /**
     * First check if there is a settings file already in user local directory - if not, create
     */
    public static void setup() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        Constants.APPDIR_ACCORD_PATH = appDirs.getUserConfigDir("Accord", null, null);

        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;

        Properties prop = new Properties();
        File file = new File(path_to_config + Constants.SETTINGS_FILE);
        File dir = new File(path_to_config);
        if (!file.exists()) {
            try {
                dir.mkdirs();
                if (file.createNewFile()) {
                    FileOutputStream op = new FileOutputStream(path_to_config + Constants.SETTINGS_FILE);
                    prop.setProperty("LANGUAGE", "en");
                    prop.store(op, null);
                }
            } catch (Exception e) {
                System.out.println(e+"");
                e.printStackTrace();
            }
        }

        LanguageController.setup();
    }

    public SettingsController(ModelBuilder builder, Parent view) {
        this.builder = builder;
        this.view = view;
    }

    public void init() {
        //init view
        this.settingsItems = (VBox) view.lookup("#settingsItems");
        this.settingsItems.getChildren().clear();
        this.settingsContainer = (VBox) view.lookup("#settingsContainer");
        this.settingsContainer.getChildren().clear();

        this.itemList = new ArrayList<>();

        // add categories
        languageButton = addItem("Language");
        addAction(languageButton, "Language");
        if (builder.getPersonalUser() != null) {
            Button doNotDisturbButton = addItem("DnD");
            doNotDisturbButton.setText("Do Not Disturb");
            addAction(doNotDisturbButton, "DoNotDisturb");
            Button customNotifications = addItem("CN");
            customNotifications.setText("Custom Notifications");
            addAction(customNotifications, "CustomNotifications");
        }

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
            Parent settingsField = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("settings/Settings_" + fxmlName + ".fxml")), StageManager.getLangBundle());

            switch (fxmlName) {
                case "Language":
                    subController = new LanguageController(settingsField);
                    subController.init();
                    break;
                case "DoNotDisturb":
                    subController = new DoNotDisturbController(settingsField, builder);
                    subController.init();
                    break;
                case "CustomNotifications":
                    subController = new CustomNotificationsController(settingsField, builder);
                    subController.init();
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
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        languageButton.setText(lang.getString("button.Language"));
    }
}

