package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;

public class DoNotDisturbController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private CheckBox doNotDisturbSelected;
    private CheckBox showNotifications;
    private CheckBox playSound;
    private Slider volume;

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
        playSound = (CheckBox) view.lookup("#playSound");
        playSound.setSelected(builder.isPlaySound());
        checkIfDoNotDisturbIsSelected(null);

        doNotDisturbSelected.setOnAction(this::checkIfDoNotDisturbIsSelected);
        showNotifications.setOnAction(this::updateSettings);
        playSound.setOnAction(this::updateSettings);


        volume.valueProperty().addListener((observable, oldValue, newValue) -> builder.setVolume(newValue.floatValue()));
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

    @Override
    public void stop() {
        super.stop();
        doNotDisturbSelected.setOnAction(null);
        showNotifications.setOnAction(null);
        playSound.setOnAction(null);
    }
}
