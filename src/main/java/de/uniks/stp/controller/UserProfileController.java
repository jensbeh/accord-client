package de.uniks.stp.controller;

import de.uniks.stp.ServerEditor;
import de.uniks.stp.model.User;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class UserProfileController {
    private Parent view;
    private ServerEditor editor;
    private User model;
    private Label userName;

    public UserProfileController(User user, Parent view, ServerEditor editor) {
        this.view = view;
        this.editor = editor;
        this.model = user;
    }

    public void init(){
        // Load all view references
        userName = (Label) view.lookup("#username");
        userName.setText(model.getName());
    }
}
