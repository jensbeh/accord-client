package de.uniks.stp.cellfactories;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.StageManager;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Message;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

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

    private static class MessageCell extends ListCell<Message> {
        private boolean loadImage;
        private String urlType;

        /**
         * shows message in cell of ListView
         */
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
                WebView webView = new WebView();
                if (!url.equals("") && !url.contains("https://ac.uniks.de/")) {
                    setImage(url, webView.getEngine());
                    if (loadImage) {
                        webView.setContextMenuEnabled(false);
                        setImageSize(url, webView);
                        textMessage = textMessage.replace(url, "");
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

                if (!loadImage) {
                    vbox.getChildren().addAll(userName, message);
                } else {
                    vbox.getChildren().addAll(userName, message, webView);
                }

                cell.setAlignment(Pos.CENTER_RIGHT);
                cell.getChildren().addAll(vbox);

            }
            this.setGraphic(cell);

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
            } else {
                urlType = "None";
            }
            return url;
        }

        private void setImage(String url, WebEngine engine) {
            if (urlType.equals("picture")) {
                engine.load(url);
                loadImage = true;
            } else if (urlType.equals("gif")) {
                engine.loadContent("<html><body><img src=\"" + url + "\" class=\"center\"></body></html>");
                loadImage = true;
            }
            engine.setJavaScriptEnabled(false);
            engine.setUserStyleSheetLocation(Objects.requireNonNull(StageManager.class.getResource("styles/message/webView.css")).toExternalForm());
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
