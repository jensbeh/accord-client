package de.uniks.stp;

import de.uniks.stp.controller.CurrentUserMessageController;
import de.uniks.stp.model.Message;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

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

    private static class MessageListCell extends ListCell<Message> {

        protected void updateItem(Message item, boolean empty) {
            // loads the message FXML and places it in the cell
            try {
                StackPane cell = new StackPane();
                super.updateItem(item, empty);
                this.setStyle("-fx-background-color: grey;");
                if (!empty) {
                    Parent view = FXMLLoader.load(StageManager.class.getResource("CurrentUserTextMessage.fxml"));
                    CurrentUserMessageController messageController = new CurrentUserMessageController(item, view);
                    messageController.init();
                    cell.setStyle("-fx-background-color: grey;");
                    cell.getChildren().addAll(view);
                }
                this.setGraphic(cell);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
