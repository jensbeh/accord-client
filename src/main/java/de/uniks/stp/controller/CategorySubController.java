package de.uniks.stp.controller;

import de.uniks.stp.AlternateServerChannelListCellFactory;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;

public class CategorySubController {
    private Parent view;
    private Categories category;
    private Label categoryName;
    private ListView<Channel> channelList;
    private AlternateServerChannelListCellFactory channelListCellFactory;
    private int ROW_HEIGHT = 30;

    public CategorySubController(Parent view, Categories category) {
        this.view = view;
        this.category = category;
    }

    public void init() {
        categoryName = (Label) view.lookup("#categoryName");
        categoryName.setText(category.getName());
        channelList = (ListView<Channel>) view.lookup("#channellist");
        channelListCellFactory = new AlternateServerChannelListCellFactory();
        channelList.setCellFactory(channelListCellFactory);
        channelList.setItems(FXCollections.observableList(category.getChannel()));
        channelList.setOnMouseClicked(this::onChannelListClicked);
        //PCL
        category.addPropertyChangeListener(Categories.PROPERTY_CHANNEL, this::onChannelChanged);
        category.addPropertyChangeListener(Categories.PROPERTY_NAME, this::onCategoryNameChanged);

        if (category.getChannel().size() > 0) {
            channelList.setPrefHeight(category.getChannel().size() * ROW_HEIGHT);
        } else {
            channelList.setPrefHeight(ROW_HEIGHT);
        }
    }

    /**
     * sets the selectedChat new.
     */
    private void onChannelListClicked(MouseEvent mouseEvent) {
        Channel channel = this.channelList.getSelectionModel().getSelectedItem(); //TODO message-counter reset
        if (mouseEvent.getClickCount() == 2 && this.channelList.getItems().size() != 0 && ServerViewController.getSelectedChat() != channel) {
            channel.setUnreadMessagesCounter(0);
            ServerViewController.setSelectedChat(channel);
            System.out.println(channel.getName());
            channelList.refresh();
            ServerViewController.showMessageView();
        }
    }

    private void onChannelChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> channelList.setItems(FXCollections.observableList(category.getChannel())));
        if (category.getChannel().size() > 0) {
            channelList.setPrefHeight(category.getChannel().size() * ROW_HEIGHT);
        } else {
            channelList.setPrefHeight(ROW_HEIGHT);
        }
    }

    /**
     * sets the new Categoryname
     */
    private void onCategoryNameChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> categoryName.setText(category.getName()));
    }

    public Categories getCategories() {
        return category;
    }

    public void stop() {
        channelList.setOnMouseReleased(null);
        category.removePropertyChangeListener(Categories.PROPERTY_CHANNEL, this::onChannelChanged);
        category.removePropertyChangeListener(Categories.PROPERTY_NAME, this::onCategoryNameChanged);
    }

    public void refreshChannelList() {
        channelList.refresh();
    }
}