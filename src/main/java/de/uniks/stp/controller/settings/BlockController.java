package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.cellfactories.BlockedUsersListCell;
import de.uniks.stp.model.User;
import de.uniks.stp.util.ResourceManager;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

import java.util.ArrayList;
import java.util.List;

public class BlockController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private ListView<User> blockedUsersLV;
    private Button unblockButton;
    private List<Button> buttonContainer;
    private Button selectedButton;

    public BlockController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void setup() {

    }

    public void init() {
        this.unblockButton = (Button) view.lookup("#button_unblock");

        ScrollPane blockedUsersSP = (ScrollPane) view.lookup("#blockedUsersSP");
        this.blockedUsersLV = (ListView<User>) blockedUsersSP.getContent().lookup("#blockedUsersLV");

        this.unblockButton.setDisable(true);
        this.unblockButton.setOnAction(this::onUnblockButtonClicked);

        this.blockedUsersLV.setCellFactory(new BlockedUsersListCell(this));
        this.blockedUsersLV.getItems().addAll(builder.getBlockedUsers());

        buttonContainer = new ArrayList<>();
    }

    public void stop() {
        this.unblockButton.setOnAction(null);
        for (Button button : buttonContainer) {
            button.setOnAction(null);
        }
    }

    /**
     * pushes a blocked user as button into a List
     *
     * @param button the button to be added in the container
     */
    public void addButtonToContainer(Button button) {
        if (!buttonContainer.contains(button)) {
            buttonContainer.add(button);
            button.setOnAction(event -> onBlockedUserClicked(button));
        }
    }

    /**
     * selects a blocked user from the List
     *
     * @param button the blocked user who got clicked
     */
    public void onBlockedUserClicked(Button button) {
        this.unblockButton.setDisable(false);

        if (selectedButton != null) {
            selectedButton.setStyle("");
            selectedButton.getStyleClass().clear();
            selectedButton.getStyleClass().add("blockedUserElement");
        }

        selectedButton = button;

        selectedButton.getStyleClass().add("blockedUserElementSelected");
    }

    /**
     * removes the user from block list
     *
     * @param actionEvent the mouse click event
     */
    private void onUnblockButtonClicked(ActionEvent actionEvent) {
        for (User user : builder.getBlockedUsers()) {
            if (selectedButton.getId().equals("user_blocked_" + user.getId())) {
                builder.removeBlockedUser(user);
                this.blockedUsersLV.getItems().remove(user);
                ResourceManager.saveBlockedUsers(builder.getPersonalUser().getName(), user, false);
                break;
            }
        }
    }
}
