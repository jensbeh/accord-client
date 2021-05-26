package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import javafx.scene.Parent;

public class ServerSubSettingsPrivilegeController {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;

    public ServerSubSettingsPrivilegeController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
    }

    public static void init() {

    }
}
