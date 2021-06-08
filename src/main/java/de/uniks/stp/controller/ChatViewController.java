package de.uniks.stp.controller;

import de.uniks.stp.AlternateMessageListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerChannel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ResourceBundle;

public class ChatViewController {
    private static ModelBuilder builder;
    private static ServerChannel currentChannel;
    private Parent view;
    private static Button sendButton;
    private TextField messageTextField;
    private ListView<Message> messageList;
    private static ObservableList<Message> ob;
    private HBox messageBox;


    public ChatViewController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public ChatViewController(Parent view, ModelBuilder builder, ServerChannel currentChannel) {
        this.view = view;
        this.builder = builder;
        this.currentChannel = currentChannel;
    }

    public void init() {
        // Load all view references
        sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        sendButton.setOnAction(this::sendButtonClicked);
        this.messageBox = (HBox) view.lookup("#messageBox");
        messageBox.setHgrow(messageTextField, Priority.ALWAYS);
        //ListView with message as parameter and observableList
        messageList = (ListView<Message>) view.lookup("#messageListView");
        messageList.setStyle("-fx-background-color: grey;");
        messageList.setCellFactory(new AlternateMessageListCellFactory());
        ob = FXCollections.observableArrayList();
        this.messageList.setItems(ob);
        AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());

        messageTextField.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
    }

    /**
     * get Text from TextField and build message
     */
    private void sendButtonClicked(ActionEvent actionEvent) {
        //get Text from TextField and clear TextField after
        String textMessage = messageTextField.getText();
        if (textMessage.length() <= 700) {
            if (!textMessage.isEmpty()) {
                if (!HomeViewController.inServerChat) {
                    AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());
                    try {
                        if (builder.getPrivateChatWebSocketCLient() != null && PrivateViewController.getSelectedChat() != null)
                            builder.getPrivateChatWebSocketCLient().sendMessage(new JSONObject().put("channel", "private").put("to", PrivateViewController.getSelectedChat().getName()).put("message", textMessage).toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());
                    try {
                        if (builder.getServerChatWebSocketClient() != null && currentChannel != null)
                            builder.getServerChatWebSocketClient().sendMessage(new JSONObject().put("channel", currentChannel.getId()).put("message", textMessage).toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * insert new message in observableList
     */
    public static void printMessage(Message msg) {
        if (!HomeViewController.inServerChat) {
            if (PrivateViewController.getSelectedChat().getName().equals(msg.getPrivateChat().getName())) { // only print message when user is on correct chat channel
                Platform.runLater(() -> ob.add(msg));
            }
        } else {
            if (currentChannel.getId().equals(msg.getServerChannel().getId()))
                Platform.runLater(() -> ob.add(msg));
        }
    }

    public void clearMessageField() {
        this.messageTextField.setText("");
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (sendButton != null)
            sendButton.setText(lang.getString("button.send"));
    }

    public void stop() {
        sendButton.setOnAction(null);
    }
}
