package de.uniks.stp.cellfactories;

import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.util.EmojiTextFlowExtended;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class PrivateChatListCell implements javafx.util.Callback<javafx.scene.control.ListView<de.uniks.stp.model.PrivateChat>, javafx.scene.control.ListCell<de.uniks.stp.model.PrivateChat>> {
    private final ModelBuilder builder;

    public PrivateChatListCell(ModelBuilder builder) {
        this.builder = builder;
    }

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
    public ListCell<PrivateChat> call(ListView<PrivateChat> param) {
        return new ChannelListCell();
    }

    private class ChannelListCell extends ListCell<PrivateChat> {

        protected void updateItem(PrivateChat item, boolean empty) {
            // creates a HBox for each cell of the listView
            VBox cell = new VBox();
            super.updateItem(item, empty);
            if (!empty) {
                float notificationCircleSize = 20;
                HBox nameAndNotificationCell = new HBox();
                HBox nameCell = new HBox();
                HBox lastMessageCell = lastMessageCell();
                HBox notificationCell = notificationCell(notificationCircleSize);

                addMouseEvents(cell, item);
                cell.setId("cell_" + item.getId());
                cell.setPrefHeight(USE_COMPUTED_SIZE);
                cell.setPrefWidth(179);
                // init name + notification cell
                nameAndNotificationCell.setSpacing(5);
                // init userName cell
                nameCell.setPrefWidth(159);
                nameCell.setAlignment(Pos.CENTER_LEFT);
                nameCell.getChildren().add(name(item));
                // set lastMessage
                if (item.getMessage().size() > 0) {
                    lastMessageCell.getChildren().add(getLastMessage(item));
                } // set chatColor - if selected / else not selected
                if (builder.getCurrentPrivateChat() != null && builder.getCurrentPrivateChat().getName().equals(item.getName())) {
                    cell.getStyleClass().clear();
                    cell.getStyleClass().add("selectedChat");
                } else { //Unselected Chat Color
                    cell.getStyleClass().clear();
                    cell.getStyleClass().add("unselectedChat");
                } // set notification color & count
                if (item.getUnreadMessagesCounter() > 0) {
                    notificationCell.getChildren().add(unreadMessageCounter(notificationCircleSize, item));
                } // set cells finally
                nameAndNotificationCell.getChildren().addAll(nameCell, notificationCell);
                cell.getChildren().addAll(nameAndNotificationCell, lastMessageCell);
            }
            this.setGraphic(cell);
        }

        private Node unreadMessageCounter(float notificationCircleSize, PrivateChat item) {
            Circle background = new Circle(notificationCircleSize / 2);
            Circle foreground = new Circle(notificationCircleSize / 2 - 1);

            //IDs to use CSS styling
            background.getStyleClass().clear();
            foreground.getStyleClass().clear();
            background.getStyleClass().add("notificationCounterBackground");
            foreground.getStyleClass().add("notificationCounterForeground");
            background.setId("notificationCounterBackground_" + item.getId());
            foreground.setId("notificationCounterForeground_" + item.getId());

            Label numberText = new Label();
            numberText.getStyleClass().clear();
            numberText.getStyleClass().add("numberTextStyle");
            numberText.setId("notificationCounter_" + item.getId());
            numberText.setAlignment(Pos.CENTER);
            numberText.setText(String.valueOf(item.getUnreadMessagesCounter()));

            StackPane stackPaneUnreadMessages = new StackPane(background, foreground, numberText);
            stackPaneUnreadMessages.setAlignment(Pos.CENTER);
            return stackPaneUnreadMessages;
        }

        private Label name(PrivateChat item) {
            Label name = new Label();
            name.setId(item.getId());
            name.getStyleClass().clear();
            name.getStyleClass().add("name");
            name.setText(item.getName());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-padding: 5 0 0 10; -fx-text-fill: white;");
            return name;
        }

        private HBox lastMessageCell() {
            HBox lastMessageCell = new HBox();
            lastMessageCell.setPrefWidth(179);
            lastMessageCell.setAlignment(Pos.CENTER_LEFT);
            lastMessageCell.setStyle("-fx-padding: 5 0 0 0");

            return lastMessageCell;
        }

        private HBox getLastMessage(PrivateChat item) {
            HBox textBox = new HBox();
            textBox.setPrefWidth(179);
            textBox.setStyle("-fx-padding: 0 10 0 10;");

            EmojiTextFlowExtended message = handleEmojis();
            message.setId("msg_" + item.getId());
            String textMessage = item.getMessage().get(item.getMessage().size() - 1).getMessage();
            message.setId("messageLabel");
            message.parseAndAppend(textMessage);
            message = setMaxChar(message);

            textBox.getChildren().add(message);
            return textBox;
        }

        private EmojiTextFlowExtended handleEmojis() {
            EmojiTextFlowParameters emojiTextFlowParameters;
            {
                emojiTextFlowParameters = new EmojiTextFlowParameters();
                emojiTextFlowParameters.setEmojiScaleFactor(1D);
                emojiTextFlowParameters.setTextAlignment(TextAlignment.LEFT);
                emojiTextFlowParameters.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                emojiTextFlowParameters.setTextColor(Color.WHITE);
            }
            return new EmojiTextFlowExtended(emojiTextFlowParameters);
        }

        /**
         * This method cuts the EmojiTextFlow by a length
         * @param message the message to be shorten
         * @return the shorten message
         */
        private EmojiTextFlowExtended setMaxChar(EmojiTextFlowExtended message) {
            int length = 13;
            EmojiTextFlowExtended newMessage = handleEmojis();

            int messageLength = 0;
            for (Node T : message.getChildren()) {
                if (T instanceof ImageView) {
                    // an emoji represents one character
                    messageLength++;
                } else if (T instanceof Text) {
                    messageLength += ((Text) T).getText().length();
                }
            }

            boolean shorted = false;
            if (messageLength > length) {
                length -= 1;
                shorted = true;
            }

            int counter = 0;
            for (int x = 0; x < message.getChildren().size(); x++) {
                Node T = message.getChildren().get(x);
                // child is emoji
                if (T instanceof ImageView) {
                    if (counter < length) {
                        ImageView image = new ImageView();
                        image.setImage(((ImageView) T).getImage());
                        image.setFitHeight(((ImageView) T).getFitHeight());
                        image.setFitWidth(((ImageView) T).getFitWidth());
                        newMessage.getChildren().add(image);
                        counter++;
                    }
                } else if (T instanceof Text) {
                    Text text = new Text("");
                    text.setTextAlignment(((Text) T).getTextAlignment());
                    text.setFont(((Text) T).getFont());
                    text.setFill(((Text) T).getFill());
                    String oldText = ((Text) T).getText();

                    for (int i = 0; i < oldText.length(); i++) {
                        if (counter < length) {
                            text.setText(text.getText() + oldText.charAt(i));
                            counter++;
                        } else {
                            break;
                        }
                    }
                    newMessage.getChildren().add(text);
                }
            }

            // if message is shorted, show that by appending ".."
            if (shorted) {
                Text t = new Text("..");
                t.setTextAlignment(TextAlignment.LEFT);
                t.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                t.setFill(Color.WHITE);
                newMessage.getChildren().add(t);
            }
            return newMessage;
        }

        private HBox notificationCell(float notificationCircleSize) {
            HBox notificationCell = new HBox();
            notificationCell.setId("notification");
            notificationCell.setAlignment(Pos.CENTER);
            notificationCell.setMinHeight(notificationCircleSize);
            notificationCell.setMaxHeight(notificationCircleSize);
            notificationCell.setMinWidth(notificationCircleSize);
            notificationCell.setMaxWidth(notificationCircleSize);
            notificationCell.setStyle("-fx-padding: 15 15 0 0;");
            return notificationCell;
        }

        private void addMouseEvents(VBox cell, PrivateChat item) {
            cell.setOnMouseEntered(event -> {
                if (builder.getCurrentPrivateChat() == null || !builder.getCurrentPrivateChat().getName().equals(item.getName())) {
                    cell.getStyleClass().clear();
                    cell.getStyleClass().add("unselectedChatHover");
                }
            });
            cell.setOnMouseExited(event -> {
                if (builder.getCurrentPrivateChat() == null || !builder.getCurrentPrivateChat().getName().equals(item.getName())) {
                    cell.getStyleClass().clear();
                    cell.getStyleClass().add("unselectedChat");
                }
            });
        }
    }
}
