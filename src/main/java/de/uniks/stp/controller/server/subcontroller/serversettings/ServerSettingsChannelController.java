package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.SubSetting;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import kong.unirest.JsonNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ServerSettingsChannelController extends SubSetting {
    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final RestClient restClient;

    private Label categoryLabel;
    private ComboBox<Categories> categorySelector;
    private Label editChannelsLabel;
    private ComboBox<ServerChannel> editChannelsSelector;
    private TextField editChannelsTextField;
    private Button channelChangeButton;
    private Button channelDeleteButton;
    private Label createChannelLabel;
    private TextField createChannelTextField;
    private RadioButton channelTextRadioButton;
    private RadioButton channelVoiceRadioButton;
    private Button channelCreateButton;
    private VBox root;
    private Label radioVoice;
    private Label radioText;

    private Categories selectedCategory;
    private ServerChannel selectedChannel;
    private String channelType;

    private final PropertyChangeListener channelNamePCL = this::onChannelNameChanged;
    private final PropertyChangeListener channelListPCL = this::onChannelListChanged;


    public ServerSettingsChannelController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
        this.restClient = builder.getRestClient();
    }

    @SuppressWarnings("unchecked")
    public void init() {
        // init view
        root = (VBox) view.lookup("#rootChannel");
        this.categoryLabel = (Label) view.lookup("#categoryLabel");
        categorySelector = (ComboBox<Categories>) view.lookup("#categorySelector");
        this.editChannelsLabel = (Label) view.lookup("#editChannelsLabel");
        editChannelsSelector = (ComboBox<ServerChannel>) view.lookup("#editChannelsSelector");
        this.editChannelsTextField = (TextField) view.lookup("#editChannelsTextField");
        this.channelChangeButton = (Button) view.lookup("#channelChangeButton");
        this.channelDeleteButton = (Button) view.lookup("#channelDeleteButton");
        this.createChannelLabel = (Label) view.lookup("#createChannelLabel");
        this.createChannelTextField = (TextField) view.lookup("#createChannelTextField");
        this.channelTextRadioButton = (RadioButton) view.lookup("#channelTextRadioButton");
        this.channelVoiceRadioButton = (RadioButton) view.lookup("#channelVoiceRadioButton");
        this.channelCreateButton = (Button) view.lookup("#channelCreateButton");
        this.radioText = (Label) view.lookup("#radioText");
        this.radioVoice = (Label) view.lookup("#radioVoice");


        ToggleGroup textVoiceToggle = new ToggleGroup();
        channelTextRadioButton.setToggleGroup(textVoiceToggle);
        channelVoiceRadioButton.setToggleGroup(textVoiceToggle);
        channelTextRadioButton.setSelected(true);
        channelType = "text";

        categorySelector.setOnAction(this::onCategoryChanged);
        editChannelsSelector.setOnAction(this::onChannelChanged);
        this.channelChangeButton.setOnAction(this::onChannelChangeButtonClicked);
        this.channelTextRadioButton.setOnAction(this::onChannelTextButtonClicked);
        this.channelVoiceRadioButton.setOnAction(this::onChannelVoiceButtonClicked);
        this.channelCreateButton.setOnAction(this::onChannelCreateButtonClicked);
        this.channelDeleteButton.setOnAction(this::onChannelDeleteButtonClicked);

        categorySelector.getItems().addAll(builder.getCurrentServer().getCategories());

        categorySelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Categories object) {
                if (object == null) {
                    ResourceBundle lang = StageManager.getLangBundle();
                    return lang.getString("comboBox.selectCategory");
                }
                return object.getName();
            }

            @Override
            public Categories fromString(String string) {
                return null;
            }
        });

        editChannelsSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ServerChannel object) {
                if (object == null) {
                    ResourceBundle lang = StageManager.getLangBundle();
                    return lang.getString("comboBox.selectChannel");
                }
                return object.getName();
            }

            @Override
            public ServerChannel fromString(String string) {
                return null;
            }
        });

        disableEditing(true);
    }

    /**
     * changes the comboBoxItems when a channel was renamed
     */
    private void onChannelNameChanged(PropertyChangeEvent propertyChangeEvent) {
        loadChannels(selectedChannel);
        System.out.println("ffff");
    }

    /**
     * changes the comboBoxItems when a channel was created or deleted and added new PCL
     */
    private void onChannelListChanged(PropertyChangeEvent propertyChangeEvent) {
        loadChannels(null);

        for (ServerChannel serverChannel : selectedCategory.getChannel()) {
            serverChannel.removePropertyChangeListener(this.channelNamePCL);
        }

        for (ServerChannel channel : selectedCategory.getChannel()) {
            channel.addPropertyChangeListener(ServerChannel.PROPERTY_NAME, this.channelNamePCL);
        }
    }

    public void stop() {
        categorySelector.setOnAction(null);
        editChannelsSelector.setOnAction(null);
        channelChangeButton.setOnAction(null);
        channelDeleteButton.setOnAction(null);


        if (selectedCategory != null) {
            selectedCategory.removePropertyChangeListener(this.channelListPCL);

            for (ServerChannel serverChannel : selectedCategory.getChannel()) {
                serverChannel.removePropertyChangeListener(this.channelNamePCL);
            }
        }
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
        radioVoice.setDisable(disable);
        radioText.setDisable(disable);
    }

    /**
     * when category changes, enable items in view and load the Channels from the Category and added PCLs
     *
     * @param actionEvent the mouse click event
     */
    private void onCategoryChanged(ActionEvent actionEvent) {
        disableEditing(false);
        Categories oldCategory = selectedCategory;
        selectedCategory = categorySelector.getValue();

        if (oldCategory != null) {
            oldCategory.removePropertyChangeListener(this.channelListPCL);
            for (ServerChannel serverChannel : oldCategory.getChannel()) {
                serverChannel.removePropertyChangeListener(this.channelNamePCL);
            }
        }

        loadChannels(null);

        selectedCategory.addPropertyChangeListener(this.channelListPCL);
        for (ServerChannel channel : selectedCategory.getChannel()) {
            channel.addPropertyChangeListener(ServerChannel.PROPERTY_NAME, this.channelNamePCL);
        }
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
    public void loadChannels(ServerChannel preSelectChannel) {
        if (categorySelector == null || editChannelsSelector == null) {
            return;
        }

        Platform.runLater(() -> {
            selectedChannel = null;
            editChannelsSelector.getItems().clear();
            editChannelsSelector.getItems().addAll(selectedCategory.getChannel());

            if (preSelectChannel != null) {
                editChannelsSelector.getSelectionModel().select(preSelectChannel);
            }
        });

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
                restClient.updateChannel(server.getId(), selectedCategory.getId(), selectedChannel.getId(), builder.getPersonalUser().getUserKey(), newChannelName, selectedChannel.isPrivilege(), userListToStringArray(selectedChannel.getPrivilegedUsers()), response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        Platform.runLater(() -> editChannelsTextField.setText(""));
                    }
                });
            }
        }
    }

    /**
     * when clicking on text radio button, set channelType text
     *
     * @param actionEvent the mouse click event
     */
    private void onChannelTextButtonClicked(ActionEvent actionEvent) {
        channelType = "text";
    }

    /**
     * when clicking on voice radio button, set channelType voice
     *
     * @param actionEvent the mouse click event
     */
    private void onChannelVoiceButtonClicked(ActionEvent actionEvent) {
        channelType = "audio";
    }

    /**
     * create a channel when create button clicked
     *
     * @param actionEvent the mouse click event
     */
    private void onChannelCreateButtonClicked(ActionEvent actionEvent) {
        if (!createChannelTextField.getText().isEmpty()) {
            String channelName = createChannelTextField.getText();
            String[] members = new String[0];
            System.out.println("Channeltype: " + channelType);
            restClient.createChannel(server.getId(), selectedCategory.getId(), builder.getPersonalUser().getUserKey(), channelName, channelType, false, members, response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {
                    Platform.runLater(() -> createChannelTextField.setText(""));
                }
            });
        }
    }

    /**
     * delete a channel when delete button clicked
     *
     * @param actionEvent the mouse click event
     */
    private void onChannelDeleteButtonClicked(ActionEvent actionEvent) {
        if (selectedChannel != null) {
            restClient.deleteChannel(server.getId(), selectedCategory.getId(), selectedChannel.getId(), builder.getPersonalUser().getUserKey(), response -> {
            });
        }
    }

    public String[] userListToStringArray(List<User> users) {
        String[] pUsers = new String[users.size()];
        int counter = 0;
        for (User u : users) {
            pUsers[counter] = u.getId();
            counter++;
        }
        return pUsers;
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ServerSettings.css")).toExternalForm());
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ServerSettings.css")).toExternalForm());
    }
}

