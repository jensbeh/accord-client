package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.StringConverter;

public class ServerSettingsChannelController extends SubSetting {
    private Parent view;
    private ModelBuilder builder;
    private Server server;

    private Label categoryLabel;
    private ComboBox<Categories> categorySelector;
    private Label editChannelsLabel;
    private ComboBox<Channel> editChannelsSelector;
    private TextField editChannelsTextField;
    private Button channelChangeButton;
    private Button channelDeleteButton;
    private Label createChannelLabel;
    private TextField createChannelTextField;
    private RadioButton channelTextRadioButton;
    private RadioButton channelVoiceRadioButton;
    private Button channelCreateButton;

    private Categories selectedCategory;
    private Channel selectedChannel;


    public ServerSettingsChannelController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
    }

    public void init() {
        // init view
        this.categoryLabel = (Label) view.lookup("#categoryLabel");
        this.categorySelector = (ComboBox<Categories>) view.lookup("#categorySelector");
        this.editChannelsLabel = (Label) view.lookup("#editChannelsLabel");
        this.editChannelsSelector = (ComboBox<Channel>) view.lookup("#editChannelsSelector");
        this.editChannelsTextField = (TextField) view.lookup("#editChannelsTextField");
        this.channelChangeButton = (Button) view.lookup("#channelChangeButton");
        this.channelDeleteButton = (Button) view.lookup("#channelDeleteButton");
        this.createChannelLabel = (Label) view.lookup("#createChannelLabel");
        this.createChannelTextField = (TextField) view.lookup("#createChannelTextField");
        this.channelTextRadioButton = (RadioButton) view.lookup("#channelTextRadioButton");
        this.channelVoiceRadioButton = (RadioButton) view.lookup("#channelVoiceRadioButton");
        this.channelCreateButton = (Button) view.lookup("#channelCreateButton");

        this.categorySelector.setOnAction(this::onCategoryChanged);
        this.editChannelsSelector.setOnAction(this::onChannelChanged);

        this.categorySelector.getItems().addAll(builder.getCurrentServer().getCategories());

        this.categorySelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Categories object) {
                if (object == null) {
                    return "Select category...";
                }
                return object.getName();
            }

            @Override
            public Categories fromString(String string) {
                return null;
            }
        });

        this.editChannelsSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Channel object) {
                if (object == null) {
                    return "Select channel...";
                }
                return object.getName();
            }

            @Override
            public Channel fromString(String string) {
                return null;
            }
        });

        disableEditing(true);
    }

    public void stop() {
        categorySelector.setOnAction(null);
        editChannelsSelector.setOnAction(null);
    }

    /**
     * Enable / Disable items in view
     *
     * @param disable true is disabling the items, false is enabling the items
     */
    private void disableEditing(boolean disable) {
        editChannelsLabel.setDisable(disable);
        editChannelsSelector.setDisable(disable);
        editChannelsTextField.setDisable(disable);
        channelChangeButton.setDisable(disable);
        channelDeleteButton.setDisable(disable);
        createChannelLabel.setDisable(disable);
        createChannelTextField.setDisable(disable);
        channelTextRadioButton.setDisable(disable);
        channelVoiceRadioButton.setDisable(disable);
        channelCreateButton.setDisable(disable);
    }

    /**
     * when category changes, enable items in view and load the Channels from the Category
     *
     * @param actionEvent the mouse click event
     */
    private void onCategoryChanged(ActionEvent actionEvent) {
        disableEditing(false);

        selectedCategory = categorySelector.getValue();
        loadChannels();
    }

    /**
     * when channel changes, save the selected Channel in variable
     *
     * @param actionEvent the mouse click event
     */
    private void onChannelChanged(ActionEvent actionEvent) {
        selectedChannel = editChannelsSelector.getValue();
    }

    /**
     * load the Channels from the selected Category
     */
    private void loadChannels() {
        selectedChannel = null;
        editChannelsSelector.getItems().clear();
        editChannelsSelector.getItems().addAll(selectedCategory.getChannel());
    }
}
