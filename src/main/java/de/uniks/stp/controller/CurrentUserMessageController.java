package de.uniks.stp.controller;

import de.uniks.stp.model.Message;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class CurrentUserMessageController {
    private Parent view;
    private Message message;
    private Label userNameLabel;
    private Label messageLabel;

    public CurrentUserMessageController(Message msg, Parent view) {
        this.view = view;
        this.message = msg;
    }

    public void init() {
        // Load all view references
        this.userNameLabel = (Label) view.lookup("#cUserName");
        this.messageLabel = (Label) view.lookup("#cUserMessage");

        userNameLabel.setText(message.getFrom());
        messageLabel.setText(message.getMessage());
    }
}
