package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.cellfactories.BlockedUsersListCell;
import de.uniks.stp.model.User;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

import java.util.ArrayList;
import java.util.List;

public class BlockController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private Label blockedUsersLabel;
    private ScrollPane blockedUsersSP;
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
        this.blockedUsersLabel = (Label) view.lookup("#blockedUsersLabel");
        this.unblockButton = (Button) view.lookup("#button_unblock");

        this.blockedUsersSP = (ScrollPane) view.lookup("#blockedUsersSP");
        this.blockedUsersLV = (ListView<User>) blockedUsersSP.getContent().lookup("#blockedUsersLV");

        this.unblockButton.setDisable(true);

        this.blockedUsersLV.setCellFactory(new BlockedUsersListCell(this));
        this.blockedUsersLV.getItems().addAll(builder.getBlockedUsers());
        /*this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));*/

        buttonContainer = new ArrayList<>();
    }

    public void stop() {
        for (Button button : buttonContainer) {
            button.setOnAction(null);
        }
    }

    /**
     * pushes a blocked user as button into a List
     * @param button the button to be added in the container
     */
    public void addButtonToContainer(Button button) {
        if (!buttonContainer.contains(button)) {
            buttonContainer.add(button);
            button.setOnAction(event -> { onBlockedUserClicked(button); });
        }
    }

    /**
     * selects a blocked user from the List
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

        //selectedButton.setStyle("-fx-background-color: #AAAAAA;");
        selectedButton.getStyleClass().add("blockedUserElementSelected");
    }

    public void setTheme() {

    }

    private void setWhiteMode() {

    }

    private void setDarkMode() {

    }
}
