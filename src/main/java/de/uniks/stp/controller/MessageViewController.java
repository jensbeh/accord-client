package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.io.IOException;

public class MessageViewController {
    private VBox root;
    private final Parent view;
    private ModelBuilder builder;
    private VBox messages;
    private HBox messageBar;
    private TextField messageField;
    private Button messageButton;

    public MessageViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        root = (VBox) view.lookup("#root");
        messages = (VBox) view.lookup("#messages");
        messageBar = (HBox) view.lookup("#messagebar");
        messageBar.setOpacity(0);
        messageField = (TextField) view.lookup("#messageField");
        messageButton = (Button) view.lookup("#messageButton");
        messageButton.setOnAction(this::onSendClicked);
        root.setPrefWidth(600);
        MessageView();
    }

    public void MessageView() {
        // Clean Message View
        this.messages.getChildren().clear();
        // Enable Message Bar
        messageBar.setOpacity(1);
        for (Message msg : PrivateViewController.getSelectedChat().getMessage()) {
            // Display each Message which are saved
            ChatViewController.printMessage(msg);
        }
    }

    /**
     * Clicking send Button sends a Message to the selected User
     *
     * @param actionEvent is called when clicked on the send Button
     */
    private void onSendClicked(ActionEvent actionEvent) {
        if (!messageField.getText().equals("")) {
            try {
                builder.getPrivateChatWebSocketCLient().sendMessage(new JSONObject().put("channel", "private").put("to", PrivateViewController.getSelectedChat().getName()).put("message", messageField.getText()).toString());
                messageField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
