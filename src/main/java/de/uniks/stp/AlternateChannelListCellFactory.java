package de.uniks.stp;

import de.uniks.stp.model.Channel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class AlternateChannelListCellFactory implements javafx.util.Callback<javafx.scene.control.ListView<de.uniks.stp.model.Channel>, javafx.scene.control.ListCell<de.uniks.stp.model.Channel>> {
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
            HBox cell = new HBox();
            Circle circle = new Circle(15);
            Label name = new Label();
            Label message = new Label();
            super.updateItem(item, empty);
            this.setStyle("-fx-background-color: #2C2F33;");
            if (!empty) {
                cell.setAlignment(Pos.CENTER_LEFT);
                circle.setFill(Paint.valueOf("#7289da"));
                name.setText("   "+item.getName()+": ");
                name.setTextFill(Paint.valueOf("#FFFFFF"));
                if (item.getMessage().size() > 0){
                    message.setText(item.getMessage().get(item.getMessage().size()-1).getMessage());
                    message.setTextFill(Paint.valueOf("#FFFFFF"));
                }
                cell.setStyle("-fx-background-color: #2C2F33;");
                cell.getChildren().addAll(circle,name,message);
            }
            this.setGraphic(cell);
        }
    }
}
