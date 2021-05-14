package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatViewController {
    private ModelBuilder builder;
    private Parent view;
    private Button sendButton;
    private TextField messageTextField;
    private ListView<Parent> messageList;
    private static ObservableList<Parent> ob;
    private VBox topVbox;


    public ChatViewController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        // Load all view references
        this.sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        this.topVbox = (VBox) view.lookup("#topVbox");
        this.sendButton.setOnAction(this::sendButtonClicked);
        messageList = (ListView<Parent>) view.lookup("#messageListView");
        messageList.setStyle("-fx-control-inner-background: grey;");
        ob = FXCollections.observableArrayList();
        messageList.setItems(ob);
    }

    private void sendButtonClicked(ActionEvent actionEvent) {
        String textMessage = messageTextField.getText();
        messageTextField.setText("");
        Message message = new Message();
        message.setMessage(textMessage);
        message.setFrom(builder.getPersonalUser().getName());
        loadCurrentUserMessageFxml(message);
    }

    private void loadCurrentUserMessageFxml(Message msg) {
        try {
            Parent view = FXMLLoader.load(StageManager.class.getResource("CurrentUserTextMessage.fxml"));
            CurrentUserMessageController messageController = new CurrentUserMessageController(msg, view, builder);
            messageController.init();
            ob.add(view);
            this.messageList.setItems(ob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
