package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.util.EmojiTextFlowExtended;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;

import javax.json.JsonException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatViewController {
    private final ModelBuilder builder;
    private final Parent view;
    private ContextMenu contextMenu;
    private ServerChannel currentChannel;
    private VBox root;
    private Button sendButton;
    private TextField messageTextField;
    private StackPane stack;
    private ScrollPane emojiScrollPane;
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
    private TabPane emojiTabPane;
    private ComboBox<Image> skinColorComboBox;
    private boolean emojiViewOpened;

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

        emojiViewOpened = false;

        // Load all view references
        root = (VBox) view.lookup("#root");
        sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        messageTextField.setText("");
        sendButton.setOnAction(this::sendButtonClicked);
        messageBox = (HBox) view.lookup("#messageBox");
        HBox.setHgrow(messageTextField, Priority.ALWAYS);
        stack = (StackPane) view.lookup("#stack");
        messageScrollPane = (ScrollPane) view.lookup("#messageScrollPane");
        VBox emojiBox = (VBox) view.lookup("#emojiBox");

        Platform.runLater(() -> emojiBox.getChildren().add(builder.getEmojiLoaderService().getView()));

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
        lang = builder.getStageManager().getLangBundle();
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
     * shows EmojiView
     */
    private void emojiButtonClicked(ActionEvent actionEvent) {
        // All Child components of StackPane
        ObservableList<Node> children = stack.getChildren();

        if (children.size() > 1) {
            // Top Component
            Node topNode = children.get(children.size() - 1);
            topNode.toBack();
        }

        emojiViewOpened = !emojiViewOpened;
    }

    /**
     * build menu with chat options
     */
    public void chatClicked(MouseEvent mouseEvent, boolean messageIsLink) {
        if (contextMenu == null) {
            contextMenu = new ContextMenu();

            MenuItem item1 = new MenuItem("copy");
            MenuItem item2 = new MenuItem("edit");
            MenuItem item3 = new MenuItem("delete");

            contextMenu.setId("messageContextMenu");
            item1.setId("messageCopy");
            item2.setId("messageEdit");
            item3.setId("messageDelete");

            contextMenu.getItems().addAll(item1, item2, item3);
        }

        ResourceBundle lang = builder.getStageManager().getLangBundle();
        contextMenu.getItems().get(0).setText(lang.getString("menuItem.copy"));
        contextMenu.getItems().get(1).setText(lang.getString("menuItem.edit"));
        contextMenu.getItems().get(2).setText(lang.getString("menuItem.delete"));

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
            // not editable if message is a link
            if (messageIsLink) {
                contextMenu.getItems().get(1).setVisible(false);
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
            ResourceBundle lang = builder.getStageManager().getLangBundle();

            Parent subview = FXMLLoader.load(Objects.requireNonNull(
                    StageManager.class.getResource("alert/DeleteMessage.fxml")), builder.getStageManager().getLangBundle());
            Scene scene = new Scene(subview);
            stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            // DropShadow of Scene
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/DropShadow/DropShadow.css")).toExternalForm());

            // create titleBar
            createTitleBar(subview, "window_title_delete_message");

            Label msg = (Label) subview.lookup("#deleteWarning");
            msg.setText(lang.getString("label.message_delete_info"));
            ScrollPane pane = (ScrollPane) subview.lookup("#deleteMsgScroll");
            if (builder.getTheme().equals("Bright")) {
                emojiTextFlowParameters.setTextColor(Color.BLACK);
            } else {
                emojiTextFlowParameters.setTextColor(Color.WHITE);
            }
            EmojiTextFlowExtended deleteMsg = new EmojiTextFlowExtended(emojiTextFlowParameters);
            deleteMsg.setId("deleteMsg");
            String msgText;
            if (text == null) {
                text = selectedMsg.getMessage();
            }
            msgText = formattedText(text);
            String urlRegex = "\\b(https?|ftp|file|src)(://|/)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            Pattern pattern = Pattern.compile(urlRegex);
            Matcher matcher = pattern.matcher(text);
            String url = "";
            if (matcher.find()) {
                url = matcher.toMatchResult().group();
            }
            if (!url.equals("")) {
                deleteMsg.addTextLinkNode(text, url);
            } else {
                deleteMsg.parseAndAppend(msgText);
            }

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

    private void createTitleBar(Parent subview, String title) {
        HBox titleBarBox = (HBox) subview.lookup("#titleBarBox");
        Parent titleBarView = null;
        try {
            titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBarView.fxml")), builder.getStageManager().getLangBundle());
        } catch (IOException e) {
            e.printStackTrace();
        }
        titleBarBox.getChildren().add(titleBarView);
        TitleBarController titleBarController = new TitleBarController(stage, titleBarView, builder);
        titleBarController.init();
        titleBarController.setTheme();
        titleBarController.setMaximizable(false);
        titleBarController.setTitle(lang.getString(title));
    }

    /**
     * formatted a text so the text is not too long in a row
     */
    private String formattedText(String text) {
        String str = text;
        int maxLen = 41;
        int point = 0;
        int counter = 25;
        boolean found = false;
        int endPoint;
        int length = str.length();
        while ((point + maxLen) < length) {
            endPoint = point + maxLen;
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
            ResourceBundle lang = builder.getStageManager().getLangBundle();
            editButton = new Button();
            editButton.setText(lang.getString("button.edit"));
            editButton.setId("editButton");
            abortButton = new Button();
            abortButton.setText(lang.getString("button.abort"));
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
        //edit message or show pop-up by empty message
        if (messageTextField.getText().equals("")) {
            try {
                //create pop-up
                Parent subview = FXMLLoader.load(Objects.requireNonNull(
                        StageManager.class.getResource("alert/EditWarningMessage.fxml")), builder.getStageManager().getLangBundle());
                Scene scene = new Scene(subview);
                stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);

                // DropShadow of Scene
                scene.setFill(Color.TRANSPARENT);
                scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/DropShadow/DropShadow.css")).toExternalForm());
                lang = builder.getStageManager().getLangBundle();

                // create titleBar
                createTitleBar(subview, "title.edit_warning");


                Label msg = (Label) subview.lookup("#editWarningText");
                Button yes = (Button) subview.lookup("#deleteEditMessage");
                Button no = (Button) subview.lookup("#abortEditMessage");
                //language
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
            if (currentChannel != null && currentChannel.getId().equals(msg.getServerChannel().getId())) {
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
        Platform.runLater(() -> {
            recalculateSizeAndUpdateMessage(msg);
            checkScrollToBottom();
        });
    }

    /**
     * method to resize the message width and boxes around the message
     */
    private void recalculateSizeAndUpdateMessage(Message msg) {
        StackPane stackPane = stackPaneHashMap.get(msg);
        VBox vBox = (VBox) stackPane.getChildren().get(0);
        HBox hBox = (HBox) vBox.getChildren().get(1);
        // one of the children is the Polygon, the other one the HBox including the message
        HBox messageHBox;
        if (hBox.getChildren().get(0) instanceof HBox) {
            messageHBox = (HBox) hBox.getChildren().get(0);
        } else {
            messageHBox = (HBox) hBox.getChildren().get(1);
        }
        EmojiTextFlowExtended emojiTextFlow = (EmojiTextFlowExtended) messageHBox.getChildren().get(0);

        String str = msg.getMessage();
        emojiTextFlow.getChildren().clear();
        emojiTextFlow.parseAndAppend(str);

        HBox box = (((HBox) (((VBox) stackPaneHashMap.get(msg).getChildren().get(0)).getChildren().get(1))));
        HBox messageBox;
        if (box.getChildren().get(0) instanceof HBox) {
            messageBox = ((HBox) box.getChildren().get(0));
        } else {
            messageBox = ((HBox) box.getChildren().get(1));
        }


        // an independent EmojiTextFlow is needed to calculate the width
        MessageView messageView = new MessageView();
        EmojiTextFlowExtended promptETF;
        String type;
        if (builder.getPersonalUser().getName().equals(msg.getFrom())) {
            type = "self";
        } else {
            type = "other";
        }
        promptETF = messageView.handleEmojis(builder, type);
        promptETF.parseAndAppend(str);


        double lyw = getLayoutBoundsGetWidth(promptETF) + 10;
        if (lyw > 320) {
            messageBox.setMaxWidth(320);
        } else {
            messageBox.setMaxWidth(lyw);
        }

        HBox finalMessageBox = ((HBox) (((VBox) stackPaneHashMap.get(msg).getChildren().get(0)).getChildren().get(1)));
        ((VBox) stackPaneHashMap.get(msg).getChildren().get(0)).getChildren().remove(1); // remove for realignment
        if (lyw > 320) {
            finalMessageBox.setMaxWidth(320 + 10);
        } else {
            finalMessageBox.setMaxWidth(lyw + 10);
        }
        ((VBox) stackPaneHashMap.get(msg).getChildren().get(0)).getChildren().add(finalMessageBox); // add back
    }

    /**
     * Sums the width of each node, Text and ImageView
     *
     * @param message the given message
     * @return the total width
     */
    private double getLayoutBoundsGetWidth(EmojiTextFlowExtended message) {
        double width = 0.0;

        for (int x = 0; x < message.getChildren().size(); x++) {
            Node T = message.getChildren().get(x);
            width += T.getLayoutBounds().getWidth();
        }
        return width;
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
        ResourceBundle lang = builder.getStageManager().getLangBundle();
        if (sendButton != null)
            sendButton.setText(lang.getString("button.send"));
        if (editButton != null)
            editButton.setText(lang.getString("button.edit"));
        if (abortButton != null)
            abortButton.setText(lang.getString("button.abort"));

        // for translating the blocking info
        checkBlocked();

        // set theme to refresh chat view
        builder.getStageManager().setTheme();
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
     * disables the view elements to disallow communicating with the user also close the emojiView if opened
     * additionally inform own user that he needs to unblock him to keep chatting with the user
     *
     * @param user the user who is blocked
     */
    public void disableView(User user) {
        if (emojiViewOpened) {
            emojiButton.fire();
        }
        messageTextField.setDisable(true);
        emojiButton.setDisable(true);
        sendButton.setDisable(true);
        messageTextField.setText(builder.getStageManager().getLangBundle().getString("textField.unblock_info") + " " + user.getName());
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }

        builder.getEmojiLoaderService().setTheme();
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

    /**
     * insert the emoji to the textField
     */
    public void onEmojiClicked(String emojiShortname) {
        messageTextField.setText(messageTextField.getText() + emojiShortname);
    }
}
