package de.uniks.stp.cellfactories;

import de.uniks.stp.StageManager;
import de.uniks.stp.model.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class UserListCell implements javafx.util.Callback<ListView<User>, ListCell<User>> {
    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     * determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */
    private HBox root;

    public void setRoot(HBox root) {
        this.root = root;
    }

    public HBox getRoot() {
        return root;
    }

    @Override
    public ListCell<User> call(ListView<User> param) {
        UserCell userCell = new UserCell();
        userCell.setRoot(root);
        return userCell;
    }

    private static class UserCell extends ListCell<User> {
        private HBox root;
        private User user;

        public void setRoot(HBox root) {
            this.root = root;
        }



        protected void updateItem(User item, boolean empty) {
            // creates a HBox for each cell of the listView
            this.user = item;
            HBox cell = new HBox();
            cell.setPadding(new Insets(5, 0, 5, 5));
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
                cell.setOnMouseClicked(this::spotifyPopup);
            }
            this.setGraphic(cell);
        }

        private void spotifyPopup(MouseEvent mouseEvent) {
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/SpotifyView.fxml")));
                VBox spotifyRoot = (VBox) root.lookup("spotifyRoot");
                ImageView spotifyArtwork = (ImageView) root.lookup("#spotifyArtwork");
                Label bandAndSong = (Label) root.lookup("#bandAndSong");

                HBox hBox = (HBox) mouseEvent.getSource();
                hBox.setStyle("-fx-background-color: #1db954; -fx-background-radius: 0 5 5 0;");
                Bounds bounds = ((HBox) mouseEvent.getSource()).localToScreen(((HBox) mouseEvent.getSource()).getBoundsInLocal());
                double x = bounds.getMinX() - 200;
                double y = bounds.getMinY();

                final Stage dialog = new Stage();
                dialog.initOwner(hBox.getScene().getWindow());
                dialog.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        dialog.close();
                        hBox.setStyle("-fx-background-color: transparent;");
                    }
                });
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                dialog.initStyle(StageStyle.TRANSPARENT);
                dialog.setX(x);
                dialog.setY(y);
                dialog.setScene(scene);
                dialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
