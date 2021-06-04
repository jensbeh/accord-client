package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import kong.unirest.JsonNode;

public class InviteUsersTempSubController {
    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final RestClient restClient;
    private Button createLink;
    private Button deleteLink;
    private TextField linkTextField;
    private ComboBox<String> linkComboBox;
    private String selectedLink;

    public InviteUsersTempSubController(Parent view, ModelBuilder builder, Server server) {
        this.restClient = new RestClient();
        this.view = view;
        this.builder = builder;
        this.server = server;
    }

    public void init() {
        createLink = (Button) view.lookup("#createLink");
        deleteLink = (Button) view.lookup("#deleteLink");
        linkTextField = (TextField) view.lookup("#linkTextField");
        linkComboBox = (ComboBox<String>) view.lookup("#LinkComboBox");

        createLink.setOnAction(this::onCreateLinkClicked);
        deleteLink.setOnAction(this::onDeleteLinkClicked);
        linkComboBox.setOnAction(this::onLinkChanged);
    }


    /**
     * OnCreate clicked send restclient request to the server and handles the response accordingly.
     */
    private void onCreateLinkClicked(ActionEvent actionEvent) {
        System.out.println("onCreateLinkClicked");
        restClient.createTempLink("temporal", 0, server.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                String link = body.getObject().getJSONObject("data").getString("link");
                linkTextField.setText(link);
                linkComboBox.getItems().add(link);
            } else if (status.equals("failure")) {
                System.out.println(body);
            }
        });
    }

    /**
     * OnDelete clicked removes selected Link from the Combobox
     */
    private void onDeleteLinkClicked(ActionEvent actionEvent) {
        if (selectedLink != null) {
            if(selectedLink.equals(linkTextField.getText())){
                linkTextField.setText("Links ...");
            }
            linkComboBox.getItems().remove(selectedLink);
        }
    }


    /**
     * updates the selectedLink and the textfield
     */
    private void onLinkChanged(ActionEvent actionEvent) {
        selectedLink = this.linkComboBox.getSelectionModel().getSelectedItem();
        this.linkComboBox.setPromptText(selectedLink);
        linkTextField.setText(selectedLink);
    }

    public void stop() {
        createLink.setOnAction(null);
        deleteLink.setOnAction(null);
        linkComboBox.setOnAction(null);
    }
}