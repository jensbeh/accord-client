package de.uniks.stp;

import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.Channel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class AlternatePrivateChatListCellFactory implements javafx.util.Callback<javafx.scene.control.ListView<de.uniks.stp.model.Channel>, javafx.scene.control.ListCell<de.uniks.stp.model.Channel>> {
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
    public ListCell<Channel> call(ListView<Channel> param) {
        return new ChannelListCell();
    }

    private static class ChannelListCell extends ListCell<Channel> {

        protected void updateItem(Channel item, boolean empty) {
            // creates a Hbox for each cell of the listView
            VBox cell = new VBox();
            HBox nameAndNotificationCell = new HBox();
            HBox nameCell = new HBox();
            HBox lastMessageCell = new HBox();
            Label name = new Label();
            Label message = new Label();
            HBox notificationCell = new HBox();
            super.updateItem(item, empty);
            this.setStyle("-fx-background-color: #2C2F33; -fx-padding: 2 2 2 2;");
            if (!empty) {
                // init complete cell
                cell.setId("cell_" + item.getId());
                cell.setPrefHeight(USE_COMPUTED_SIZE);
                cell.setPrefWidth(179);

                // init name + notification cell
                nameAndNotificationCell.setSpacing(5);

                // init userName cell
                nameCell.setPrefWidth(159);
                nameCell.setAlignment(Pos.CENTER_LEFT);

                // init lastMessage cell
                lastMessageCell.setPrefWidth(179);
                lastMessageCell.setAlignment(Pos.CENTER_LEFT);
                lastMessageCell.setStyle("-fx-padding: 5 0 0 0");

                // init notificationCell cell
                notificationCell.setAlignment(Pos.CENTER);
                float notificationCircleSize = 20;
                notificationCell.setMinHeight(notificationCircleSize);
                notificationCell.setMaxHeight(notificationCircleSize);
                notificationCell.setMinWidth(notificationCircleSize);
                notificationCell.setMaxWidth(notificationCircleSize);
                notificationCell.setStyle("-fx-padding: 15 15 0 0;");

                // set userName
                name.setId(item.getId());
                name.setText(item.getName());
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-padding: 5 0 0 10;");
                name.setTextFill(Paint.valueOf("#FFFFFF"));
                nameCell.getChildren().add(name);

                // set lastMessage
                if (item.getMessage().size() > 0) {
                    message.setId("msg_" + item.getId());
                    message.setPrefWidth(USE_COMPUTED_SIZE);
                    message.setStyle("-fx-font-size: 15;  -fx-padding: 0 10 0 10;");
                    message.setText(item.getMessage().get(item.getMessage().size() - 1).getMessage());
                    message.setTextFill(Paint.valueOf("#D0D0D0"));
                    lastMessageCell.getChildren().add(message);
                }

                // set chatColor - if selected / else not selected
                if (PrivateViewController.getSelectedChat() != null && PrivateViewController.getSelectedChat().getName().equals(item.getName())) {
                    //cell.setStyle("-fx-background-color: #737373; -fx-border-size: 2px; -fx-border-color: #AAAAAA; -fx-pref-height: 65; -fx-max-width: 183");
                    cell.setStyle("-fx-background-color: #666666; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183");
                } else {
                    //cell.setStyle("-fx-background-color: #2C2F33; -fx-border-size: 2px; -fx-border-color: #AAAAAA; -fx-pref-height: 65; -fx-max-width: 183");
                    cell.setStyle("-fx-background-color: #404040; -fx-background-radius: 13px; -fx-pref-height: 65; -fx-max-width: 183");
                }

                // set notification color & count
                if (item.getUnreadMessagesCounter() > 0) {
                    Circle background = new Circle(notificationCircleSize / 2, Paint.valueOf("#bd7b78"));
                    Circle foreground = new Circle(notificationCircleSize / 2 - 1, Paint.valueOf("#f3cdcd"));
                    background.setId("notificationCounterBackground_" + item.getId());
                    foreground.setId("notificationCounterForeground_" + item.getId());

                    Label numberText = new Label();
                    numberText.setId("notificationCounter_" + item.getId());
                    numberText.setAlignment(Pos.CENTER);
                    numberText.setTextFill(Color.BLACK);
                    numberText.setText(String.valueOf(item.getUnreadMessagesCounter()));

                    StackPane stackPaneUnreadMessages = new StackPane(background, foreground, numberText);
                    stackPaneUnreadMessages.setAlignment(Pos.CENTER);

                    notificationCell.getChildren().add(stackPaneUnreadMessages);
                }

                // set cells finally
                nameAndNotificationCell.getChildren().addAll(nameCell, notificationCell);
                cell.getChildren().addAll(nameAndNotificationCell, lastMessageCell);
            }
            this.setGraphic(cell);
        }
    }
}
