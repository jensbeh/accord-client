package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiParser;
import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.cellfactories.MessageListCell;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.home.HomeViewController;
import de.uniks.stp.controller.home.PrivateViewController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.json.JsonException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static de.uniks.stp.util.Constants.*;

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
    private String text;
    private ContextMenu contextMenu;
    private ResourceBundle lang;
    private HBox messageBox;
    private Stage stage;
    private EmojiTextFlowParameters emojiTextFlowParameters;
    private Button editButton;
    private Button abortButton;
    private String textWrote;
    private RestClient restClient;
    private Message selectedMsg;
    private int counter;
    private ArrayList<String> pngNames = new ArrayList<>();

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
        restClient = builder.getRestClient();

        emojiTextFlowParameters = new EmojiTextFlowParameters();
        emojiTextFlowParameters.setEmojiScaleFactor(1D);
        emojiTextFlowParameters.setTextAlignment(TextAlignment.CENTER);
        emojiTextFlowParameters.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        emojiTextFlowParameters.setTextColor(Color.WHITE);


        // Load all view references
        root = (VBox) view.lookup("#root");
        sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        messageTextField.setText("");
        sendButton.setOnAction(this::sendButtonClicked);
        messageBox = (HBox) view.lookup("#messageBox");
        messageBox.setHgrow(messageTextField, Priority.ALWAYS);
        stack = (StackPane) view.lookup("#stack");
        scrollPane = (ScrollPane) view.lookup("#scroll");

        //ListView with message as parameter and observableList
        messageList = (ListView<Message>) view.lookup("#messageListView");
        messageList.setCellFactory(new MessageListCell());
        MessageListCell.setTheme(builder.getTheme());
        messages = new ArrayList<>();
        lang = StageManager.getLangBundle();

        MessageListCell.setCurrentUser(builder.getPersonalUser());
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
        if (pngNames.isEmpty()) {
            File folder = new File(APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                String name = fileEntry.getName().substring(0, fileEntry.getName().length() - 4);
                for (Emoji emoji : EmojiParser.getInstance().search("")) {
                    if (emoji.getHex().equals(name)) {
                        pngNames.add(name);
                    }
                }
            }
        }
        showEmojis();
    }

    /**
     * sets emojis on FlowPane
     */
    private void showEmojis() {
        FlowPane flow = new FlowPane();
        scrollPane.setContent(flow);
        final File folder = new File(APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH);
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            String name = fileEntry.getName().substring(0, fileEntry.getName().length() - 4);
            if (pngNames.contains(name)) {
                flow.getChildren().add((getImageStack(fileEntry)));
            }
        }
    }

    /**
     * creates StackPane for each image
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
        if (contextMenu == null) {
            contextMenu = new ContextMenu();
            contextMenu.setId("contextMenu");
            contextMenu.setStyle("-fx-background-color: #23272a;" + "-fx-background-radius: 4;");
            final MenuItem item1 = new MenuItem("copy");
            final MenuItem item2 = new MenuItem("edit");
            final MenuItem item3 = new MenuItem("delete");
            item1.setStyle("-fx-text-fill: #FFFFFF");
            item2.setStyle("-fx-text-fill: #FFFFFF");
            item3.setStyle("-fx-text-fill: #FFFFFF");
            item1.setId("copy");
            item2.setId("editItem");
            item3.setId("deleteItem");
            contextMenu.getItems().addAll(item1, item2, item3);
        }
        if (messageList.getSelectionModel().getSelectedItem() == null) {
            messageList.setContextMenu(null);
        } else {
            messageList.setContextMenu(contextMenu);
            text = messageList.getSelectionModel().getSelectedItem().getMessage();

            if (!messageList.getSelectionModel().getSelectedItem().getFrom().equals(builder.getPersonalUser().getName())
                    || !HomeViewController.inServerChat) {
                contextMenu.getItems().get(1).setVisible(false);
                contextMenu.getItems().get(2).setVisible(false);
            } else {
                contextMenu.getItems().get(1).setVisible(true);
                contextMenu.getItems().get(2).setVisible(true);
            }
        }
        contextMenu.getItems().get(0).setOnAction(this::copy);
        contextMenu.getItems().get(1).setOnAction(this::edit);
        contextMenu.getItems().get(2).setOnAction(this::delete);
        selectedMsg = messageList.getSelectionModel().getSelectedItem();
        messageList.getSelectionModel().select(null);
    }

    /**
     * load delete pop-up
     */
    private void delete(ActionEvent actionEvent) {
        try {
            Parent subview = FXMLLoader.load(Objects.requireNonNull(
                    StageManager.class.getResource("alert/DeleteMessage.fxml")), StageManager.getLangBundle());
            Scene scene = new Scene(subview);
            stage = new Stage();
            stage.setTitle("Delete Message");
            VBox root = (VBox) subview.lookup("#root");
            Label msg = (Label) subview.lookup("#deleteWarning");
            msg.setText("Are you sure you want to delete " + "\n" + "the following message:");
            ScrollPane pane = (ScrollPane) subview.lookup("#deleteMsgScroll");
            if (builder.getTheme().equals("Bright")) {
                emojiTextFlowParameters.setTextColor(Color.BLACK);
            } else {
                emojiTextFlowParameters.setTextColor(Color.WHITE);
            }
            EmojiTextFlow deleteMsg = new EmojiTextFlow(emojiTextFlowParameters);
            deleteMsg.setId("deleteMsg");
            String msgText = formattedText(text);
            deleteMsg.parseAndAppend(msgText);
            deleteMsg.setMinWidth(530);
            pane.setContent(deleteMsg);
            Button no = (Button) subview.lookup("#chooseCancel");
            Button yes = (Button) subview.lookup("#chooseDelete");
            yes.setOnAction(this::deleteMessage);
            no.setOnAction(this::cancelDelete);
            if (builder.getTheme().equals("Bright")) {
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/styles/themes/bright/ChatView.css")).toExternalForm());
            } else {
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/styles/themes/dark/ChatView.css")).toExternalForm());
            }
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(messageBox.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * formatted a text so the teyt is not too long in a row
     */
    private String formattedText(String text) {
        String str = text;
        int point = 0;
        int counter = 25;
        boolean found = false;
        int endPoint;
        int length = str.length();
        while ((point + 50) < length) {
            endPoint = point + 50;
            while (counter != 0 && !found) {
                counter--;
                if (str.charAt(endPoint - (25 - counter)) == ' ') {
                    str = new StringBuilder(str).insert(endPoint - (25 - counter), "\n").toString();
                    length += 2;
                    found = true;
                    point = endPoint - (25 - counter) + 2;
                }
            }
            if (counter == 0) {
                str = new StringBuilder(str).insert(endPoint, "\n").toString();
                length += 2;
                point = endPoint + 2;
            }
            found = false;
            counter = 25;
        }
        return str;
    }

    private void cancelDelete(ActionEvent actionEvent) {
        stage.close();
    }

    private void deleteMessage(ActionEvent actionEvent) {
        String serverId = selectedMsg.getServerChannel().getCategories().getServer().getId();
        String catId = selectedMsg.getServerChannel().getCategories().getId();
        String channelId = selectedMsg.getServerChannel().getId();
        String userKey = builder.getPersonalUser().getUserKey();
        String msgId = selectedMsg.getId();
        restClient.deleteMessage(serverId, catId, channelId, msgId, messageTextField.getText(), userKey, response -> {
        });
        refreshMessageListView();
        stage.close();
    }

    /**
     * load edit and abort button and save text from textField
     * add enter functionality for editButton
     */
    private void edit(ActionEvent actionEvent) {
        if (messageBox.getChildren().contains(sendButton)) {
            editButton = new Button();
            editButton.setText("edit");
            editButton.setId("editButton");
            abortButton = new Button();
            abortButton.setText("abort");
            abortButton.setId("abortButton");
            messageBox.getChildren().remove(sendButton);
            messageBox.getChildren().add(editButton);
            messageBox.getChildren().add(abortButton);
            textWrote = messageTextField.getText();
            setTheme();
        }
        messageBox.setPadding(new Insets(0, 20, 0, 0));
        messageTextField.setText(text);
        abortButton.setOnAction(this::abortEdit);
        editButton.setOnAction(this::editMessage);
        messageTextField.setOnKeyReleased(key -> {//messageList?
            if (key.getCode() == KeyCode.ENTER) {
                editButton.fire();
            }
        });
    }

    /**
     * edit message and refresh the ListView
     */
    private void editMessage(ActionEvent actionEvent) {
        String serverId = selectedMsg.getServerChannel().getCategories().getServer().getId();
        String catId = selectedMsg.getServerChannel().getCategories().getId();
        String channelId = selectedMsg.getServerChannel().getId();
        String userKey = builder.getPersonalUser().getUserKey();
        String msgId = selectedMsg.getId();
        restClient.updateMessage(serverId, catId, channelId, msgId, messageTextField.getText(), userKey, response -> {
        });
        refreshMessageListView();
        abortEdit(actionEvent);
    }

    /**
     * show normal chatView and text before click edit
     */
    private void abortEdit(ActionEvent actionEvent) {
        messageBox.getChildren().remove(editButton);
        messageBox.getChildren().remove(abortButton);
        messageBox.getChildren().add(sendButton);
        messageTextField.setText(textWrote);
        messageTextField.setOnKeyReleased(key -> {//messageList?
            if (key.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
    }

    /**
     * copied the selected text
     */
    private void copy(ActionEvent actionEvent) {
        final ClipboardContent clipboardContent = new ClipboardContent();
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
                    MessageListCell.setCurrentUser(builder.getPersonalUser());
                    try {
                        if (builder.getPrivateChatWebSocketClient() != null && PrivateViewController.getSelectedChat() != null) {
                            builder.getPrivateChatWebSocketClient().sendMessage(new JSONObject().put("channel", "private").put("to", PrivateViewController.getSelectedChat().getName()).put("message", textMessage).toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    MessageListCell.setCurrentUser(builder.getPersonalUser());
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
    public void printMessage(Message msg) {
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

    /**
     * removes message from observableList
     */
    public void removeMessage(Message msg) {
        if (!HomeViewController.inServerChat) {
            if (PrivateViewController.getSelectedChat().getName().equals(msg.getPrivateChat().getName())) {
                messages.remove(msg);
                refreshMessageListView();
            }
        } else {
            if (currentChannel.getId().equals(msg.getServerChannel().getId())) {
                messages.remove(msg);
                refreshMessageListView();
            }
        }
    }

    public void refreshMessageListView() {
        Platform.runLater(() -> {
            messageList.setItems(FXCollections.observableArrayList(messages));
            checkScrollToBottom();
        });

    }

    public void checkScrollToBottom() {
        ListViewSkin<?> ts = (ListViewSkin<?>) messageList.getSkin();
        int lastMessagePosition = 0;
        int firstMessagePosition = 0;
        if (ts != null) {
            VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
            if (vf != null) {
                if (vf.getFirstVisibleCell() != null && vf.getFirstVisibleCell() != null) {
                    lastMessagePosition = vf.getFirstVisibleCell().getIndex();
                    firstMessagePosition = vf.getFirstVisibleCell().getIndex();
                }
            }
        }
        if (lastMessagePosition == messages.size() || firstMessagePosition == 0) {
            messageList.scrollTo(messages.size());
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

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/styles/themes/bright/ChatView.css")).toExternalForm());
        refreshMessageListView();

    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/styles/themes/dark/ChatView.css")).toExternalForm());
        refreshMessageListView();
    }
}
