package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MessageViewController {
    private VBox root;
    private final Parent view;
    private ModelBuilder builder;
    private VBox messages;
    private HBox messageBar;

    public MessageViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        root = (VBox) view.lookup("#root");
        messages = (VBox) view.lookup("#messages");
        messageBar = (HBox) view.lookup("#messagebar");
        messageBar.setOpacity(0);
        root.setPrefWidth(600);
        MessageView();
    }

    private void MessageView() {
        // Clean Message View
        this.messages.getChildren().clear();
        // Enable Message Bar
        messageBar.setOpacity(1);
        for (Message msg : PrivateViewController.getSelectedChat().getMessage()) {
            // Display each Message which are saved
            //ChatViewController.printMessage(msg);
        }
    }

}
