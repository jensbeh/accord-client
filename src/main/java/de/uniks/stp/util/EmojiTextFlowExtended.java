package de.uniks.stp.util;

import com.pavlobu.emojitextflow.EmojiParser;
import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmojiTextFlowExtended extends EmojiTextFlow {
    private EmojiTextFlowParameters parameters;

    public EmojiTextFlowExtended(EmojiTextFlowParameters parameters) {
        super(parameters);
        this.parameters = parameters;
        if (parameters.getTextAlignment() != null) {
            this.setTextAlignment(parameters.getTextAlignment());
        }
    }

    public void addTextLinkNode(String text, int firstIndex, int lastIndex) {
        System.out.println(firstIndex + " last " + lastIndex);
        Text textNode = new Text();
        textNode.setText(text);
        textNode.setFont(this.parameters.getFont());
        if (this.parameters.getTextColor() != null) {
            textNode.setFill(this.parameters.getTextColor());
        }

        this.getChildren().add(textNode);
    }
}
