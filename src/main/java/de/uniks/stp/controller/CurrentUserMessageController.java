package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class CurrentUserMessageController {
    private ModelBuilder builder;
    private Parent view;
    private Message message;
    private Label userNameLabel;
    private Label messageLabel;

    public CurrentUserMessageController(Message msg, Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
        this.message = msg;
    }

    public void init() {
        // Load all view references
        this.userNameLabel = (Label) view.lookup("#cUserName");
        this.messageLabel = (Label) view.lookup("#cUserMessage");

        userNameLabel.setText(builder.getPersonalUser().getName());
        messageLabel.setText(message.getMessage());
    }
}
