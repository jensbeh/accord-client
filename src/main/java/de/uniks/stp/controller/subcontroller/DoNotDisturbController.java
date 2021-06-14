package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;

import java.awt.event.MouseEvent;

public class DoNotDisturbController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private CheckBox doNotDisturbSelected;
    private CheckBox showNotifications;
    private CheckBox playSound;

    public DoNotDisturbController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }


    @Override
    public void init() {
        doNotDisturbSelected = (CheckBox) view.lookup("#doNotDisturbSelected");
        showNotifications = (CheckBox) view.lookup("#ShowNotifications");
        playSound = (CheckBox) view.lookup("#playSound");
        checkIfDoNotDisturbIsSelected();

        doNotDisturbSelected.setOnAction(this::updateDoNotDisturbIsSelected);
    }

    private void updateDoNotDisturbIsSelected(ActionEvent actionEvent) {
        builder.getPersonalUser().setDoNotDisturb(doNotDisturbSelected.isSelected());
        checkIfDoNotDisturbIsSelected();
    }

    private void checkIfDoNotDisturbIsSelected() {
        if (builder.getPersonalUser().isDoNotDisturb()) {
            showNotifications.setDisable(false);
            playSound.setDisable(false);
        } else {
            showNotifications.setDisable(true);
            playSound.setDisable(true);
        }
        updateSettings();
    }

    private void updateSettings() {
        builder.setDoNotDisturb(doNotDisturbSelected.isSelected());
        builder.setShowNotifications(showNotifications.isSelected());
        builder.setPlaySound(playSound.isSelected());
        builder.saveSettings();
    }
}
