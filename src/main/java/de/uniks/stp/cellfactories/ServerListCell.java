package de.uniks.stp.cellfactories;

import de.uniks.stp.model.Server;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class ServerListCell implements javafx.util.Callback<ListView<Server>, ListCell<Server>> {
    private static Server currentServer;

    public static Server getCurrentServer() {
        return currentServer;
    }

    public static void setCurrentServer(Server currentServer) {
        de.uniks.stp.cellfactories.ServerListCell.currentServer = currentServer;
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
    public ListCell<Server> call(ListView<Server> param) {
        return new ServerListCell();
    }

    private static class ServerListCell extends ListCell<Server> {
        protected void updateItem(Server item, boolean empty) {
            // creates a HBox for each cell of the listView
            StackPane cell = new StackPane();
            Circle circle = new Circle(34);
            Label serverName = new Label();
            super.updateItem(item, empty);
            if (!empty) {
                cell.setId("server");
                cell.setAlignment(Pos.CENTER);

                circle.setId("serverName_" + item.getId());
                if (item == currentServer) {
                    circle.setFill(Paint.valueOf("#5a5c5e"));
                } else {
                    circle.setFill(Paint.valueOf("#a4a4a4"));
                }
                serverName.setText(item.getName());
                serverName.setTextFill(Color.WHITE);
                serverName.setFont(Font.font("System", FontWeight.BOLD, 12));
                serverName.setAlignment(Pos.CENTER);
                serverName.setPrefHeight(35.0);
                serverName.setPrefWidth(61.0);
                serverName.setTextAlignment(TextAlignment.CENTER);
                serverName.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
                serverName.setWrapText(true);
                cell.getChildren().addAll(circle, serverName);
            }
            this.setGraphic(cell);
        }
    }
}

