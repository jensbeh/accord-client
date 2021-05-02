package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class ServerViewController {

    private final Server server;
    private final Parent view;
    private HBox root;
    private ScrollPane scrollPaneUserBox;
    private ModelBuilder builder;

    private VBox channelBox;
    private VBox textChannelBox;
    private Label serverNameText;
    private TextField sendTextField;
    private Button sendMessageButton;

    public ServerViewController(Parent view, ModelBuilder modelBuilder, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
    }

    public void init() {
        root = (HBox) view.lookup("#root");
        channelBox = (VBox) view.lookup("#channelBox");
        serverNameText = (Label) view.lookup("#serverName");
        serverNameText.setText(server.getName());
        textChannelBox = (VBox) view.lookup("#textChannelBox");
        sendTextField = (TextField) view.lookup("#sendTextField");
        sendMessageButton = (Button) view.lookup("#sendMessageButton");
        sendMessageButton.setOnAction(this::onSendMessage);

    }

    public HBox showServer() {
        showOnlineUsers();
        showText();
        showChannels();
        return root;
    }

    public void showOnlineUsers() {
        System.out.println("Test");
    }

    public void showText() {

    }

    public void onSendMessage(ActionEvent event) {

    }

    public void showChannels() {

    }

}
