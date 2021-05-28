package de.uniks.stp.controller;

import de.uniks.stp.AlternateChannelListCellFactory2;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;

public class CategorySubController {
    private Parent view;
    private ModelBuilder builder;
    private Categories category;
    private Label categoryName;
    private ListView<Channel> channelList;
    private AlternateChannelListCellFactory2 channeListCellFactory;

    public CategorySubController(Parent view, ModelBuilder builder, Categories c) {
        this.view = view;
        this.builder = builder;
        this.category = c;
    }

    public void init() {
        categoryName = (Label) view.lookup("#categoryName");
        categoryName.setText(category.getName());
        channelList = (ListView<Channel>) view.lookup("#channellist");
        channeListCellFactory = new AlternateChannelListCellFactory2();
        channelList.setCellFactory(channeListCellFactory);
        channelList.setItems(FXCollections.observableList(category.getChannel()));
        channelList.setOnMouseClicked(this::onChannelListClicked);
        //PCL
        category.addPropertyChangeListener(Categories.PROPERTY_CHANNEL, this::onChannelchanged);

    }

    /**
     * sets the selectedChat new.
     */
    private void onChannelListClicked(MouseEvent mouseEvent) {
        Channel channel = this.channelList.getSelectionModel().getSelectedItem();
        if (mouseEvent.getClickCount() == 2 && this.channelList.getItems().size() != 0 && ServerViewController.getSelectedChat() != channel) {
            ServerViewController.setSelectedChat(channel);
            System.out.println(channel.getName());
            channelList.refresh();
        }
    }

    private void onChannelchanged(PropertyChangeEvent propertyChangeEvent) {
        channelList.refresh();
    }

    public void stop() {
        channelList.setOnMouseReleased(null);
        category.removePropertyChangeListener(this::onChannelchanged);
    }
}
