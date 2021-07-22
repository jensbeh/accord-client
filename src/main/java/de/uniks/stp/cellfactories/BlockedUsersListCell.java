package de.uniks.stp.cellfactories;

import de.uniks.stp.controller.settings.BlockController;
import de.uniks.stp.model.User;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class BlockedUsersListCell implements javafx.util.Callback<ListView<User>, ListCell<User>> {
    BlockController blockController;

    public BlockedUsersListCell(BlockController blockController) {
        this.blockController = blockController;
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
            VBox cell = new VBox();
            cell.setPrefWidth(462);
            cell.setMaxWidth(462);
            cell.setPrefHeight(40);
            cell.setMaxHeight(40);
            Button button = new Button();
            button.setPrefWidth(462);
            button.setMaxWidth(462);
            button.setPrefHeight(40);
            button.setMaxHeight(40);
            button.getStyleClass().clear();
            button.getStyleClass().add("blockedUserElement");
            blockController.addButtonToContainer(button);

            super.updateItem(item, empty);
            if (!empty) {
                cell.setId("cell_blocked_" + item.getId());
                button.setId("user_blocked_" + item.getId());
                button.setText(item.getName());
                cell.getChildren().addAll(button);
            }
            this.setGraphic(cell);
        }
    }
}
