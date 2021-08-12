package de.uniks.stp.controller.settings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.util.ResourceManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.uniks.stp.util.Constants.*;

public class NotificationsController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private CheckBox doNotDisturbSelected;
    private CheckBox showNotifications;
    private CheckBox playSound;
    private Label volumeLabel;
    private ComboBox<String> customSoundComboBox;
    private Button addButton;
    private Button deleteButton;
    private List<File> files;
    private List<String> fileNames;
    private List<String> addedFiles;
    private ObservableList<File> ob;
    private InputStream stream;
    private Label selectedSound;

    public NotificationsController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }


    @Override
    public void init() {
        doNotDisturbSelected = (CheckBox) view.lookup("#doNotDisturbSelected");
        doNotDisturbSelected.setSelected(builder.isDoNotDisturb());
        showNotifications = (CheckBox) view.lookup("#ShowNotifications");
        showNotifications.setSelected(builder.isShowNotifications());
        Slider volume = (Slider) view.lookup("#volume");
        volumeLabel = (Label) view.lookup("#volumeLabel");
        playSound = (CheckBox) view.lookup("#playSound");
        playSound.setSelected(builder.isPlaySound());
        checkIfDoNotDisturbIsSelected(null);

        doNotDisturbSelected.setOnAction(this::checkIfDoNotDisturbIsSelected);
        showNotifications.setOnAction(this::updateSettings);
        playSound.setOnAction(this::updateSettings);

        volume.setMin(-80.0);
        volume.setMax(6.0206);
        volume.setValue(ResourceManager.getVolume(builder.getPersonalUser().getName()));
        volume.valueProperty().addListener((observable, oldValue, newValue) -> {
            builder.setVolume(newValue.floatValue());
            builder.playSound();
        });
        customSoundComboBox = (ComboBox<String>) view.lookup("#comboBox");
        addButton = (Button) view.lookup("#add");
        deleteButton = (Button) view.lookup("#delete");
        selectedSound = (Label) view.lookup("#selectedSound");
        if (addedFiles == null) {
            addedFiles = new ArrayList<>();
        }
        files = new ArrayList<>();
        addButton.setOnAction(this::add);
        deleteButton.setOnAction(this::delete);
        fileNames = new ArrayList<>();
        ob = FXCollections.observableList(ResourceManager.getNotificationSoundFiles());
        if (!ResourceManager.getComboValue(builder.getPersonalUser().getName()).equals("")) {
            customSoundComboBox.setPromptText(ResourceManager.getComboValue(builder.getPersonalUser().getName()));
        }
        deleteButton.setDisable(!customSoundComboBox.getPromptText().equals("default"));
        for (File file : ob) {
            String fileName = file.getName().substring(0, file.getName().length() - 4);
            fileNames.add(fileName);
            files.add(file);
            customSoundComboBox.getItems().add(fileName);
        }
        customSoundComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            deleteButton.setDisable(newValue.equals("default"));
            customSoundComboBox.setPromptText(newValue);
            ResourceManager.setComboValue(builder.getPersonalUser().getName(), newValue);
            for (File file : files) {
                String fileName = file.getName().substring(0, file.getName().length() - 4);
                fileNames.add(fileName);
                if (fileName.equals(newValue)) {
                    try {
                        stream = new FileInputStream(file);
                        URL url = file.toURI().toURL();
                        builder.setSoundFile(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        onLanguageChanged();
    }

    private void checkIfDoNotDisturbIsSelected(ActionEvent actionEvent) {
        if (!doNotDisturbSelected.isSelected()) {
            showNotifications.setDisable(false);
            playSound.setDisable(false);
        } else {
            showNotifications.setDisable(true);
            playSound.setDisable(true);
        }
        updateSettings(null);
    }

    private void updateSettings(ActionEvent actionEvent) {
        builder.setDoNotDisturb(doNotDisturbSelected.isSelected());
        builder.setShowNotifications(showNotifications.isSelected());
        builder.setPlaySound(playSound.isSelected());
        builder.saveSettings();
        builder.getUserProfileController().showHideDoNotDisturb();
    }

    private void delete(ActionEvent actionEvent) {
        if (stream != null) {
            cleanUp();
        }
        if (customSoundComboBox.getValue() != null) {
            String newValue = customSoundComboBox.getValue();
            ResourceManager.deleteNotificationSound(newValue);
            fileNames.remove(newValue);
            customSoundComboBox.getItems().remove(newValue);
            customSoundComboBox.setPromptText("saved sounds");
        }
    }

    private void add(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("WAV Documents", "*.wav"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null && !fileNames.contains(selectedFile.getName().substring(0, selectedFile.getName().length() - 4))) {
            String fileName = selectedFile.getName().substring(0, selectedFile.getName().length() - 4);
            customSoundComboBox.setPromptText(fileName);
            files.add(selectedFile);
            fileNames.add(selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
            customSoundComboBox.getItems().add(selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
            File file = new File(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH + "/" + selectedFile.getName());
            ResourceManager.saveNotifications(selectedFile);
            ob.add(file);
            ResourceManager.setComboValue(builder.getPersonalUser().getName(),
                    selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
        } else {
            System.out.println("File is not valid!");
        }
    }

    public void onLanguageChanged() {
        ResourceBundle lang = builder.getStageManager().getLangBundle();
        addButton.setText(lang.getString("button.CN_add"));
        deleteButton.setText(lang.getString("button.CN_delete"));
        selectedSound.setText(lang.getString("label.CN_selected_sound"));
        customSoundComboBox.setPromptText(lang.getString("comboBox.CN_saved_sounds"));
        doNotDisturbSelected.setText(lang.getString("checkbox.dnd"));
        showNotifications.setText(lang.getString(("checkbox.show_notifications")));
        playSound.setText(lang.getString("checkbox.play_sound"));
        volumeLabel.setText(lang.getString("slider.volume"));
    }

    public void cleanUp() {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        super.stop();
        doNotDisturbSelected.setOnAction(null);
        showNotifications.setOnAction(null);
        playSound.setOnAction(null);
    }

}
