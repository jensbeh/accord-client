package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.cellfactories.BlockedUsersListCell;
import de.uniks.stp.cellfactories.ServerChannelListCell;
import de.uniks.stp.model.User;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

public class BlockController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private Label blockedUsersLabel;
    private ScrollPane blockedUsersSP;
    private ListView<User> blockedUsersLV;
    private Button unblockButton;

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

        this.blockedUsersLV.setCellFactory(new BlockedUsersListCell());
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        /*this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));
        this.blockedUsersLV.getItems().add(new User().setName("TestUser").setId("abcdefgID"));*/
    }

    public void stop() {

    }

    public void setTheme() {

    }

    private void setWhiteMode() {

    }

    private void setDarkMode() {

    }
}
