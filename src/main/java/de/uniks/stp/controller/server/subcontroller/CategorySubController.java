package de.uniks.stp.controller.server.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.cellfactories.ServerChannelListCell;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.server.ServerViewController;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.ServerChannel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import kong.unirest.JsonNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class CategorySubController {
    private final ServerViewController serverViewController;
    private final Parent view;
    private final ModelBuilder builder;
    private final Categories category;
    private Label categoryName;
    private ListView<ServerChannel> channelList;
    private final int CHANNEL_HEIGHT = 30;
    private final PropertyChangeListener channelListPCL = this::onChannelNameChanged;
    private VBox categoryVbox;

    public CategorySubController(Parent view, ModelBuilder builder, ServerViewController serverViewController, Categories category) {
        this.view = view;
        this.builder = builder;
        this.category = category;
        this.serverViewController = serverViewController;
    }

    @SuppressWarnings("unchecked")
    public void init() {
        categoryName = (Label) view.lookup("#categoryName");
        categoryName.setText(category.getName());
        channelList = (ListView<ServerChannel>) view.lookup("#channelList");
        ServerChannelListCell channelListCellFactory = new ServerChannelListCell(serverViewController);
        ServerChannelListCell.setTheme(builder.getTheme());
        channelList.setCellFactory(channelListCellFactory);
        channelList.setOnMouseClicked(this::onChannelListClicked);
        //PCL
        category.addPropertyChangeListener(Categories.PROPERTY_CHANNEL, this::onChannelChanged);
        category.addPropertyChangeListener(Categories.PROPERTY_NAME, this::onCategoryNameChanged);

        for (ServerChannel channel : category.getChannel()) {
            channel.addPropertyChangeListener(ServerChannel.PROPERTY_NAME, this.channelListPCL);
        }
        refreshChannelList();
    }

    /**
     * sets the selectedChat new.
     */
    private void onChannelListClicked(MouseEvent mouseEvent) {
        ServerChannel channel = this.channelList.getSelectionModel().getSelectedItem();
        // TextChannel
        if (mouseEvent.getClickCount() == 2 && this.channelList.getItems().size() != 0 && serverViewController.getCurrentChannel() != channel && channel.getType().equals("text")) {
            channel.setUnreadMessagesCounter(0);
            serverViewController.setCurrentChannel(channel);
            serverViewController.refreshAllChannelLists();
            serverViewController.showMessageView();
        }

        // AudioChannel
        // when no other is connected
        if (mouseEvent.getClickCount() == 2 && this.channelList.getItems().size() != 0 && builder.getCurrentAudioChannel() != channel && channel.getType().equals("audio")) {
            if (builder.getCurrentAudioChannel() == null) {
                builder.getRestClient().joinVoiceChannel(builder.getCurrentServer().getId(), category.getId(), channel.getId(), builder.getPersonalUser().getUserKey(), response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        System.out.println(body);
                    }
                });
            }
            // when audioChannel is connected - leave old one, join new one
            else {
                builder.getRestClient().leaveVoiceChannel(builder.getCurrentAudioChannel().getCategories().getServer().getId(), builder.getCurrentAudioChannel().getCategories().getId(), builder.getCurrentAudioChannel().getId(), builder.getPersonalUser().getUserKey(), response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        builder.getRestClient().joinVoiceChannel(builder.getCurrentServer().getId(), category.getId(), channel.getId(), builder.getPersonalUser().getUserKey(), response2 -> {
                            JsonNode body2 = response2.getBody();
                            String status2 = body2.getObject().getString("status");
                            if (status2.equals("success")) {
                                System.out.println(body2);
                            }
                        });
                    }
                });
            }
        }
    }

    private void onChannelChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> channelList.setItems(FXCollections.observableList(category.getChannel())));
        if (category.getChannel().size() > 0) {
            channelList.setPrefHeight(category.getChannel().size() * CHANNEL_HEIGHT);
            for (ServerChannel channel : category.getChannel()) {
                /*TODO if newValue not null -> add PCL, else if null -> removePCL*/
                //ServerChannel theChannel = (ServerChannel) propertyChangeEvent.getNewValue();
                channel.removePropertyChangeListener(this.channelListPCL);
                channel.addPropertyChangeListener(ServerChannel.PROPERTY_NAME, this.channelListPCL);
            }
        } else {
            channelList.setPrefHeight(CHANNEL_HEIGHT);
        }
    }

    /**
     * sets the new Category name
     */
    private void onCategoryNameChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> categoryName.setText(category.getName()));
    }

    private void onChannelNameChanged(PropertyChangeEvent propertyChangeEvent) {
        channelList.refresh();
    }

    public Categories getCategories() {
        return category;
    }

    public void stop() {
        channelList.setOnMouseReleased(null);
        category.removePropertyChangeListener(Categories.PROPERTY_CHANNEL, this::onChannelChanged);
        category.removePropertyChangeListener(Categories.PROPERTY_NAME, this::onCategoryNameChanged);

        for (ServerChannel channel : category.getChannel()) {
            channel.removePropertyChangeListener(this.channelListPCL);
        }
    }

    /**
     * refreshes the current category view with all channels in dependent on audioChannel user size
     */
    public void refreshChannelList() {
        if (category.getChannel().size() > 0) {
            int AUDIO_CHANNEL_HEIGHT = 0;
            for (ServerChannel audioChannel : category.getChannel()) {
                if (audioChannel.getAudioMember().size() > 0) {
                    AUDIO_CHANNEL_HEIGHT = AUDIO_CHANNEL_HEIGHT + 25 * audioChannel.getAudioMember().size();
                }
            }
            this.channelList.setPrefHeight(10 + category.getChannel().size() * CHANNEL_HEIGHT + AUDIO_CHANNEL_HEIGHT);
        } else {
            this.channelList.setPrefHeight(CHANNEL_HEIGHT);
        }

        Platform.runLater(() -> this.channelList.setItems(FXCollections.observableList(category.getChannel())));
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        view.getStylesheets().clear();
        view.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/CategorySubView.css")).toExternalForm());
    }

    private void setDarkMode() {
        view.getStylesheets().clear();
        view.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/CategorySubView.css")).toExternalForm());
    }
}
