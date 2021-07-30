package de.uniks.stp.cellfactories;

import de.uniks.stp.controller.server.ServerViewController;
import de.uniks.stp.model.AudioMember;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.lang.reflect.Field;

public class ServerChannelListCell implements javafx.util.Callback<ListView<ServerChannel>, ListCell<ServerChannel>> {
    private ListView<ServerChannel> channelListView;
    private final ServerViewController serverViewController;

    public ServerChannelListCell(ServerViewController serverViewController) {
        this.serverViewController = serverViewController;
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
    public ListCell<ServerChannel> call(ListView<ServerChannel> param) {
        this.channelListView = param;
        return new ChannelListCell();
    }

    private class ChannelListCell extends ListCell<ServerChannel> {

        private boolean isScrollBarVisible;
        private int audioMemberCount = 0;

        protected void updateItem(ServerChannel item, boolean empty) {
            // creates a HBox for each cell of the listView
            VBox cell = new VBox();
            Label name = new Label();
            VBox channelCell = new VBox();
            HBox channelNameCell = new HBox();
            VBox channelAudioUserCell = new VBox();
            HBox notificationCell = new HBox();
            HBox nameAndNotificationCell = new HBox();


            // get visibility of scrollbar
            try {
                VBox vBox = (VBox) channelListView.getParent().getParent();
                if (vBox != null) {
                    ScrollPane scrollPane = (ScrollPane) vBox.getParent().getParent().getParent();

                    ScrollPaneSkin skin = (ScrollPaneSkin) scrollPane.getSkin();
                    Field field = skin.getClass().getDeclaredField("vsb");

                    field.setAccessible(true);
                    ScrollBar scrollBar = (ScrollBar) field.get(skin);
                    field.setAccessible(false);
                    isScrollBarVisible = scrollBar.isVisible();
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            super.updateItem(item, empty);
            if (!empty) {

                cell.setOnMouseEntered(event -> {
                    if (item != serverViewController.getCurrentChannel() && item != serverViewController.getCurrentAudioChannel()) {
                        if (isScrollBarVisible) {
                            this.setId("channelScrollbarTrueUnselectedHover");
                        }
                        if (!isScrollBarVisible) {
                            this.setId("channelScrollbarFalseUnselectedHover");
                        }
                    }
                });
                cell.setOnMouseExited(event -> {
                    if (item != serverViewController.getCurrentChannel() && item.getType().equals("text")) {
                        if (isScrollBarVisible) {
                            this.setId("textChannelScrollbarTrueUnselected");
                        }
                        if (!isScrollBarVisible) {
                            this.setId("textChannelScrollbarFalseUnselected");
                        }
                    }
                    if (item != serverViewController.getCurrentAudioChannel() && item.getType().equals("audio")) {
                        if (isScrollBarVisible) {
                            this.setId("audioChannelScrollbarTrueUnselected");
                        }
                        if (!isScrollBarVisible) {
                            this.setId("audioChannelScrollbarFalseUnselected");
                        }
                    }
                });
                if (item == serverViewController.getCurrentChannel()) {
                    if (item == serverViewController.getCurrentChannel() && isScrollBarVisible) {
                        this.setId("textChannelScrollbarTrueSelected");
                    }
                    if (item == serverViewController.getCurrentChannel() && !isScrollBarVisible) {
                        this.setId("textChannelScrollbarFalseSelected");
                    }
                } else {
                    if (item != serverViewController.getCurrentChannel() && isScrollBarVisible) {
                        this.setId("textChannelScrollbarTrueUnselected");
                    }
                    if (item != serverViewController.getCurrentChannel() && !isScrollBarVisible) {
                        this.setId("textChannelScrollbarFalseUnselected");
                    }
                }

                if (item == serverViewController.getCurrentAudioChannel()) {
                    if (item == serverViewController.getCurrentAudioChannel() && isScrollBarVisible) {
                        this.setId("audioChannelScrollbarTrueSelected");
                    }
                    if (item == serverViewController.getCurrentAudioChannel() && !isScrollBarVisible) {
                        this.setId("audioChannelScrollbarFalseSelected");
                    }
                } else {
                    if (item == serverViewController.getCurrentAudioChannel() && isScrollBarVisible) {
                        this.setId("audioChannelScrollbarTrueUnselected");
                    }
                    if (item == serverViewController.getCurrentAudioChannel() && !isScrollBarVisible) {
                        this.setId("audioChannelScrollbarFalseUnselected");
                    }
                }
                // init complete cell
                cell.setId("cell_" + item.getId());
                cell.setMaxWidth(169);
                cell.setAlignment(Pos.CENTER_LEFT);

                nameAndNotificationCell.setSpacing(9);

                // init channel cell
                if (isScrollBarVisible) {
                    channelCell.setPrefWidth(140);
                    cell.setMaxWidth(164);
                } else {
                    channelCell.setPrefWidth(150);
                }

                // init channelName cell
                channelNameCell.setPrefHeight(USE_COMPUTED_SIZE);
                channelNameCell.setAlignment(Pos.CENTER_LEFT);
                channelNameCell.setStyle("-fx-padding: 0 10 0 20;");

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
                name.getStyleClass().clear();
                name.getStyleClass().add("channelName");
                if (item.getType() != null) {
                    if (item.getType().equals("text")) {
                        name.setText("\uD83D\uDD8A  " + item.getName());
                    } else if (item.getType().equals("audio")) {
                        name.setText("\uD83D\uDD0A  " + item.getName());
                    }
                } else {
                    name.setText("\uD83D\uDD8A  " + item.getName());
                }
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
                channelNameCell.getChildren().add(name);

                //initialize ContextMenu
                ContextMenu menu = new ContextMenu();
                MenuItem mute = new MenuItem("mute");
                MenuItem unMute = new MenuItem("unmute");
                menu.setId("AudioMemberControl");
                mute.setId("muteAudioMember");
                mute.setId("unMuteAudioMember");
                menu.getItems().addAll(mute, unMute);
                unMute.setVisible(false);
                mute.setVisible(false);
                if (serverViewController.getTheme().equals("Dark")) {
                    menu.setStyle("-fx-background-color: #23272a");
                    mute.setStyle("-fx-text-fill: #FFFFFF");
                    unMute.setStyle("-fx-text-fill: #FFFFFF");
                } else {
                    menu.setStyle("-fx-background-color: White");
                    mute.setStyle("-fx-text-fill: #000000");
                    unMute.setStyle("-fx-text-fill: #000000");
                }

                // channel is audioChannel
                if (item.getType().equals("audio")) {
                    channelAudioUserCell.setStyle("-fx-padding: 0 10 0 45;");
                    audioMemberCount = 0;

                    for (AudioMember audioMember : item.getAudioMember()) {
                        for (User user : item.getCategories().getServer().getUser()) {
                            if (audioMember.getId().equals(user.getId())) {
                                Label audioMemberName = new Label();
                                audioMemberName.setId("audioMember");
                                audioMemberName.getStyleClass().clear();
                                audioMemberName.getStyleClass().add("audioMember");
                                audioMemberName.setStyle("-fx-font-size: 14");
                                HBox audioMemberCell = new HBox();
                                audioMemberCell.setPrefHeight(25);
                                audioMemberName.setText(user.getName());
                                audioMemberCell.getChildren().add(audioMemberName);
                                channelAudioUserCell.getChildren().add(audioMemberCell);

                                //set ContextMenu when user is not personal user
                                if (!user.getId().equals(serverViewController.getServer().getCurrentUser().getId())) {
                                    audioMemberName.setContextMenu(menu);
                                }

                                //set mute option only when currentUser in the same AudioChannel
                                boolean currentUserAudio = false;
                                for (AudioMember member : item.getAudioMember()) {
                                    if (member.getId().equals(serverViewController.getServer().getCurrentUser().getId())) {
                                        currentUserAudio = true;
                                        break;
                                    }
                                }
                                if (currentUserAudio) {
                                    mute.setVisible(true);
                                    unMute.setVisible(false);
                                } else {
                                    audioMemberName.setContextMenu(null);
                                    if (serverViewController.getMutedAudioMember().contains(user.getName())) {
                                        audioMemberName.setText(user.getName());
                                        serverViewController.setUnMutedAudioMember(user.getName());
                                    }
                                }

                                if (serverViewController.getMutedAudioMember().contains(user.getName())) {
                                    unMute.setVisible(true);
                                    mute.setVisible(false);
                                    audioMemberName.setText("\uD83D\uDD07 " + user.getName());
                                }

                                //set on action from contextMenu in action from audioMemberCell to get the selected user
                                audioMemberCell.setOnMouseClicked((ae) -> {
                                    mute.setOnAction((act) -> {
                                        audioMemberName.setText("\uD83D\uDD07 " + user.getName());
                                        serverViewController.setMutedAudioMember(user.getName());
                                        unMute.setVisible(true);
                                        mute.setVisible(false);
                                    });
                                    unMute.setOnAction((act) -> {
                                        audioMemberName.setText(user.getName());
                                        serverViewController.setUnMutedAudioMember(user.getName());
                                        unMute.setVisible(false);
                                        mute.setVisible(true);
                                    });
                                });
                                audioMemberCount++;
                            }
                        }
                    }
                }

                channelCell.getChildren().addAll(channelNameCell, channelAudioUserCell);

                // set notification color & count
                if (item.getUnreadMessagesCounter() > 0) {
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
                    numberText.setId("notificationCounter_" + item.getId());
                    numberText.getStyleClass().clear();
                    numberText.getStyleClass().add("notificationCounter");
                    numberText.setAlignment(Pos.CENTER);
                    numberText.setTextFill(Color.BLACK);
                    numberText.setText(String.valueOf(item.getUnreadMessagesCounter()));

                    StackPane stackPaneUnreadMessages = new StackPane(background, foreground, numberText);
                    stackPaneUnreadMessages.setAlignment(Pos.CENTER);

                    notificationCell.getChildren().add(stackPaneUnreadMessages);
                }

                // set cells finally
                if (item.getType().equals("audio")) {
                    cell.getChildren().addAll(channelCell);
                    cell.setPrefHeight(28 + 25 * audioMemberCount);
                } else {
                    nameAndNotificationCell.getChildren().addAll(channelCell, notificationCell);
                    cell.getChildren().addAll(nameAndNotificationCell);
                    cell.setPrefHeight(28);
                }
            }
            this.setGraphic(cell);
        }
    }
}