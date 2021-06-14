package de.uniks.stp;

import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Message;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
                userName.setId("userNameLabel");
                userName.setTextFill(Color.WHITE);
                Label message = new Label();
                message.setId("messageLabel");
                //right alignment if User is currentUser else left
                Date date = new Date(item.getTimestamp());
                DateFormat formatterTime = new SimpleDateFormat("dd.MM - HH:mm");
                if (currentUser.getName().equals(item.getFrom())) {
                    vbox.setAlignment(Pos.CENTER_RIGHT);
                    message.setStyle("-fx-background-color: ff9999;" + "-fx-background-radius: 4;");
                    message.setTextFill(Color.WHITE);
                    userName.setText(formatterTime.format(date) + " " + item.getFrom());
                } else {
                    vbox.setAlignment(Pos.CENTER_LEFT);
                    message.setStyle("-fx-background-color: white;" + "-fx-background-radius: 4;");
                    message.setTextFill(Color.BLACK);
                    userName.setText(item.getFrom() + " " + formatterTime.format(date));
                }
                //new Line after 50 Characters
                String str = item.getMessage();
                int point = 0;
                int counter = 25;
                boolean found = false;
                int endPoint = 0;
                int length = str.length();
                while ((point + 50) < length) {
                    endPoint = point + 50;
                    while (counter != 0 && !found) {
                        counter--;
                        if (str.charAt(endPoint - (25 - counter)) == ' ') {
                            str = new StringBuilder(str).insert(endPoint - (25 - counter), "\n").toString();
                            length += 2;
                            found = true;
                            point = endPoint - (25 - counter) + 2;
                        }
                    }
                    if (counter == 0) {
                        str = new StringBuilder(str).insert(endPoint, "\n").toString();
                        length += 2;
                        point = endPoint + 2;
                    }
                    found = false;
                    counter = 25;
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
