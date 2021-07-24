package de.uniks.stp.cellfactories;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.User;
import de.uniks.stp.util.ResourceManager;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class UserListCell implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    private final ModelBuilder builder;

    public UserListCell(ModelBuilder builder) {
        this.builder = builder;
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
    public ListCell<User> call(ListView<User> param) {
        return new UserCell();
    }

    private class UserCell extends ListCell<User> {
        protected void updateItem(User item, boolean empty) {
            // creates a HBox for each cell of the listView
            HBox cell = new HBox();
            Circle circle = new Circle(15);
            Label name = new Label();
            super.updateItem(item, empty);
            if (!empty) {
                cell.setId("user");
                cell.setAlignment(Pos.CENTER_LEFT);
                if (item.isStatus()) {
                    circle.setFill(Paint.valueOf("#13d86b"));
                } else {
                    circle.setFill(Paint.valueOf("#eb4034"));
                }
                name.setId(item.getId());
                name.setText("   " + item.getName());
                name.setStyle("-fx-font-size: 18");
                name.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
                name.setPrefWidth(135);
                cell.getChildren().addAll(circle, name);

                addContextMenu(item, name);
            }
            this.setGraphic(cell);
        }
    }

    /**
     * adds a ContextMenu to the User Cell, where block/unblock can be clicked
     *
     * @param item the user
     * @param name the user name as Label
     */
    private void addContextMenu(User item, Label name) {
        ContextMenu menu = new ContextMenu();
        MenuItem block = new MenuItem("block");
        MenuItem unblock = new MenuItem("unblock");
        menu.setId("UserBlockControl");
        block.setId("blockUser");
        unblock.setId("unblockUser");
        menu.getItems().addAll(block, unblock);
        block.setVisible(false);
        unblock.setVisible(false);
        if (builder.getTheme().equals("Dark")) {
            menu.setStyle("-fx-background-color: #23272a");
            block.setStyle("-fx-text-fill: #FFFFFF");
            unblock.setStyle("-fx-text-fill: #FFFFFF");
        } else {
            menu.setStyle("-fx-background-color: White");
            block.setStyle("-fx-text-fill: #000000");
            unblock.setStyle("-fx-text-fill: #000000");
        }

        name.setContextMenu(menu);

        updateContextMenuItems(item, block, unblock);

        block.setOnAction(event -> blockUser(item, block, unblock));
        unblock.setOnAction(event -> unblockUser(item, block, unblock));

        // keep refreshing in case user has been unblocked from settings
        name.setOnContextMenuRequested(event -> updateContextMenuItems(item, block, unblock));
    }

    private void updateContextMenuItems(User item, MenuItem block, MenuItem unblock) {
        boolean isBlocked = false;
        for (User user : builder.getBlockedUsers()) {
            if (user.getId().equals(item.getId())) {
                isBlocked = true;
                break;
            }
        }

        if (isBlocked) {
            block.setVisible(false);
            unblock.setVisible(true);
        } else {
            block.setVisible(true);
            unblock.setVisible(false);
        }
    }

    private void blockUser(User item, MenuItem block, MenuItem unblock) {
        builder.addBlockedUser(item);
        ResourceManager.saveBlockedUsers(builder.getPersonalUser().getName(), item, true);
        block.setVisible(false);
        unblock.setVisible(true);
    }

    private void unblockUser(User item, MenuItem block, MenuItem unblock) {
        builder.removeBlockedUser(item);
        ResourceManager.saveBlockedUsers(builder.getPersonalUser().getName(), item, false);
        block.setVisible(true);
        unblock.setVisible(false);
    }
}
