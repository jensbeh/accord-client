package de.uniks.stp.util;

import com.pavlobu.emojitextflow.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public class EmojiTextFlowExtended extends EmojiTextFlow {
    private static final Logger logger = LoggerFactory.getLogger(EmojiTextFlow.class);
    private EmojiTextFlowParameters parameters;

    public EmojiTextFlowExtended(EmojiTextFlowParameters parameters) {
        super(parameters);
        this.parameters = parameters;
        if (parameters.getTextAlignment() != null) {
            this.setTextAlignment(parameters.getTextAlignment());
        }
    }

    public void addTextLinkNode(String message, String url) {
        message = message.replace(url, "#&!link!&#");

        Queue obs = EmojiParser.getInstance().toEmojiAndText(message);
        while(!obs.isEmpty()) {
            Object ob = obs.poll();
            if (ob instanceof String) {
                String str = handleSpacing(replaceLinkHolder((String)ob, url));
                this.addTextNode(str);
            } else if (ob instanceof Emoji) {
                Emoji emoji = (Emoji)ob;
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

    private String replaceLinkHolder(String text, String url) {
        String replacedString;
        if (text.contains("#&!link!&#")){
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
        return this.getClass().getClassLoader().getResource("emoji_images/" + hexStr + ".png").toExternalForm();
    }
}
