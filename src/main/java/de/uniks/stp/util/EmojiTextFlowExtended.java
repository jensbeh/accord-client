package de.uniks.stp.util;

import com.pavlobu.emojitextflow.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;

public class EmojiTextFlowExtended extends EmojiTextFlow {
    /**
     * This class extends the already existing EmojiTextFlow. Its purpose is to handle links, so it is not shown as an emoji.
     */
    private static final Logger logger = LoggerFactory.getLogger(EmojiTextFlow.class);
    private final EmojiTextFlowParameters parameters;

    public EmojiTextFlowExtended(EmojiTextFlowParameters parameters) {
        super(parameters);
        this.parameters = parameters;
        if (parameters.getTextAlignment() != null) {
            this.setTextAlignment(parameters.getTextAlignment());
        }
    }

    public void convertToUnicode(String message, String url) {
        if (url != null) {
            message = message.replace(url, "#&!link!&#");
        }
        Queue<Object> obs = EmojiParser.getInstance().toEmojiAndText(message);
        while (!obs.isEmpty()) {
            Object ob = obs.poll();
            if (ob instanceof String) {
                String str;
                if (url != null) {
                    str = replaceLinkHolder((String) ob, url);
                } else {
                    str = (String) ob;
                }
                this.addTextNode(str);
            } else if (ob instanceof Emoji) {
                Emoji emoji = (Emoji) ob;
                this.addTextNode(emoji.getUnicode());
            }
        }
    }

    public void addTextLinkNode(String message, String url) {
        message = message.replace(url, "#&!link!&#");

        Queue<Object> obs = EmojiParser.getInstance().toEmojiAndText(message);
        while (!obs.isEmpty()) {
            Object ob = obs.poll();
            if (ob instanceof String) {
                String str = handleSpacing(replaceLinkHolder((String) ob, url));
                this.addTextNode(str);
            } else if (ob instanceof Emoji) {
                Emoji emoji = (Emoji) ob;
                try {
                    this.addEmojiImageNode(this.createEmojiImageNode(emoji));
                } catch (NullPointerException var6) {
                    logger.error("Image with hex code: " + emoji.getHex() + " appear not to exist in resources path");
                    var6.printStackTrace();
                    this.addTextNode(emoji.getUnicode());
                }
            }
        }
    }


    private String handleSpacing(String str) {
        //new Line after 50 Characters
        int maxLen = 41;
        int point = 0;
        int endPoint;
        int length = str.length();
        while ((point + maxLen) < length) {
            endPoint = point + maxLen;

            str = new StringBuilder(str).insert(endPoint, "\n").toString();
            length += 2;
            point = endPoint + 2;
        }
        return str;
    }

    private String replaceLinkHolder(String text, String url) {
        String replacedString;
        if (text.contains("#&!link!&#")) {
            replacedString = text.replace("#&!link!&#", url);
        } else {
            replacedString = text;
        }
        return replacedString;
    }

    private ImageView createEmojiImageNode(Emoji emoji) throws NullPointerException {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(this.parameters.getEmojiFitWidth());
        imageView.setFitHeight(this.parameters.getEmojiFitHeight());
        imageView.setImage(EmojiImageCache.getInstance().getImage(this.getEmojiImagePath(emoji.getHex())));
        return imageView;
    }

    private void addEmojiImageNode(ImageView emojiImageNode) {
        this.getChildren().add(emojiImageNode);
    }

    private void addTextNode(String text) {
        Text textNode = new Text();
        textNode.setText(text);
        textNode.setFont(this.parameters.getFont());
        if (this.parameters.getTextColor() != null) {
            textNode.setFill(this.parameters.getTextColor());
        }

        this.getChildren().add(textNode);
    }

    private String getEmojiImagePath(String hexStr) throws NullPointerException {
        return Objects.requireNonNull(this.getClass().getClassLoader().getResource("emoji_images/" + hexStr + ".png")).toExternalForm();
    }
}
