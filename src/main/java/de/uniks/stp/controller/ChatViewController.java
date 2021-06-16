package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.EmojiParser;
import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.AlternateMessageListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerChannel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ChatViewController {
    private static ModelBuilder builder;
    private static ServerChannel currentChannel;
    private Parent view;
    private static Button sendButton;
    private EmojiTextFlow messageTextField;
    private ListView<Message> messageList;
    private static ObservableList<Message> ob;
    private HBox messageBox;
    private ImageView imageView;
    private Button emojiButton;
    private VBox container;
    private StackPane stack;
    private TableView<String> emojiTable;


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
        EmojiTextFlowParameters emojiTextFlowParameters;
        {
            emojiTextFlowParameters = new EmojiTextFlowParameters();
            emojiTextFlowParameters.setEmojiScaleFactor(1D);
            emojiTextFlowParameters.setTextAlignment(TextAlignment.CENTER);
            emojiTextFlowParameters.setFont(Font.font("Verdana", FontWeight.BOLD, 5));
            emojiTextFlowParameters.setTextColor(Color.BLACK);
        }

        // Load all view references
        sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = ((EmojiTextFlow) view.lookup("#messageTextField"));
        sendButton.setOnAction(this::sendButtonClicked);
        this.messageBox = (HBox) view.lookup("#messageBox");
        imageView = (ImageView) view.lookup("#imageView");
        messageBox.setHgrow(messageTextField, Priority.ALWAYS);
        container = (VBox) view.lookup("#container");
        stack = (StackPane) view.lookup("#stack");
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

        /*messageTextField.textProperty().addListener((observable, oldText, newText)->{
            String changedText = EmojiParser.getInstance().asciiToUnicode(messageTextField.getText());
            try {
                messageTextField.setText(changedText);
            }catch(Exception e){
                //e.printStackTrace();
            }
        });*/


        emojiButton = (Button) view.lookup("#emojiButton");
        emojiButton.setOnAction(this::emojiButtonClicked);
        emojiTable = (TableView) view.lookup("#emojiTable");
    }

    private void emojiButtonClicked(ActionEvent actionEvent) {
        ObservableList<String> emojisString = FXCollections.observableArrayList();
        /*ArrayList<Emoji> emojis = new ArrayList<>(EmojiManager.getAll());
        for(Emoji emoj : emojis){
            emojisString.add(emoj.getUnicode());
        }*/
        //emojiTable.getItems().addAll(emojisString);
        //emojiTable.getItems().add("Hallo");
        emojiTable.setItems(emojisString);
        // All Child components of StackPane
        ObservableList<Node> childs = stack.getChildren();

        if (childs.size() > 1) {
            // Top Component
            Node topNode = childs.get(childs.size()-1);
            topNode.toBack();
        }


    }

    /**
     * get Text from TextField and build message
     */
    private void sendButtonClicked(ActionEvent actionEvent) {

        messageTextField.parseAndAppend(":wink: hjasgddzasfuzt");
        //get Text from TextField and clear TextField after
        //String textMessage = messageTextField.getAccessibleText();
        //String textMessage = EmojiParser.parseToUnicode(result);
        /*if (textMessage.length() <= 700) {
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
        }*/
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
        //this.messageTextField.setText("");
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
