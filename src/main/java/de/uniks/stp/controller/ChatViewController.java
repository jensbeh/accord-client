package de.uniks.stp.controller;

import de.uniks.stp.AlternateMessageListCellFactory;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatViewController {
    private ModelBuilder builder;
    private Parent view;
    private Button sendButton;
    private TextField messageTextField;
    private ListView<Message> messageList;
    private static ObservableList<Message> ob;


    public ChatViewController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        // Load all view references
        this.sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        this.sendButton.setOnAction(this::sendButtonClicked);
        //ListView with message as parameter and observableList
        messageList = (ListView<Message>) view.lookup("#messageListView");
        messageList.setStyle("-fx-background-color: grey;");
        messageList.setCellFactory(new AlternateMessageListCellFactory());
        ob = FXCollections.observableArrayList();
        this.messageList.setItems(ob);
    }

    private void sendButtonClicked(ActionEvent actionEvent) {
        //get Text from TextField and clear TextField after
        String textMessage = messageTextField.getText();
        messageTextField.setText("");
        if (!textMessage.isEmpty()) {
            //build Message
            Message message = new Message();
            message.setMessage(textMessage);
            message.setFrom(builder.getPersonalUser().getName());
            printMessage(message);
        }
    }


    private static void printMessage(Message msg) {
        //insert new message in observableList
        ob.add(msg);
    }
}
