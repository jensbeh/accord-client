package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiParser;
import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.cellfactories.MessageListCell;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.json.JsonException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.uniks.stp.util.Constants.*;

public class ChatViewController {
    private ModelBuilder builder;
    private ServerChannel currentChannel;
    private final Parent view;
    private VBox root;
    private Button sendButton;
    private TextField messageTextField;
    private ListView<Message> messageList;
    private ArrayList<Message> messages;
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
    private ArrayList<String> pngNames = new ArrayList<>();
    private MessageListCell messageListCellFactory;
    private VBox container;

    private boolean loadImage;
    private boolean loadVideo;
    private boolean videoPlayed;
    private String urlType;
    private final boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private javafx.util.Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Slider volumeSlider;
    private HBox mediaBar;
    private VBox mediaBox;
    private VBox messagesBox;
    private ScrollPane messageScrollPane;

    private HashMap<StackPane, Message> messagesHashMap;
    private HashMap<Message, StackPane> stackPaneHashMap;

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
        messageScrollPane = (ScrollPane) view.lookup("#messageScrollPane");
        container = (VBox) messageScrollPane.getContent().lookup("#messageVBox");
        messagesHashMap = new HashMap<>();
        stackPaneHashMap = new HashMap<>();
//        messageList = (ListView<Message>) view.lookup("#messageListView");
//        messageListCellFactory = new MessageListCell();
//        messageList.setCellFactory(messageListCellFactory);
//        messageListCellFactory.setTheme(builder.getTheme());
//        messages = new ArrayList<>();
        lang = StageManager.getLangBundle();

//        messageListCellFactory.setCurrentUser(builder.getPersonalUser());
//        messageList.setOnMouseClicked(this::chatClicked);

