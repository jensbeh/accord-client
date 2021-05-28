package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import kong.unirest.JsonNode;
import org.json.JSONArray;

import java.util.ArrayList;

public class ServerSettingsChannelController extends SubSetting {
    private Parent view;
    private ModelBuilder builder;
    private Server server;
    private RestClient restClient;

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
        this.restClient = new RestClient();
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
        this.channelChangeButton.setOnAction(this::onChannelChangeButtonClicked);

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
        channelChangeButton.setOnAction(null);
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

    /**
     * when clicking change button, change the channel name
     *
     * @param actionEvent the mouse click event
     */
    private void onChannelChangeButtonClicked(ActionEvent actionEvent) {
        if (selectedChannel != null && !editChannelsTextField.getText().isEmpty()) {
            String newChannelName = editChannelsTextField.getText();
            if (!selectedChannel.getName().equals(newChannelName)) {
                String[] members = new String[0];
                restClient.updateChannel(server.getId(), selectedCategory.getId(), selectedChannel.getId(), builder.getPersonalUser().getUserKey(), newChannelName, false /*TODO*/, members /*TODO*/, response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        System.out.println("--> SUCCESS: changed channel name");
                        selectedChannel.setName(newChannelName);
                        editChannelsTextField.setText("");
                        Platform.runLater(() -> loadChannels());
                    } else {
                        System.out.println(status);
                        System.out.println(body.getObject().getString("message"));
                    }
                });
            } else {
                System.out.println("--> ERR: New name equals old name");
            }
        } else {
            System.out.println("--> ERR: No Channel selected OR Field is empty");
        }
    }
}
