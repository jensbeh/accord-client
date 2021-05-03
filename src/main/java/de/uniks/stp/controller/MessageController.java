package de.uniks.stp.controller;

import de.uniks.stp.ServerEditor;
import de.uniks.stp.model.Message;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class MessageController {
    private Parent view;
    private ServerEditor editor;
    private Message model;
    private Label userName;
    private Label message;

    public MessageController(Message message, Parent view, ServerEditor editor) {
        this.view = view;
        this.editor = editor;
        this.model = message;
    }

    public void init() {
        // Load all view references
        userName = (Label) view.lookup("#username");
        message = (Label) view.lookup("#message");

        userName.setText(model.getFrom()+": ");
        message.setText(model.getMessage());
    }
}
