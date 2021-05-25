package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;

public class ServerSettingsChannelController extends SubSetting {
    private Parent view;
    private ModelBuilder builder;
    private Server server;
    private ComboBox<String> categorySelector;

    public ServerSettingsChannelController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
    }

    public void init() {
        // init view
        this.categorySelector = (ComboBox<String>) view.lookup("#categorySelector");
        this.categorySelector.getItems().add("Test");
        this.categorySelector.getItems().add("Cool");
    }

    public void stop() {

    }
}
