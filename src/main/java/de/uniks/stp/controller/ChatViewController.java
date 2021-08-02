package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiParser;
import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;

import javax.json.JsonException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static de.uniks.stp.util.Constants.*;

public class ChatViewController {
    private ContextMenu contextMenu;
    private final ModelBuilder builder;
    private ServerChannel currentChannel;
    private final Parent view;
    private VBox root;
    private Button sendButton;
    private TextField messageTextField;
    private StackPane stack;
    private ScrollPane scrollPane;
    private String text;
    private ResourceBundle lang;
    private HBox messageBox;
    private Stage stage;
    private EmojiTextFlowParameters emojiTextFlowParameters;
    private Button editButton;
    private Button abortButton;
    private Button emojiButton;
    private String textWrote;
    private RestClient restClient;
    private Message selectedMsg;
    private ArrayList<String> pngNames;
    private VBox messagesBox;
    private ScrollPane messageScrollPane;
    private HashMap<StackPane, Message> messagesHashMap;
    private HashMap<Message, StackPane> stackPaneHashMap;
    private ArrayList<MediaPlayer> mediaPlayers;
    private ArrayList<WebEngine> webEngines;
    private ListChangeListener<User> blockedUserListener;

    public ChatViewController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public ChatViewController(Parent view, ModelBuilder builder, ServerChannel currentChannel) {
        this.view = view;
        this.builder = builder;
        this.currentChannel = currentChannel;
    }

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
        HBox.setHgrow(messageTextField, Priority.ALWAYS);
        stack = (StackPane) view.lookup("#stack");
        scrollPane = (ScrollPane) view.lookup("#scroll");
        messageScrollPane = (ScrollPane) view.lookup("#messageScrollPane");
        // set scroll speed
        final double SPEED = 0.001;
        messageScrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            messageScrollPane.setVvalue(messageScrollPane.getVvalue() - deltaY);
        });

        messageScrollPane.setFitToHeight(true);
        messageScrollPane.setFitToWidth(true);
        messagesBox = (VBox) messageScrollPane.getContent().lookup("#messageVBox");
        messagesHashMap = new HashMap<>();
        stackPaneHashMap = new HashMap<>();
        lang = StageManager.getLangBundle();
        messageTextField.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
        mediaPlayers = new ArrayList<>();
        webEngines = new ArrayList<>();
        pngNames = new ArrayList<>();
        emojiButton = (Button) view.lookup("#emojiButton");
        emojiButton.setOnAction(this::emojiButtonClicked);
        builder.setCurrentChatViewController(this);

        // only add blocked listener if it is a private chat
        if (currentChannel == null) {
            blockedUserListener();
        }
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public ArrayList<MediaPlayer> getMediaPlayers() {
        return mediaPlayers;
    }

    public ScrollPane getMessageScrollPane() {
        return messageScrollPane;
    }

    public VBox getContainer() {
        return messagesBox;
    }

    public HashMap<StackPane, Message> getMessagesHashMap() {
        return messagesHashMap;
    }

    public HashMap<Message, StackPane> getStackPaneHashMap() {
        return stackPaneHashMap;
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
    public void chatClicked(MouseEvent mouseEvent) {
        if (contextMenu == null) {
            contextMenu = new ContextMenu();

            MenuItem item1 = new MenuItem("copy");
            MenuItem item2 = new MenuItem("edit");
            MenuItem item3 = new MenuItem("delete");

            contextMenu.getItems().addAll(item1, item2, item3);
        }

        if (!messageBox.getChildren().contains(sendButton)) {
            abortButton.fire();
        }

        StackPane selected = null;
        if (mouseEvent.getPickResult().getIntersectedNode() instanceof StackPane) {
            selected = (StackPane) mouseEvent.getPickResult().getIntersectedNode();
        }
        //if video gets clicked
        else if (mouseEvent.getPickResult().getIntersectedNode().getParent().getParent() instanceof StackPane) {
            selected = (StackPane) mouseEvent.getPickResult().getIntersectedNode().getParent().getParent();
        }

        if (selected != null) {
            StackPane finalSelected = selected;
            //needs to happen here, otherwise contextMenu won't get the css
            if (builder.getTheme().equals("Bright")) {
                finalSelected.getScene().getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ChatView.css")).toExternalForm());
            } else {
                finalSelected.getScene().getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ChatView.css")).toExternalForm());
            }
            selected.setOnContextMenuRequested(event -> {
                contextMenu.setY(event.getScreenY());
                contextMenu.setX(event.getScreenX());
                contextMenu.show(finalSelected.getScene().getWindow());
            });
            // if message is a text message
            text = messagesHashMap.get(selected).getMessage();
            if (!messagesHashMap.get(selected).getFrom().equals(builder.getPersonalUser().getName()) || !builder.getInServerState()) {
                contextMenu.getItems().get(1).setVisible(false);
                contextMenu.getItems().get(2).setVisible(false);
            } else {
                contextMenu.getItems().get(1).setVisible(true);
                contextMenu.getItems().get(2).setVisible(true);
            }
            selectedMsg = messagesHashMap.get(selected);
        }
        contextMenu.getItems().get(0).setOnAction(this::copy);
        contextMenu.getItems().get(1).setOnAction(this::edit);
        contextMenu.getItems().get(2).setOnAction(this::delete);
    }

    /**
     * load delete pop-up
     */
    private void delete(ActionEvent actionEvent) {
        try {
            ResourceBundle lang = StageManager.getLangBundle();

            Parent subview = FXMLLoader.load(Objects.requireNonNull(
                    StageManager.class.getResource("alert/DeleteMessage.fxml")), StageManager.getLangBundle());
            Scene scene = new Scene(subview);
            stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle(lang.getString("window_title_delete_message"));

            // DropShadow of Scene
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(StageManager.class.getResource("styles/DropShadow/DropShadow.css").toExternalForm());

            HBox titleBarBox = (HBox) subview.lookup("#titleBarBox");
            try {
                Parent titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBar.fxml")), StageManager.getLangBundle());
                titleBarBox.getChildren().add(titleBarView);
                TitleBarController titleBarController = new TitleBarController(stage, titleBarView, builder);
                titleBarController.init();
                titleBarController.setMaximizable(false);
                titleBarController.setTheme();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Label msg = (Label) subview.lookup("#deleteWarning");
            msg.setText(lang.getString("label.message_delete_info"));
            ScrollPane pane = (ScrollPane) subview.lookup("#deleteMsgScroll");
            if (builder.getTheme().equals("Bright")) {
                emojiTextFlowParameters.setTextColor(Color.BLACK);
            } else {
                emojiTextFlowParameters.setTextColor(Color.WHITE);
            }
            EmojiTextFlow deleteMsg = new EmojiTextFlow(emojiTextFlowParameters);
            deleteMsg.setId("deleteMsg");
            String msgText;
            if (text == null) {
                text = selectedMsg.getMessage();
            }
            msgText = formattedText(text);
            deleteMsg.parseAndAppend(msgText);
            deleteMsg.setMinWidth(530);
            pane.setContent(deleteMsg);
            Button no = (Button) subview.lookup("#chooseCancel");
            Button yes = (Button) subview.lookup("#chooseDelete");
            yes.setText(lang.getString("button.yes"));
            no.setText(lang.getString("button.no"));
            yes.setOnAction(this::deleteMessage);
            no.setOnAction(this::cancelDelete);
            if (builder.getTheme().equals("Bright")) {
                scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ChatView.css")).toExternalForm());
            } else {
                scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ChatView.css")).toExternalForm());
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
     * formatted a text so the text is not too long in a row
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
        StackPane toRemoveStack = stackPaneHashMap.get(selectedMsg);
        Message toRemoveMsg = messagesHashMap.get(toRemoveStack);
        stackPaneHashMap.remove(toRemoveMsg);
        messagesHashMap.remove(toRemoveStack);
        messagesBox.getChildren().remove(toRemoveStack);
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
            onLanguageChanged();
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
        //edit message or show pop-up by empty message
        if (messageTextField.getText().equals("")) {
            try {
                //create pop-up
                Parent subview = FXMLLoader.load(Objects.requireNonNull(
                        StageManager.class.getResource("alert/EditWarningMessage.fxml")), StageManager.getLangBundle());
                Scene scene = new Scene(subview);
                stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);

                // DropShadow of Scene
                scene.setFill(Color.TRANSPARENT);
                scene.getStylesheets().add(StageManager.class.getResource("styles/DropShadow/DropShadow.css").toExternalForm());

                HBox titleBarBox = (HBox) subview.lookup("#titleBarBox");
                try {
                    Parent titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBar.fxml")), StageManager.getLangBundle());
                    titleBarBox.getChildren().add(titleBarView);
                    TitleBarController titleBarController = new TitleBarController(stage, titleBarView, builder);
                    titleBarController.init();
                    titleBarController.setMaximizable(false);
                    titleBarController.setTheme();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Label msg = (Label) subview.lookup("#editWarningText");
                Button yes = (Button) subview.lookup("#deleteEditMessage");
                Button no = (Button) subview.lookup("#abortEditMessage");
                //language
                lang = StageManager.getLangBundle();
                stage.setTitle(lang.getString("title.edit_warning"));
                msg.setText(lang.getString("label.edit_warning"));
                yes.setText(lang.getString("button.edit_delete"));
                no.setText(lang.getString("button.abort_edit_delete"));
                yes.setOnAction((event) -> {
                    stage.close();
                    deleteMessage(event);
                });
                //by click on delete close pop-up and edit menu
                no.setOnAction((event -> stage.close()));
                //theme
                if (builder.getTheme().equals("Bright")) {
                    scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ChatView.css")).toExternalForm());
                } else {
                    scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ChatView.css")).toExternalForm());
                }
                //show pop-up and leave edit mode
                abortEdit(actionEvent);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.initOwner(messageBox.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            restClient.updateMessage(serverId, catId, channelId, msgId, messageTextField.getText(), userKey, response -> {
            });
            abortEdit(actionEvent);
        }
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
        ClipboardContent clipboardContent = new ClipboardContent();
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
            if (!textMessage.isEmpty() && !textMessage.endsWith("#arrival") && !textMessage.endsWith("#exit")) {
                if (!builder.getInServerState()) {
                    try {
                        if (builder.getPrivateChatWebSocketClient() != null && builder.getCurrentPrivateChat() != null) {
                            builder.getPrivateChatWebSocketClient().sendMessage(new JSONObject().put("channel", "private").put("to", builder.getCurrentPrivateChat().getName()).put("message", textMessage).toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
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
        if (!builder.getInServerState()) {
            if (builder.getCurrentPrivateChat().getName().equals(msg.getPrivateChat().getName())) { // only print message when user is on correct chat channel
                MessageView messageView = new MessageView();
                messageView.setBuilder(builder);
                messageView.setChatViewController(this);
                messageView.setScroll(this::checkScrollToBottom);
                messageView.updateItem(msg);
            }
        } else {
            if (currentChannel.getId().equals(msg.getServerChannel().getId())) {
                MessageView messageView = new MessageView();
                messageView.setBuilder(builder);
                messageView.setChatViewController(this);
                messageView.setScroll(this::checkScrollToBottom);
                messageView.updateItem(msg);
            }
        }
    }

    /**
     * removes message from observableList
     */
    public void removeMessage(Message msg) {
        if (!builder.getInServerState()) {
            if (builder.getCurrentPrivateChat().getName().equals(msg.getPrivateChat().getName())) {
                StackPane toRemoveStack = stackPaneHashMap.get(msg);
                Message toRemoveMsg = messagesHashMap.get(toRemoveStack);
                stackPaneHashMap.remove(toRemoveMsg);
                messagesHashMap.remove(toRemoveStack);
                Platform.runLater(() -> messagesBox.getChildren().remove(toRemoveStack));
            }
        } else {
            if (currentChannel.getId().equals(msg.getServerChannel().getId())) {
                StackPane toRemoveStack = stackPaneHashMap.get(msg);
                Message toRemoveMsg = messagesHashMap.get(toRemoveStack);
                stackPaneHashMap.remove(toRemoveMsg);
                messagesHashMap.remove(toRemoveStack);
                Platform.runLater(() -> messagesBox.getChildren().remove(toRemoveStack));

            }
        }
    }

    public void updateMessage(Message msg) {
        recalculateSizeAndUpdateMessage(msg);
        checkScrollToBottom();
    }

    /**
     * method to resize the message width and boxes around the message
     */
    private void recalculateSizeAndUpdateMessage(Message msg) {
        Text textToCalculateWidth = new Text(msg.getMessage());
        textToCalculateWidth.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        EmojiTextFlow emojiTextFlow = ((EmojiTextFlow) ((((HBox) ((((HBox) (((VBox) stackPaneHashMap.get(msg).getChildren().get(0)).getChildren().get(1)))).getChildren().get(0))).getChildren().get(0))));

        if (textToCalculateWidth.getLayoutBounds().getWidth() > 320) {
            emojiTextFlow.setMaxWidth(320);
            emojiTextFlow.setPrefWidth(320);
            emojiTextFlow.setMinWidth(320);
        } else {
            emojiTextFlow.setMaxWidth(textToCalculateWidth.getLayoutBounds().getWidth());
            emojiTextFlow.setPrefWidth(textToCalculateWidth.getLayoutBounds().getWidth());
            emojiTextFlow.setMinWidth(textToCalculateWidth.getLayoutBounds().getWidth());
        }
        String str = formattedText(msg.getMessage());
        ((Text) (emojiTextFlow.getChildren().get(0))).setText(str);

        HBox messageBox = ((HBox) ((((HBox) (((VBox) stackPaneHashMap.get(msg).getChildren().get(0)).getChildren().get(1)))).getChildren().get(0)));

        if (textToCalculateWidth.getLayoutBounds().getWidth() > 320) {
            messageBox.setMaxWidth(320);
        } else {
            messageBox.setMaxWidth(textToCalculateWidth.getLayoutBounds().getWidth());
        }

        HBox finalMessageBox = ((HBox) (((VBox) stackPaneHashMap.get(msg).getChildren().get(0)).getChildren().get(1)));
        if (textToCalculateWidth.getLayoutBounds().getWidth() > 320) {
            finalMessageBox.setMaxWidth(320 + 10);
        } else {
            finalMessageBox.setMaxWidth(textToCalculateWidth.getLayoutBounds().getWidth() + 10);
        }
    }

    public void checkScrollToBottom() {
        Platform.runLater(() -> {
            double vValue = messageScrollPane.getVvalue();
            if (vValue == 0 || vValue >= 0.92 - 10.0 / messagesBox.getChildren().size()) {
                messageScrollPane.setVvalue(1.0);
            }
        });
    }

    public void clearMessageField() {
        this.messageTextField.setText("");
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (sendButton != null)
            sendButton.setText(lang.getString("button.send"));
        if (editButton != null)
            editButton.setText(lang.getString("button.edit"));
        if (abortButton != null)
            abortButton.setText(lang.getString("button.abort"));

        // for translating the blocking info
        checkBlocked();

        // set theme to refresh chat view
        StageManager.setTheme();
    }

    public void stop() {
        sendButton.setOnAction(null);
        stopMediaPlayers();
        if (builder.getBlockedUsers() != null && blockedUserListener != null) {
            builder.getBlockedUsers().removeListener(blockedUserListener);
        }
    }

    public void stopMediaPlayers() {
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.stop();
        }
        stopVideoPlayers();
    }

    public void stopVideoPlayers() {
        for (WebEngine webEngine : webEngines) {
            webEngine.load(null);
        }
    }

    /**
     * listen to the blocked list and make a check if list changed
     */
    public void blockedUserListener() {
        checkBlocked();
        blockedUserListener = c -> checkBlocked();
        if (builder.getBlockedUsers() != null) {
            builder.getBlockedUsers().addListener(blockedUserListener);
        }
    }

    /**
     * call disableView if user is blocked, else call enableView
     */
    public void checkBlocked() {
        for (User user : builder.getBlockedUsers()) {
            if (user.getId().equals(builder.getCurrentPrivateChat().getId())) {
                disableView(user);
                return;
            }
        }
        enableView();
    }

    /**
     * enables the view elements to allow communicating with the user
     */
    public void enableView() {
        messageTextField.setDisable(false);
        emojiButton.setDisable(false);
        sendButton.setDisable(false);
        messageTextField.setText("");
    }

    /**
     * disables the view elements to disallow communicating with the user
     * additionally inform own user that he needs to unblock him to keep chatting with the user
     *
     * @param user the user who is blocked
     */
    public void disableView(User user) {
        messageTextField.setDisable(true);
        emojiButton.setDisable(true);
        sendButton.setDisable(true);
        messageTextField.setText(StageManager.getLangBundle().getString("textField.unblock_info") + " " + user.getName());
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
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ChatView.css")).toExternalForm());
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ChatView.css")).toExternalForm());
    }

    public ArrayList<WebEngine> getWebEngines() {
        return webEngines;
    }
}
