package de.uniks.stp.controller;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageView {
    private String urlType;
    private ModelBuilder builder;
    private ChatViewController chatViewController;
    boolean loadVideo;

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    public void setChatViewController(ChatViewController chatViewController) {
        this.chatViewController = chatViewController;
    }

    public void updateItem(Message item, boolean empty) {
        boolean loadImage;
        boolean loadVideo;
        StackPane cell = new StackPane();
        cell.setId("messageCell");
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
            if (urlType.equals("video")) {
                loadVideo = true;
                setMedia(url, mediaView);
                if (loadVideo) {
                    setVideoSize(chatViewController.getMessageScrollPane(), url, mediaView);
                    textMessage = textMessage.replace(url, "");
                }
            } else if (!urlType.equals("None")) {
                loadImage = true;
                setMedia(url, webView.getEngine());
                textMessage = textMessage.replace(url, "");
            }
            if (loadImage) {
                webView.setContextMenuEnabled(false);
                setImageSize(chatViewController.getMessageScrollPane(), url, webView);
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
//                vbox.setPrefSize(webView.getMaxWidth(), webView.getMaxHeight());
                vbox.getChildren().addAll(userName, message, webView);
//                cell.setPrefSize(chatViewController.getContainer().getMaxWidth(), chatViewController.getContainer().getMaxHeight());
                cell.setMinSize(webView.getMaxWidth(), webView.getPrefHeight());
            } else if (loadVideo) {
                MediaControl mediaControl = new MediaControl();
                VBox mediaBox = mediaControl.setMediaControls(mediaView);
                setVideoSize(chatViewController.getMessageScrollPane(), url, mediaView);
                vbox.getChildren().addAll(userName, message, mediaBox);

            } else {
                vbox.getChildren().addAll(userName, message);
                vbox.setMouseTransparent(true);
            }

            cell.setAlignment(Pos.CENTER_RIGHT);
            cell.getChildren().addAll(vbox);
//            cell.setMinSize(420, 60);
            cell.setOnMouseClicked(chatViewController::chatClicked);
            chatViewController.getContainer().getChildren().add(cell);
            chatViewController.getMessagesHashMap().put(cell, item);
            chatViewController.getStackPaneHashMap().put(item, cell);
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
        String urlRegex = "\\b(https?|ftp|file|src)(://|/test)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
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
        } else if (url.contains(".mp4")) {
            urlType = "video";
        } else {
            urlType = "None";
        }
        return url;
    }

    private void setVideo(String url, MediaView mediaView) {
        Media mediaUrl = null;
        if (url.contains("src/test")) {
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
            Bounds bounds = parent.getParent().getParent().getParent().getBoundsInLocal();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            int height = 0;
            int width = 0;
            if (!urlType.equals("None")) {
                URL url_stream = null;
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


}