package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiParser;
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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.json.JSONObject;
import org.junit.Assert;
import util.ResourceManager;

import javax.json.JsonException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static util.Constants.*;

public class ChatViewController {
    private static ModelBuilder builder;
    private static ServerChannel currentChannel;
    private final Parent view;
    private VBox root;
    private static Button sendButton;
    private TextField messageTextField;
    private static ListView<Message> messageList;
    private static ArrayList<Message> messages;
    private StackPane stack;
    private ScrollPane scrollPane;
    private List<String> searchList;

    public ChatViewController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public ChatViewController(Parent view, ModelBuilder builder, ServerChannel currentChannel) {
        this.view = view;
        this.builder = builder;
        this.currentChannel = currentChannel;
    }

    @SuppressWarnings("unchecked")
    public void init() throws JsonException, IOException {
        EmojiTextFlowParameters emojiTextFlowParameters;
        {
            emojiTextFlowParameters = new EmojiTextFlowParameters();
            emojiTextFlowParameters.setEmojiScaleFactor(1D);
            emojiTextFlowParameters.setTextAlignment(TextAlignment.CENTER);
            emojiTextFlowParameters.setFont(Font.font("Verdana", FontWeight.BOLD, 5));
            emojiTextFlowParameters.setTextColor(Color.BLACK);
        }

        // Load all view references
        root = (VBox) view.lookup("#root");
        sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        messageTextField.setText("");
        sendButton.setOnAction(this::sendButtonClicked);
        HBox messageBox = (HBox) view.lookup("#messageBox");
        messageBox.setHgrow(messageTextField, Priority.ALWAYS);
        stack = (StackPane) view.lookup("#stack");
        scrollPane = (ScrollPane) view.lookup("#scroll");

        //ListView with message as parameter and observableList
        messageList = (ListView<Message>) view.lookup("#messageListView");
        messageList.setCellFactory(new AlternateMessageListCellFactory());
        AlternateMessageListCellFactory.setTheme(builder.getTheme());
        messages = new ArrayList<>();

        AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());
        messageList.setOnMouseClicked(this::chatClicked);

        messageTextField.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
        Button emojiButton = (Button) view.lookup("#emojiButton");
        emojiButton.setOnAction(this::emojiButtonClicked);
    }

    /**
     * opens emojiList
     */
    private void emojiButtonClicked(ActionEvent actionEvent) {
        // All Child components of StackPane
        ObservableList<Node> children = stack.getChildren();

        if (children.size() > 1) {
            // Top Component
            Node topNode = children.get(children.size() - 1);
            topNode.toBack();
        }
        showEmojis();
    }

    /**
     * sets emojis on FlowPane
     */
    private void showEmojis() {
        ArrayList<String> pngNames = new ArrayList<>();
        FlowPane flow = new FlowPane();
        scrollPane.setContent(flow);
        final File folder = new File(APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH);

        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            String name = fileEntry.getName().substring(0, fileEntry.getName().length() - 4);
            pngNames.add(name);
            flow.getChildren().add((getImageStack(fileEntry)));
        }
    }

    /**
     * creates StackPane for each image
     *
     */
    private StackPane getImageStack(File fileEntry) {
        StackPane stackPane = new StackPane();
        stackPane.setMaxSize(32, 32);
        stackPane.setPrefSize(32, 32);
        stackPane.setMinSize(32, 32);
        stackPane.setPadding(new Insets(3));
        stackPane.getChildren().add(getEmojiImage(fileEntry));
        return stackPane;
    }

    /**
     * get correct image to the hexStr and sets the textField if emoji clicked
     *
     */
    private ImageView getEmojiImage(File fileEntry) {
        ImageView imageView = new ImageView();
        imageView.setId(fileEntry.getName().substring(0, fileEntry.getName().length() - 4));
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);

        String path = APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH + "/" + fileEntry.getName();
        File newFile = new File(path);
        Image image = new Image(newFile.toURI().toString());
        imageView.setImage(image);
        AtomicReference<String> url = new AtomicReference<>("");
        imageView.setOnMouseClicked(event -> {
            url.set(imageView.getImage().getUrl());
            for (Emoji emoji : EmojiParser.getInstance().search("")) {
                if (emoji.getHex().equals(imageView.getId())) {
                    messageTextField.setText(messageTextField.getText() + emoji.getShortname());
                }
            }
        });
        return imageView;
    }

    /**
     * build menu with chat options
     */
    private void chatClicked(MouseEvent mouseEvent) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #23272a;" + "-fx-background-radius: 4;");
        final MenuItem item1 = new MenuItem("copy");
        item1.setStyle("-fx-text-fill: #FFFFFF");
        contextMenu.getItems().addAll(item1);
        messageList.setContextMenu(contextMenu);
        contextMenu.setOnAction(this::copy);
    }

    /**
     * copied the selected text
     */
    private void copy(ActionEvent actionEvent) {
        final ClipboardContent clipboardContent = new ClipboardContent();
        String text = messageList.getSelectionModel().getSelectedItem().getMessage();
        clipboardContent.putString(text);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
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
                        if (builder.getPrivateChatWebSocketClient() != null && PrivateViewController.getSelectedChat() != null) {
                            builder.getPrivateChatWebSocketClient().sendMessage(new JSONObject().put("channel", "private").put("to", PrivateViewController.getSelectedChat().getName()).put("message", textMessage).toString());
                        }
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
                messages.add(msg);
                refreshMessageListView();
            }
        } else {
            if (currentChannel.getId().equals(msg.getServerChannel().getId())) {
                messages.add(msg);
                refreshMessageListView();
            }
        }
    }

    private static void refreshMessageListView() {
        Platform.runLater(() -> messageList.setItems(FXCollections.observableArrayList(messages)));
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

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/bright/ChatView.css")).toExternalForm());
        refreshMessageListView();

    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/dark/ChatView.css")).toExternalForm());
        refreshMessageListView();
    }
}
