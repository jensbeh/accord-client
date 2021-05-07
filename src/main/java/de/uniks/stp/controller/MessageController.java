package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class MessageController {
    private ModelBuilder builder;
    private Parent view;
    private Message model;
    private Label userName;
    private Label message;

    public MessageController(Message message, Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
        this.model = message;
    }

    public void init() {
        // Load all view references
        userName = (Label) view.lookup("#username");
        message = (Label) view.lookup("#message");

        userName.setText(model.getFrom() + ": ");
        message.setText(model.getMessage());
    }
}
