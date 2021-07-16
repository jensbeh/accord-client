package de.uniks.stp.controller.settings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.util.ResourceManager;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.util.ResourceBundle;

public class DoNotDisturbController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private CheckBox doNotDisturbSelected;
    private CheckBox showNotifications;
    private CheckBox playSound;
    private Slider volume;
    private Label volumeLabel;

    public DoNotDisturbController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }


    @Override
    public void init() {
        doNotDisturbSelected = (CheckBox) view.lookup("#doNotDisturbSelected");
        doNotDisturbSelected.setSelected(builder.isDoNotDisturb());
        showNotifications = (CheckBox) view.lookup("#ShowNotifications");
        showNotifications.setSelected(builder.isShowNotifications());
        volume = (Slider) view.lookup("#volume");
        volumeLabel = (Label) view.lookup("#volumeLabel");
        playSound = (CheckBox) view.lookup("#playSound");
        playSound.setSelected(builder.isPlaySound());
        checkIfDoNotDisturbIsSelected(null);

        doNotDisturbSelected.setOnAction(this::checkIfDoNotDisturbIsSelected);
        showNotifications.setOnAction(this::updateSettings);
        playSound.setOnAction(this::updateSettings);

        System.out.println("Volume: " + ResourceManager.getVolume(builder.getPersonalUser().getName()));
        volume.setMin(-80.0);
        volume.setMax(6.0206);
        volume.setValue(ResourceManager.getVolume(builder.getPersonalUser().getName()));
        volume.valueProperty().addListener((observable, oldValue, newValue) -> builder.setVolume(newValue.floatValue()));
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
    }

    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        doNotDisturbSelected.setText(lang.getString("checkbox.dnd"));
        showNotifications.setText(lang.getString(("checkbox.show_notifications")));
        playSound.setText(lang.getString("checkbox.play_sound"));
        volumeLabel.setText(lang.getString("slider.volume"));
    }

    @Override
    public void stop() {
        super.stop();
        doNotDisturbSelected.setOnAction(null);
        showNotifications.setOnAction(null);
        playSound.setOnAction(null);
    }
}
