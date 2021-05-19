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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ChatViewController {
    private ModelBuilder builder;
    private Parent view;
    private Button sendButton;
    private TextField messageTextField;
    private ListView<Message> messageList;
    private static ObservableList<Message> ob;
    private HBox messageBox;


    public ChatViewController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        // Load all view references
        this.sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        this.sendButton.setOnAction(this::sendButtonClicked);
        this.messageBox = (HBox) view.lookup("#messageBox");
        messageBox.setHgrow(messageTextField, Priority.ALWAYS);
        //ListView with message as parameter and observableList
        messageList = (ListView<Message>) view.lookup("#messageListView");
        messageList.setStyle("-fx-background-color: grey;");
        messageList.setCellFactory(new AlternateMessageListCellFactory());
        ob = FXCollections.observableArrayList();
        this.messageList.setItems(ob);
    }

    /**
     * get Text from TextField and build message
     */
    private void sendButtonClicked(ActionEvent actionEvent) {
        //get Text from TextField and clear TextField after
        String textMessage = messageTextField.getText();
        messageTextField.setText("");
        if (!textMessage.isEmpty()) {
            //build Message
            Message message = new Message();
            message.setMessage(textMessage);
            message.setFrom(builder.getPersonalUser().getName());
            AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());
            printMessage(message);
        }
    }

    /**
     * insert new message in observableList
     */
    public static void printMessage(Message msg) {
        ob.add(msg);
    }
}