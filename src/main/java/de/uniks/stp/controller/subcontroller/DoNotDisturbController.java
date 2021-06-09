package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;

import java.awt.event.MouseEvent;

public class DoNotDisturbController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private RadioButton doNotDisturbSelected;
    private RadioButton showNotifications;
    private RadioButton playSound;

    public DoNotDisturbController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }


    @Override
    public void init() {
        doNotDisturbSelected = (RadioButton) view.lookup("#doNotDisturbSelected");
        showNotifications = (RadioButton) view.lookup("#ShowNotifications");
        playSound = (RadioButton) view.lookup("#playSound");
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
    }
}
