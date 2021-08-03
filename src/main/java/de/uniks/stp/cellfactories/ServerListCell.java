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
    private Server currentServer;

    public void setCurrentServer(Server newCurrentServer) {
        this.currentServer = newCurrentServer;
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
        return new ServerCell();
    }

    private class ServerCell extends ListCell<Server> {
        protected void updateItem(Server item, boolean empty) {
            // creates a HBox for each cell of the listView
            StackPane cell = new StackPane();
            super.updateItem(item, empty);
            if (!empty) {
                Circle circle = circle(item);
                Circle topCircle = new Circle(34, Color.TRANSPARENT);
                Label serverName = serverName(item);

                addMouseEvents(item, topCircle, circle);

                cell.setId("server");
                cell.setAlignment(Pos.CENTER);
                topCircle.setId("serverName_" + item.getId());
                
                cell.getChildren().addAll(circle, serverName, topCircle);
            }
            this.setGraphic(cell);
        }

        private Label serverName(Server item) {
            Label serverName = new Label();
            serverName.setText(item.getName());
            serverName.setTextFill(Color.WHITE);
            serverName.setFont(Font.font("System", FontWeight.BOLD, 12));
            serverName.setAlignment(Pos.CENTER);
            serverName.setPrefHeight(35.0);
            serverName.setPrefWidth(61.0);
            serverName.setTextAlignment(TextAlignment.CENTER);
            serverName.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            serverName.setWrapText(true);
            return serverName;
        }

        private Circle circle(Server item) {
            Circle circle = new Circle(34);
            circle.setId("serverName_" + item.getId());
            if (item == currentServer) {
                circle.setFill(Paint.valueOf("#5a5c5e"));
            } else {
                circle.setFill(Paint.valueOf("#a4a4a4"));
            }
            return circle;
        }

        private void addMouseEvents(Server item, Circle topCircle, Circle circle) {
            topCircle.setOnMouseEntered(event -> {
                if (item != currentServer) {
                    circle.setFill(Paint.valueOf("#bababa"));
                }
            });
            topCircle.setOnMouseExited(event -> {
                if (item != currentServer) {
                    circle.setFill(Paint.valueOf("#a4a4a4"));
                }
            });
        }
    }
}

