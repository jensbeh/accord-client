package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageView {
    private String urlType;
    private ModelBuilder builder;
    private ChatViewController chatViewController;
    private Runnable scroll;

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    public void setChatViewController(ChatViewController chatViewController) {
        this.chatViewController = chatViewController;
    }

    public void updateItem(Message item) {
        boolean messageIsInfo = false;
        boolean loadImage;
        boolean loadVideo;
        StackPane cell = new StackPane();
        cell.setId("messageCell");
        //Background for the messages

        if (item.getMessage().endsWith("#arrival") || item.getMessage().endsWith("#exit")) {
            messageIsInfo = true;
        }

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
        if (urlType.equals("video") || urlType.equals("localVideo")) {
            loadVideo = true;
            setVideo(url, mediaView);
            textMessage = textMessage.replace(url, "");
        } else if (!urlType.equals("None")) {
            loadImage = true;
            setMedia(url, webView.getEngine());
            textMessage = textMessage.replace(url, "");
        }
        if (loadImage) {
            webView.setContextMenuEnabled(false);
            setImageSize(chatViewController.getMessageScrollPane(), url, webView);
        }

        if (messageIsInfo) {
            vbox.setAlignment(Pos.CENTER_LEFT);
            userName.setText((formatterTime.format(date)));

            message = handleEmojis("system");
        } else if (builder.getPersonalUser().getName().equals(item.getFrom())) {
            vbox.setAlignment(Pos.CENTER_RIGHT);
            userName.setText((formatterTime.format(date)) + " " + item.getFrom());

            message = handleEmojis("self");
        } else {
            vbox.setAlignment(Pos.CENTER_LEFT);
            userName.setText(item.getFrom() + " " + (formatterTime.format(date)));

            message = handleEmojis("other");
        }
        Text textToCalculateWidth = new Text(textMessage);
        if (!textMessage.equals("")) {
            textToCalculateWidth.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            message.setId("messageLabel");
            if (textToCalculateWidth.getLayoutBounds().getWidth() > 320) {
                message.setMaxWidth(320);
                message.setPrefWidth(320);
                message.setMinWidth(320);
            } else {
                message.setMaxWidth(textToCalculateWidth.getLayoutBounds().getWidth());
                message.setPrefWidth(textToCalculateWidth.getLayoutBounds().getWidth());
                message.setMinWidth(textToCalculateWidth.getLayoutBounds().getWidth());
            }
            String str = null;
            if (messageIsInfo) {
                ResourceBundle lang = StageManager.getLangBundle();
                if (item.getMessage().endsWith("#arrival")) {
                    str = handleSpacing(":white_check_mark: " + item.getFrom() + " " + lang.getString("message.user_arrived"));
                } else if (item.getMessage().endsWith("#exit")) {
                    str = handleSpacing(":no_entry: " + item.getFrom() + " " + lang.getString("message.user_exited"));
                }
            } else {
                str = handleSpacing(textMessage);
            }
            message.parseAndAppend(str);
        }

        HBox messageBox = new HBox();
        messageBox.getChildren().add(message);
        if (textToCalculateWidth.getLayoutBounds().getWidth() > 320) {
            messageBox.setMaxWidth(320);
        } else {
            messageBox.setMaxWidth(textToCalculateWidth.getLayoutBounds().getWidth());
        }
        HBox finalMessageBox = new HBox();
        if (textToCalculateWidth.getLayoutBounds().getWidth() > 320) {
            finalMessageBox.setMaxWidth(320 + 10);
        } else {
            finalMessageBox.setMaxWidth(textToCalculateWidth.getLayoutBounds().getWidth() + 10);
        }

        //Message background
        Polygon polygon = new Polygon();
        if (messageIsInfo) {
            polygon.getStyleClass().add("messagePolygonSystem");
            messageBox.setId("messageBoxSystem");
            polygon.getPoints().addAll(0.0, 0.0,
                    10.0, 0.0,
                    10.0, 10.0);
            finalMessageBox.getChildren().addAll(polygon, messageBox);
        } else if (builder.getPersonalUser().getName().equals(item.getFrom())) {
            polygon.getStyleClass().add("messagePolygonCurrentUser");
            messageBox.setId("messageBoxCurrentUser");
            polygon.getPoints().addAll(0.0, 0.0,
                    10.0, 0.0,
                    0.0, 10.0);
            finalMessageBox.getChildren().addAll(messageBox, polygon);
        } else {
            polygon.getStyleClass().add("messagePolygonOther");
            messageBox.setId("messageBoxOtherUser");
            polygon.getPoints().addAll(0.0, 0.0,
                    10.0, 0.0,
                    10.0, 10.0);
            finalMessageBox.getChildren().addAll(polygon, messageBox);
        }


        if (loadImage) {
            vbox.getChildren().addAll(userName, webView);
            cell.setMinSize(webView.getMaxWidth(), webView.getPrefHeight());
        } else if (loadVideo) {
            MediaControl mediaControl = new MediaControl();
            VBox mediaBox = mediaControl.setMediaControls(mediaView);
            setVideoSize(chatViewController.getMessageScrollPane(), url, mediaView);
            vbox.getChildren().addAll(userName, mediaBox);

        } else {
            vbox.getChildren().addAll(userName, finalMessageBox);
            vbox.setMouseTransparent(true);
        }

        cell.setAlignment(Pos.CENTER_RIGHT);
        cell.getChildren().addAll(vbox);
        if (!messageIsInfo) {
            cell.setOnMouseClicked(chatViewController::chatClicked);
        }
        chatViewController.getContainer().getChildren().add(cell);
        chatViewController.getMessagesHashMap().put(cell, item);
        chatViewController.getStackPaneHashMap().put(item, cell);
        if (scroll != null) {
            scroll.run();
        }
    }

    private EmojiTextFlow handleEmojis(String type) {
        EmojiTextFlowParameters emojiTextFlowParameters;
        {
            emojiTextFlowParameters = new EmojiTextFlowParameters();
            emojiTextFlowParameters.setEmojiScaleFactor(1D);
            emojiTextFlowParameters.setTextAlignment(TextAlignment.LEFT);
            emojiTextFlowParameters.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        }
        if (type.equals("system")) {
            emojiTextFlowParameters.setTextColor(Color.BLACK);
        } else if (type.equals("self")) {
            if (builder.getTheme().equals("Dark")) {
                emojiTextFlowParameters.setTextColor(Color.BLACK);
            } else {
                emojiTextFlowParameters.setTextColor(Color.WHITE);
            }
        } else {
            emojiTextFlowParameters.setTextColor(Color.BLACK);
        }
        return new EmojiTextFlow(emojiTextFlowParameters);
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
        String urlRegex = "\\b(https?|ftp|file|src)(://|/)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
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
        } else if (url.contains("youtube")) {
            urlType = "youtube";
        } else if ((url.contains("src/") || url.contains("file://")) && url.contains(".mp4")) {
            urlType = "localVideo";
        } else if (url.contains(".mp4")) {
            urlType = "video";
        } else {
            urlType = "None";
        }
        return url;
    }

    private void setVideo(String url, MediaView mediaView) {
        Media mediaUrl;
        if (urlType.equals("localVideo")) {
            File file = new File(url);
            mediaUrl = new Media(file.toURI().toString());
        } else {
            mediaUrl = new Media(url);
        }
        MediaPlayer mp = new MediaPlayer(mediaUrl);
        chatViewController.getMediaPlayers().add(mp);
        mediaView.setMediaPlayer(mp);
        mp.setOnError(() -> System.out.println("Error : " + mp.getError().toString()));
    }

    private void setMedia(String url, WebEngine engine) {
        switch (urlType) {
            case "picture":
                engine.load(url);
                engine.setJavaScriptEnabled(false);
                break;
            case "gif":
                engine.loadContent("<html><body><img src=\"" + url + "\" class=\"center\"></body></html>");
                engine.setJavaScriptEnabled(false);
                break;
            case "youtube":
                String videoIdPatternRegex = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
                Pattern videoIdPattern = Pattern.compile(videoIdPatternRegex);
                Matcher videoIdMatcher = videoIdPattern.matcher(url); //url is youtube url for which you want to extract the id.
                String youtube_url = "";
                if (videoIdMatcher.find()) {
                    String videoId = videoIdMatcher.group();
                    youtube_url = "https://www.youtube.com/embed/" + videoId;
                }
                engine.load(youtube_url);
                engine.setJavaScriptEnabled(true);
                break;
        }
        chatViewController.getWebEngines().add(engine);
        if (builder.getTheme().equals("Bright")) {
            engine.setUserStyleSheetLocation(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/styles/themes/bright/webView.css")).toExternalForm());
        } else {
            engine.setUserStyleSheetLocation(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/styles/themes/dark/webView.css")).toExternalForm());
        }

    }

    private void setVideoSize(Parent parent, String url, MediaView mediaView) {
        try {
            Bounds bounds = parent.getParent().getParent().getParent().getBoundsInLocal();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            int height = 0;
            int width = 0;
            if (!urlType.equals("None")) {
                URL url_stream;
                if (url.contains("src/test")) {
                    File file = new File(url);
                    url_stream = new URL(file.toURI().toString());
                } else {
                    url_stream = new URL(url);
                }
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
            while (parent.getParent() != null && parent.getId() != null && !Objects.equals(parent.getId(), "chatBox")) {
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

    public void setScroll(Runnable scroll) {
        this.scroll = scroll;
    }
}
