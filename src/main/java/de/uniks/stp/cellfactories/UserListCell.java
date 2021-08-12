package de.uniks.stp.cellfactories;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.User;
import de.uniks.stp.util.ResourceManager;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.ResourceBundle;

public class UserListCell implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    private final ModelBuilder builder;
    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     */
    private HBox root;

    public UserListCell(ModelBuilder builder) {
        this.builder = builder;
    }

    public HBox getRoot() {
        return root;
    }

    public void setRoot(HBox root) {
        this.root = root;
    }

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new UserCell();
    }

    private class UserCell extends ListCell<User> {
        private User user;

        protected void updateItem(User item, boolean empty) {
            // creates a HBox for each cell of the listView
            this.user = item;
            super.updateItem(item, empty);
            if (!empty) {
                HBox cell = cell();
                Region hoverBg = hoverBackground(cell);
                Circle circle = new Circle(15);
                Label name = name(item);

                StackPane stackPane = new StackPane();
                addContextMenu(item, name);
                if (item.isStatus()) {
                    circle.setFill(Paint.valueOf("#13d86b"));
                    if (item.getDescription() != null && (!item.getDescription().equals("") && !item.getDescription().equals("?") && Character.toString(item.getDescription().charAt(0)).equals("?"))) {
                        VBox vBox = new VBox();
                        Label game = game(item);

                        vBox.getChildren().addAll(name, game);
                        cell.getChildren().addAll(circle, vBox);
                        stackPane.getChildren().addAll(hoverBg, cell);
                    } else {
                        cell.getChildren().addAll(circle, name);
                        stackPane.getChildren().addAll(hoverBg, cell);
                    }
                } else {
                    circle.setFill(Paint.valueOf("#eb4034"));
                    cell.getChildren().addAll(circle, name);
                    stackPane.getChildren().addAll(hoverBg, cell);
                }
                this.setGraphic(stackPane);
            } else {
                this.setGraphic(null);
            }
        }

        private Label game(User item) {
            Label game = new Label();
            game.setText(item.getDescription());
            game.setText("   " + builder.getStageManager().getLangBundle().getString("label.steam_playing") + " " + item.getDescription().substring(1));
            game.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            game.setPrefWidth(135);
            return game;
        }

        private Label name(User item) {
            Label name = new Label();
            name.setId(item.getId());
            name.setText("   " + item.getName());
            name.setStyle("-fx-font-size: 18");
            name.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            name.setPrefWidth(135);
            return name;
        }

        private HBox cell() {
            HBox cell = new HBox();
            cell.setId("user");
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setStyle("-fx-padding: 5 5 5 5;");
            return cell;
        }

        private Region hoverBackground(HBox cell) {
            Region hoverBg = new Region();
            hoverBg.setPrefSize(175, 30);
            hoverBg.setOpacity(0.3);
            setOnMouseEffects(hoverBg, cell);
            return hoverBg;
        }

        /**
         * this method adds onMouse Effects for hovering or clicking with mouse
         */
        private void setOnMouseEffects(Region hoverBg, HBox cell) {
            this.setOnMouseEntered(event -> {
                if (builder.getTheme().equals("Dark")) {
                    hoverBg.setStyle("-fx-background-color: #5b5d61; -fx-background-radius: 10 10 10 10");
                } else {
                    hoverBg.setStyle("-fx-background-color: #bababa; -fx-background-radius: 10 10 10 10");
                }
            });
            this.setOnMouseExited(event -> {
                if (builder.getTheme().equals("Dark")) {
                    hoverBg.setStyle("-fx-background-color: transparent; -fx-background-radius: 10 10 10 10");
                } else {
                    if (!hoverBg.getStyle().equals("-fx-background-color: #ffffff; -fx-background-radius: 10 10 10 10")) {
                        hoverBg.setStyle("-fx-background-color: transparent; -fx-background-radius: 10 10 10 10");
                    }
                }
            });
            this.setOnMousePressed(event -> hoverBg.setStyle("-fx-background-color: transparent; -fx-background-radius: 10 10 10 10"));
            this.setOnMouseReleased(event -> {
                if (builder.isSpotifyShow()) {
                    builder.getSpotifyConnection().showSpotifyPopupView(cell, false, user.getDescription());
                }
                if (event.getX() < 0 || event.getX() > 175 || event.getY() < 0 || event.getY() > 30) {
                    hoverBg.setStyle("-fx-background-color: transparent; -fx-background-radius: 10 10 10 10");
                } else if (builder.getTheme().equals("Dark")) {
                    hoverBg.setStyle("-fx-background-color: #5b5d61; -fx-background-radius: 10 10 10 10");
                } else {
                    hoverBg.setStyle("-fx-background-color: #cccccc; -fx-background-radius: 10 10 10 10");
                }
            });
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

            name.setContextMenu(menu);

            updateContextMenuItems(item, block, unblock);

            block.setOnAction(event -> blockUser(item, block, unblock));
            unblock.setOnAction(event -> unblockUser(item, block, unblock));

            // keep refreshing in case user has been unblocked from settings
            name.setOnContextMenuRequested(event -> {
                updateContextMenuItems(item, block, unblock);
            });
        }

        private void updateContextMenuItems(User item, MenuItem block, MenuItem unblock) {
            ResourceBundle lang = builder.getStageManager().getLangBundle();
            block.setText(lang.getString("menuItem.block"));
            unblock.setText(lang.getString("menuItem.unblock"));

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
}
