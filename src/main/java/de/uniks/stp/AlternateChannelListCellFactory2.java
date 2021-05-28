package de.uniks.stp;

import de.uniks.stp.controller.ServerViewController;
import de.uniks.stp.model.Channel;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class AlternateChannelListCellFactory2 implements javafx.util.Callback<ListView<Channel>, ListCell<Channel>> {
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
    public ListCell<Channel> call(ListView<Channel> param) {
        return new ChannelListCell();
    }

    private static class ChannelListCell extends ListCell<Channel> {

        protected void updateItem(Channel item, boolean empty) {
            // creates a Hbox for each cell of the listView
            VBox cell = new VBox();
            Label name = new Label();
            Label message = new Label();
            super.updateItem(item, empty);
            this.setStyle("-fx-background-color: #23272a;");
            if (!empty) {
                if (item == ServerViewController.getSelectedChat()) {
                    this.setStyle("-fx-background-color: #666666;");
                }
                // init complete cell
                cell.setId("cell_" + item.getId());
                cell.setSpacing(5);
                cell.setPrefWidth(USE_COMPUTED_SIZE);
                // set channelName
                name.setId(item.getId());
                name.setText(item.getName());
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 5 0 0 10;");
                name.setTextFill(Paint.valueOf("#FFFFFF"));

                // set lastMessage
                if (item.getMessage().size() > 0) {
                    message.setId("msg_" + item.getId());
                    message.setPrefWidth(USE_COMPUTED_SIZE);
                    message.setStyle("-fx-font-size: 11;  -fx-padding: 0 10 0 10;");
                    message.setText(item.getMessage().get(item.getMessage().size() - 1).getMessage());
                    message.setTextFill(Paint.valueOf("#D0D0D0"));
                }
        /*
                // set chatColor - if selected / else not selected
                if (ServerViewController.getSelectedChat() != null && PrivateViewController.getSelectedChat().getName().equals(item.getName())) {
                    cell.setStyle("-fx-background-color: #737373; -fx-border-size: 2px; -fx-border-color: #AAAAAA; -fx-pref-height: 65; -fx-max-width: 183");
                } else {
                    cell.setStyle("-fx-background-color: #2C2F33; -fx-border-size: 2px; -fx-border-color: #AAAAAA; -fx-pref-height: 65; -fx-max-width: 183");
                }

         */
                cell.getChildren().addAll(name, message);
            }
            this.setGraphic(cell);
        }
    }
}
