package de.uniks.stp.cellfactories;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Message;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
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
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListCell implements javafx.util.Callback<ListView<Message>, ListCell<Message>> {

    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     *              determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */
    @Override
    public ListCell<Message> call(ListView<Message> param) {
        return new MessageCell();
    }

    private static CurrentUser currentUser;

    public static CurrentUser getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(CurrentUser newCurrentUser) {
        currentUser = newCurrentUser;
    }

    private static String theme;

    public static void setTheme(String newTheme) {
        theme = newTheme;
    }

    private class MessageCell extends ListCell<Message> {
        private boolean loadImage;
        private boolean loadVideo = false;
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
        private MediaView mediaView;
        private MediaPlayer mp;
        private WebView webView;

        /**
         * shows message in cell of ListView
         */
        @Override
        protected void updateItem(Message item, boolean empty) {
            StackPane cell = new StackPane();
            super.updateItem(item, empty);
            //Background for the messages
            this.setId("messagesBox");
            if (!empty) {
                VBox vbox = new VBox();
                Label userName = new Label();
                userName.setId("userNameLabel");
                if (theme.equals("Bright")) {
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

                if (webView == null && (urlType.equals("gif") || urlType.equals("picture"))) {
                    webView = new WebView();
                }
                if (mediaView == null && urlType.equals("video")) {
                    mediaView = new MediaView();
                }
                if (!url.equals("") && !url.contains("https://ac.uniks.de/")) {

                    if (webView != null) {
                        setMedia(url, mediaView);
                        if (loadImage) {
                            webView.setContextMenuEnabled(false);
                            setImageSize(url, webView);
                            textMessage = textMessage.replace(url, "");
                        }
                    }
                    if (mediaView != null) {
                        setMedia(url, mediaView);
                        if (loadVideo) {
                            setVideoSize(url, mediaView);
                            textMessage = textMessage.replace(url, "");
                        }
                    }
                }
                if (currentUser.getName().equals(item.getFrom())) {
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
                    mediaBox = setMediaControls(mediaView);
                    vbox.getChildren().addAll(userName, message, mediaBox);
                } else {
                    vbox.getChildren().addAll(userName, message);
                }

                cell.setAlignment(Pos.CENTER_RIGHT);
                cell.getChildren().addAll(vbox);
                Platform.runLater(() -> this.setGraphic(cell));
            } else {
                Platform.runLater(() -> this.setGraphic(null));
            }
        }

        private VBox setMediaControls(MediaView mediaView) {
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
                    updateValues();
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
                    updateValues();
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

        protected void updateValues() {
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
            if (mp == null) {
                mp = new MediaPlayer(mediaUrl);
                mediaView.setMediaPlayer(mp);
                mp.setOnError(() -> System.out.println("Error : " + mp.getError().toString()));
            }
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
                if (mp == null) {
                    setVideo(url, mediaView);
                    loadVideo = true;
                }
            }
        }

        private void setVideoSize(String url, MediaView mediaView) {
            try {
                Parent parent = this.getParent();
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

        private void setImageSize(String url, WebView webView) {
            try {
                Parent parent = this.getParent();
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
    }
}

