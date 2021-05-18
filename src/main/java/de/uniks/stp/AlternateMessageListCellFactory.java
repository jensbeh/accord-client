package de.uniks.stp;

import de.uniks.stp.controller.CurrentUserMessageController;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;

public class AlternateMessageListCellFactory implements javafx.util.Callback<ListView<Message>, ListCell<Message>> {

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
    public ListCell<Message> call(ListView<Message> param) {
        return new AlternateMessageListCellFactory.MessageListCell();
    }

    private static CurrentUser currentUser;

    public static CurrentUser getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(CurrentUser currentUser) {
        AlternateMessageListCellFactory.currentUser = currentUser;
    }


    private static class MessageListCell extends ListCell<Message> {

        private int x;
        /**
         * shows message in cell of ListView
         */
        protected void updateItem(Message item, boolean empty) {
            StackPane cell = new StackPane();
            super.updateItem(item, empty);
            this.setStyle("-fx-background-color: grey;");
            if (!empty) {
                VBox vbox = new VBox();
                Label userName = new Label();
                userName.setTextFill(Color.WHITE);
                Label message = new Label();
                //right alignment if User is currentUser else left
                if (currentUser.getName().equals(item.getFrom())) {
                    vbox.setAlignment(Pos.CENTER_RIGHT);
                    message.setStyle("-fx-background-color: pink;" + "-fx-background-radius: 4;");
                    message.setTextFill(Color.WHITE);
                } else {
                    vbox.setAlignment(Pos.CENTER_LEFT);
                    message.setStyle("-fx-background-color: white;" + "-fx-background-radius: 4;");
                    message.setTextFill(Color.BLACK);
                }
                userName.setText(item.getFrom());
                //new Line after 50 Characters
                String str = item.getMessage();
                int i = str.length();
                i -= 50;
                int f = 50;
                while(i > 0){
                    x = f;
                    System.out.println("f: " + f);
                    if(f <= str.length()) {
                        if (str.charAt(f) == ' ') {
                            str = new StringBuilder(str).insert(f, "\n").toString();
                        } else {
                            while (str.charAt(x) != ' ') {
                                x -= 1;
                            }
                            str = new StringBuilder(str).insert(x, "\n").toString();
                        }
                    }
                    i -= 50;
                    f += 50;
                }
                message.setText(" " + str + " ");
                vbox.getChildren().addAll(userName, message);
                cell.setAlignment(Pos.CENTER_RIGHT);
                cell.getChildren().addAll(vbox);
            }
            this.setGraphic(cell);
        }
    }
}