        messageTextField.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });
        Button emojiButton = (Button) view.lookup("#emojiButton");
        emojiButton.setOnAction(this::emojiButtonClicked);
    }

    protected void updateItem(Message item, boolean empty) {
        StackPane cell = new StackPane();
        //Background for the messages
        if (empty) {
            VBox vbox = new VBox();
            Label userName = new Label();
            userName.setId("userNameLabel");
            if (builder.getTheme().equals("Bright")) {
                userName.setTextFill(Color.BLACK);
            } else {
                userName.setTextFill(Color.WHITE);
            }
            EmojiTextFlow message;

            //right alignment if User is currentUser else left
            Date date = new Date(item.getTimestamp());
            DateFormat formatterTime = new SimpleDateFormat("dd.MM - HH:mm");
            String textMessage = item.getMessage();
            String url = searchUrl(textMessage);
            loadImage = false;
            loadVideo = false;
            WebView webView = new WebView();
            MediaView mediaView = new MediaView();
            if (!url.equals("") && !url.contains("https://ac.uniks.de/")) {
                if (webView != null) {
                    setMedia(url, webView.getEngine());
                    if (loadImage) {
                        webView.setContextMenuEnabled(false);
                        setImageSize(cell, url, webView);
                        textMessage = textMessage.replace(url, "");
                    }
                }
                if (mediaView != null) {
                    setMedia(url, mediaView);
                    if (loadVideo) {
                        setVideoSize(cell, url, mediaView);
                        textMessage = textMessage.replace(url, "");
                    }
                }
            }
            if (builder.getPersonalUser().getName().equals(item.getFrom())) {
                vbox.setAlignment(Pos.CENTER_RIGHT);
                userName.setText((formatterTime.format(date)) + " " + item.getFrom());

                message = handleEmojis(true);
                //Message background own user
                message.getStyleClass().clear();
                message.getStyleClass().add("messageLabelTo");

            } else {
                vbox.setAlignment(Pos.CENTER_LEFT);
                userName.setText(item.getFrom() + " " + (formatterTime.format(date)));

                message = handleEmojis(false);
                //Message background
                message.getStyleClass().clear();
                message.getStyleClass().add("messageLabelFrom");
            }
            if (!textMessage.equals("")) {
                message.setId("messageLabel");
                message.setMaxWidth(320);
                message.setPrefWidth(textMessage.length());
                String str = handleSpacing(textMessage);
                message.parseAndAppend(" " + str + " ");
            }

            if (loadImage) {
                vbox.getChildren().addAll(userName, message, webView);
            } else if (loadVideo) {
                if (mediaBox == null) {
                    mediaBox = setMediaControls(mediaView);
                }
                vbox.getChildren().addAll(userName, message, mediaBox);
            } else {
                vbox.getChildren().addAll(userName, message);
            }

            cell.setAlignment(Pos.CENTER_RIGHT);
            cell.getChildren().addAll(vbox);
            cell.setMinWidth(420);
            cell.setOnMouseClicked(this::chatClicked);
            container.getChildren().add(cell);
            messagesHashMap.put(cell, item);
            stackPaneHashMap.put(item, cell);
        }
    }

    private VBox setMediaControls(MediaView mediaView) {
        MediaPlayer mp = mediaView.getMediaPlayer();
        mediaBar = new HBox();
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);
        final Button playButton = new Button("Play");
        mediaBar.getChildren().add(playButton);
        // Add spacer
        Label spacer = new Label("   ");
        mediaBar.getChildren().add(spacer);

        Label timeLabel = new Label("Time: ");
        mediaBar.getChildren().add(timeLabel);

        timeSlider = new Slider();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        mediaBar.getChildren().add(timeSlider);

        playTime = new Label();
        playTime.setPrefWidth(130);
        playTime.setMinWidth(50);
        mediaBar.getChildren().add(playTime);

        Label volumeLabel = new Label("Vol: ");
        mediaBar.getChildren().add(volumeLabel);

        volumeSlider = new Slider();
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setMinWidth(30);

        mediaBar.getChildren().add(volumeSlider);


        if (mediaBox == null) {
            mediaBox = new VBox();
            mediaBox.getChildren().addAll(mediaView, mediaBar);
            mediaBox.setAlignment(Pos.CENTER_RIGHT);
        }

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                MediaPlayer.Status status = mp.getStatus();

                if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
                    // don't do anything in these states
                    return;
                }

                if (status == MediaPlayer.Status.PAUSED
                        || status == MediaPlayer.Status.READY
                        || status == MediaPlayer.Status.STOPPED) {
                    // rewind the movie if we're sitting at the end
                    if (atEndOfMedia) {
                        mp.seek(mp.getStartTime());
                        atEndOfMedia = false;
                    }
                    mp.play();
                    videoPlayed = true;
                } else {
                    mp.pause();
                    videoPlayed = true;
                }
            }
        });

        mp.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                updateValues(mp);
            }
        });

        mp.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    mp.pause();
                    stopRequested = false;
                } else {
                    playButton.setText("||");
                }
            }
        });

        mp.setOnPaused(new Runnable() {
            public void run() {
                System.out.println("onPaused");
                playButton.setText(">");
            }
        });

        mp.setOnReady(new Runnable() {
            public void run() {
                duration = mp.getMedia().getDuration();
                updateValues(mp);
            }
        });

        mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mp.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!repeat) {
                    playButton.setText(">");
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
        });
        return mediaBox;
    }

    protected void updateValues(MediaPlayer mp) {
        if (playTime != null && timeSlider != null && volumeSlider != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mp.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled()
                            && duration.greaterThan(Duration.ZERO)
                            && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration).toMillis()
                                * 100.0);
                    }
                    if (!volumeSlider.isValueChanging()) {
                        volumeSlider.setValue((int) Math.round(mp.getVolume()
                                * 100));
                    }
                }
            });
        }
    }

    private String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 -
                    durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

    private EmojiTextFlow handleEmojis(boolean isUser) {
        if (isUser) {
            EmojiTextFlowParameters emojiTextFlowParameters;
            {
                emojiTextFlowParameters = new EmojiTextFlowParameters();
                emojiTextFlowParameters.setEmojiScaleFactor(1D);
                emojiTextFlowParameters.setTextAlignment(TextAlignment.LEFT);
                emojiTextFlowParameters.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                emojiTextFlowParameters.setTextColor(Color.WHITE);
            }
            return new EmojiTextFlow(emojiTextFlowParameters);
        } else {
            EmojiTextFlowParameters emojiTextFlowParameters;
            {
                emojiTextFlowParameters = new EmojiTextFlowParameters();
                emojiTextFlowParameters.setEmojiScaleFactor(1D);
                emojiTextFlowParameters.setTextAlignment(TextAlignment.LEFT);
                emojiTextFlowParameters.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                emojiTextFlowParameters.setTextColor(Color.BLACK);
            }
            return new EmojiTextFlow(emojiTextFlowParameters);
        }
    }

    private String handleSpacing(String str) {
        //new Line after 50 Characters
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

    private String searchUrl(String msg) {
        String urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(msg);
        String url = "";
        if (matcher.find()) {
            url = matcher.toMatchResult().group();
        }
        if (url.contains(".png") || url.contains(".jpg") || url.contains(".bmp") || url.contains(".svg")) {
            urlType = "picture";
        } else if (url.contains(".gif")) {
            urlType = "gif";
        } else if (url.contains(".mp4")) {
            urlType = "video";
        } else {
            urlType = "None";
        }
        return url;
    }

    private void setVideo(String url, MediaView mediaView) {
        Media mediaUrl = new Media(url);
        MediaPlayer mp = new MediaPlayer(mediaUrl);
        mediaView.setMediaPlayer(mp);
        mp.setOnError(() -> System.out.println("Error : " + mp.getError().toString()));
    }

    private void setMedia(String url, WebEngine engine) {
        if (urlType.equals("picture")) {
            engine.load(url);
            loadImage = true;
            engine.setJavaScriptEnabled(false);
        } else if (urlType.equals("gif")) {
            engine.loadContent("<html><body><img src=\"" + url + "\" class=\"center\"></body></html>");
            loadImage = true;
            engine.setJavaScriptEnabled(false);
        }
        engine.setUserStyleSheetLocation(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/styles/message/webView.css")).toExternalForm());
    }

    private void setMedia(String url, MediaView mediaView) {
        if (urlType.equals("video")) {
            setVideo(url, mediaView);
            loadVideo = true;
        }
    }

    private void setVideoSize(Parent parent, String url, MediaView mediaView) {
        try {
            while (parent.getParent() != null && (parent.getId() == null || parent.getId().equals("container"))) {
                parent = parent.getParent();
            }
            Bounds bounds = parent.getBoundsInLocal();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            int height = 0;
            int width = 0;
            if (!urlType.equals("None")) {
                URL url_stream = new URL(url);
                BufferedImage image = ImageIO.read(url_stream.openStream());
                if (image != null) {
                    height = image.getHeight();
                    width = image.getWidth();
                }
            }
            if (height != 0 && width != 0 && (height < maxY - 50 || width < maxX - 50)) {
                mediaView.setFitHeight(height);
                mediaView.setFitWidth(width);
            } else {
                mediaView.setFitHeight(maxY - 50);
                mediaView.setFitWidth(maxX - 50);
            }
            mediaView.autosize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setImageSize(Parent parent, String url, WebView webView) {
        try {
            while (parent.getParent() != null && (parent.getId() == null || parent.getId().equals("container"))) {
                parent = parent.getParent();
            }
            Bounds bounds = parent.getBoundsInLocal();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            int height = 0;
            int width = 0;
            if (!urlType.equals("None")) {
                URL url_stream = new URL(url);
                BufferedImage image = ImageIO.read(url_stream.openStream());
                if (image != null) {
                    height = image.getHeight();
                    width = image.getWidth();
                }
            }
            if (height != 0 && width != 0 && (height < maxY - 50 || width < maxX - 50)) {
                webView.setMaxSize(width, height);
            } else {
                webView.setMaxSize(maxX - 50, maxY - 50);
            }
            webView.autosize();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        StackPane selected = (StackPane) mouseEvent.getPickResult().getIntersectedNode().getParent().getParent();

        if (selected == null) {

        } else {
            selected.setOnContextMenuRequested(event -> {
                contextMenu.setY(event.getScreenY());
                contextMenu.setX(event.getScreenX());
                contextMenu.show(selected.getScene().getWindow());
            });
            text = messagesHashMap.get(selected).getMessage();

            if (!messagesHashMap.get(selected).getFrom().equals(builder.getPersonalUser().getName())
                    || !builder.getInServerChat()) {
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
        selectedMsg = messagesHashMap.get(selected);
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
        StackPane toRemove = stackPaneHashMap.get(selectedMsg);
        container.getChildren().remove(toRemove);
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
                if (!builder.getInServerChat()) {
                    messageListCellFactory.setCurrentUser(builder.getPersonalUser());
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
        if (!builder.getInServerChat()) {
            if (builder.getCurrentPrivateChat().getName().equals(msg.getPrivateChat().getName())) { // only print message when user is on correct chat channel
                updateItem(msg, true);
                refreshMessageListView();
            }
        } else {
            if (currentChannel.getId().equals(msg.getServerChannel().getId())) {
                updateItem(msg, true);
                refreshMessageListView();
            }
        }
    }

    /**
     * removes message from observableList
     */
    public void removeMessage(Message msg) {
        if (!builder.getInServerChat()) {
            if (builder.getCurrentPrivateChat().getName().equals(msg.getPrivateChat().getName())) {
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
    public void onLanguageChanged() {
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
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ChatView.css")).toExternalForm());
        refreshMessageListView();

    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ChatView.css")).toExternalForm());
        refreshMessageListView();
    }
}
