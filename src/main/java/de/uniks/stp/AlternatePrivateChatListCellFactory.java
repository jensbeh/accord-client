package de.uniks.stp;

import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.model.PrivateChat;
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

public class AlternatePrivateChatListCellFactory implements javafx.util.Callback<javafx.scene.control.ListView<de.uniks.stp.model.PrivateChat>, javafx.scene.control.ListCell<de.uniks.stp.model.PrivateChat>> {
    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     * determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */

    private static String theme;

    public static void setTheme(String theme) {
        AlternatePrivateChatListCellFactory.theme = theme;
    }

    @Override
    public ListCell<PrivateChat> call(ListView<PrivateChat> param) {
        return new ChannelListCell();
    }

    private static class ChannelListCell extends ListCell<PrivateChat> {

        protected void updateItem(PrivateChat item, boolean empty) {
            // creates a HBox for each cell of the listView
            VBox cell = new VBox();
            HBox nameAndNotificationCell = new HBox();
            HBox nameCell = new HBox();
            HBox lastMessageCell = new HBox();
            Label name = new Label();
            Label message = new Label();
            HBox notificationCell = new HBox();
            notificationCell.setId("notification");
            super.updateItem(item, empty);

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

                if (theme.equals("Bright")) {
                    name.setTextFill(Paint.valueOf(String.valueOf(Color.BLACK)));
                } else {
                    name.setTextFill(Paint.valueOf("#FFFFFF"));
                }

                nameCell.getChildren().add(name);

                // set lastMessage
                if (item.getMessage().size() > 0) {
                    message.setId("msg_" + item.getId());
                    message.setPrefWidth(USE_COMPUTED_SIZE);
                    message.setStyle("-fx-font-size: 15;  -fx-padding: 0 10 0 10;");
                    message.setText(item.getMessage().get(item.getMessage().size() - 1).getMessage());
                    if (theme.equals("Bright")) {
                        message.setTextFill(Paint.valueOf("#3b3b3b"));
                    } else {
                        message.setTextFill(Paint.valueOf("#D0D0D0"));
                    }
                    lastMessageCell.getChildren().add(message);
                }

                // set chatColor - if selected / else not selected
                if (PrivateViewController.getSelectedChat() != null && PrivateViewController.getSelectedChat().getName().equals(item.getName())) {
                    if (theme.equals("Bright")) {
                        cell.setStyle("-fx-background-color: #bfbfbf; -fx-border-color: #7c7c7c; -fx-border-width: 2px; -fx-border-radius: 13px; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183");
                    } else if (theme.equals("Dark")) {
                        cell.setStyle("-fx-background-color: #999999; -fx-background-radius: 13px;  -fx-pref-height: 65; -fx-max-width: 183");
                    }

                } else {
                    //Unselected Chat Color
                    if (theme.equals("Bright")) {
                        cell.setStyle("-fx-background-color: #e2e2e2; -fx-border-color: #c1c1c1; -fx-border-width: 1.5px; -fx-border-radius: 13px; -fx-background-radius: 13px; -fx-pref-height: 65; -fx-max-width: 183");
                    } else if (theme.equals("Dark")) {
                        cell.setStyle("-fx-background-color: #404040; -fx-background-radius: 13px; -fx-pref-height: 65; -fx-max-width: 183");
                    }
                }

                // set notification color & count
                if (item.getUnreadMessagesCounter() > 0) {

                    Circle background = null;
                    Circle foreground = null;

                    if (theme.equals("Bright")) {
                        background = new Circle(notificationCircleSize / 2, Paint.valueOf("#5e7da8"));
                        foreground = new Circle(notificationCircleSize / 2 - 1, Paint.valueOf("#7da6df"));
                        background.setId("notificationCounterBackground_" + item.getId());
                        foreground.setId("notificationCounterForeground_" + item.getId());
                    } else {
                        background = new Circle(notificationCircleSize / 2, Paint.valueOf("#bd7b78"));
                        foreground = new Circle(notificationCircleSize / 2 - 1, Paint.valueOf("#f3cdcd"));
                        background.setId("notificationCounterBackground_" + item.getId());
                        foreground.setId("notificationCounterForeground_" + item.getId());
                    }

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
