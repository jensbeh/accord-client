package de.uniks.stp;

import de.uniks.stp.controller.ServerViewController;
import de.uniks.stp.model.Channel;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.lang.reflect.Field;

public class AlternateServerChannelListCellFactory implements javafx.util.Callback<ListView<Channel>, ListCell<Channel>> {
    private static ListView<Channel> channelListView;

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
        this.channelListView = param;
        return new ChannelListCell();
    }

    private static class ChannelListCell extends ListCell<Channel> {

        private boolean isScrollBarVisible;

        protected void updateItem(Channel item, boolean empty) {
            // creates a HBox for each cell of the listView
            VBox cell = new VBox();
            Label name = new Label();
            Label message = new Label();
            HBox nameCell = new HBox();
            HBox notificationCell = new HBox();
            HBox nameAndNotificationCell = new HBox();

            // get visibility of scrollbar
            try {
                VBox vBox = (VBox) channelListView.getParent().getParent();
                ScrollPane scrollPane = (ScrollPane) vBox.getParent().getParent().getParent();

                ScrollPaneSkin skin = (ScrollPaneSkin) scrollPane.getSkin();
                Field field = skin.getClass().getDeclaredField("vsb");

                field.setAccessible(true);
                ScrollBar scrollBar = (ScrollBar) field.get(skin);
                field.setAccessible(false);
                isScrollBarVisible = scrollBar.isVisible();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            super.updateItem(item, empty);
            this.setStyle("-fx-background-color: #23272a;");
            if (!empty) {
                if (item == ServerViewController.getSelectedChat() && isScrollBarVisible) {
                    this.setStyle("-fx-background-color: #666666; -fx-background-radius: 13px; -fx-padding: 0 10 0 0; -fx-border-insets: 0 10 0 0; -fx-background-insets: 0 10 0 0;");
                }
                if (item == ServerViewController.getSelectedChat() && !isScrollBarVisible) {
                    this.setStyle("-fx-background-color: #666666; -fx-background-radius: 13px;");
                }

                // init complete cell
                cell.setId("cell_" + item.getId());
                cell.setPrefWidth(USE_COMPUTED_SIZE);
                cell.setPrefHeight(30);
                cell.setAlignment(Pos.CENTER_LEFT);

                nameAndNotificationCell.setSpacing(9);

                // init userName cell
                nameCell.setPrefHeight(USE_COMPUTED_SIZE);
                nameCell.setAlignment(Pos.CENTER_LEFT);
                if (isScrollBarVisible) {
                    nameCell.setPrefWidth(140);
                } else {
                    nameCell.setPrefWidth(150);
                }
                nameCell.setStyle("-fx-padding: 0 0 0 20;");

                // init notificationCell cell
                notificationCell.setAlignment(Pos.CENTER);
                float notificationCircleSize = 20;
                notificationCell.setMinHeight(notificationCircleSize);
                notificationCell.setMaxHeight(notificationCircleSize);
                notificationCell.setMinWidth(notificationCircleSize);
                notificationCell.setMaxWidth(notificationCircleSize);
                notificationCell.setStyle("-fx-padding: 3 0 0 0;");

                // set channelName
                name.setId(item.getId());
                name.setText("\uD83D\uDD8A  " + item.getName());
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
                name.setTextFill(Paint.valueOf("#FFFFFF"));
                nameCell.getChildren().add(name);

                /*
                // set lastMessage
                if (item.getMessage().size() > 0) {
                    message.setId("msg_" + item.getId());
                    message.setPrefWidth(USE_COMPUTED_SIZE);
                    message.setStyle("-fx-font-size: 11;  -fx-padding: 0 10 0 10;");
                    message.setText(item.getMessage().get(item.getMessage().size() - 1).getMessage());
                    message.setTextFill(Paint.valueOf("#D0D0D0"));
                }*/

                // set notification color & count
                //if (item.getUnreadMessagesCounter() > 0) {
                Circle background = new Circle(notificationCircleSize / 2, Paint.valueOf("#bd7b78"));
                Circle foreground = new Circle(notificationCircleSize / 2 - 1, Paint.valueOf("#f3cdcd"));
                background.setId("notificationCounterBackground_" + item.getId());
                foreground.setId("notificationCounterForeground_" + item.getId());

                Label numberText = new Label();
                numberText.setId("notificationCounter_" + item.getId());
                numberText.setAlignment(Pos.CENTER);
                numberText.setTextFill(Color.BLACK);
                numberText.setText("4");//String.valueOf(item.getUnreadMessagesCounter()));

                StackPane stackPaneUnreadMessages = new StackPane(background, foreground, numberText);
                stackPaneUnreadMessages.setAlignment(Pos.CENTER);

                notificationCell.getChildren().add(stackPaneUnreadMessages);
                //}

                // set cells finally
                nameAndNotificationCell.getChildren().addAll(nameCell, notificationCell);

                cell.getChildren().addAll(nameAndNotificationCell);
            }
            this.setGraphic(cell);
        }
    }
}