package de.uniks.stp.controller.subcontroller;

import com.sun.javafx.fxml.expression.Expression;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class InviteUsersUserLimitSubController {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final RestClient restClient;
    private Button createLink;
    private Button deleteLink;
    private TextField linkTextField;
    private TextField userLimit;
    private ComboBox<List<String>> linkComboBox;
    private List<String> selectedList;
    private HashMap<String, String> links;

    public InviteUsersUserLimitSubController(Parent view, ModelBuilder builder, Server server) {
        this.restClient = new RestClient();
        this.view = view;
        this.builder = builder;
        this.server = server;
    }

    public void init() {
        createLink = (Button) view.lookup("#createLink");
        deleteLink = (Button) view.lookup("#deleteLink");
        linkTextField = (TextField) view.lookup("#linkTextField");
        userLimit = (TextField) view.lookup("#maxUsers");
        linkComboBox = (ComboBox<List<String>>) view.lookup("#LinkComboBox");

        links = new HashMap<String, String>();
        createLink.setOnAction(this::onCreateLinkClicked);
        deleteLink.setOnAction(this::onDeleteLinkClicked);
        linkComboBox.setOnAction(this::onLinkChanged);
        linkComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(List<String> list) {
                if (list == null) {
                    return "Select Link...";
                }
                return list.get(0) + " | " + list.get(1);
            }

            @Override
            public List<String> fromString(String string) {
                return null;
            }
        });
        loadLinks();
    }

    /**
     * Load old links
     */
    private void loadLinks() {
        restClient.getInvLinks(server.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            JSONArray data = body.getObject().getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject inv = data.getJSONObject(i);
                String link = inv.getString("link");
                String type = inv.getString("type");
                String maxUsers = String.valueOf(inv.getInt("max"));
                String id = String.valueOf(inv.getInt("id"));
                if (type.equals("count")) {
                    linkComboBox.getItems().add(List.of(link, maxUsers));
                    links.put(link, id);
                }
            }
        });
    }

    /**
     * OnCreate clicked send restclient request to the server and handles the response accordingly.
     */
    private void onCreateLinkClicked(ActionEvent actionEvent) {
        if (!userLimit.getText().equals("")) {
            System.out.println("onCreateLinkClicked");
            int count = 0;
            try {
                count = Integer.parseInt(userLimit.getText());
            } catch (NumberFormatException e) {
                System.out.println("Not a number");
            }
            if (count > 0)
                restClient.createTempLink("count", count, server.getId(), builder.getPersonalUser().getUserKey(), response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        String link = body.getObject().getJSONObject("data").getString("link");
                        String maxUsers = String.valueOf(body.getObject().getJSONObject("data").getInt("max"));
                        String id = String.valueOf(body.getObject().getJSONObject("data").getInt("id"));
                        linkTextField.setText(link);
                        linkComboBox.getItems().add(List.of(link, maxUsers));
                        links.put(link, id);
                    } else if (status.equals("failure")) {
                        System.out.println(body);
                    }
                });
        }
    }

    /**
     * OnDelete clicked removes selected Link from the Combobox
     */
    private void onDeleteLinkClicked(ActionEvent actionEvent) {
        if (selectedList != null) {
            String link = selectedList.get(0);
            String link2 = linkTextField.getText();
            if (link.equals(link2)) {
                linkTextField.setText("Links ...");
            }
            String invId = links.get(selectedList.get(0));
            restClient.deleteInvLink(server.getId(), invId, builder.getPersonalUser().getUserKey(), response -> {
            });
            linkComboBox.getItems().remove(selectedList);
        }
    }


    /**
     * updates the selectedLink and the textfield
     */
    private void onLinkChanged(ActionEvent actionEvent) {
        selectedList = this.linkComboBox.getSelectionModel().getSelectedItem();
        String selectedLink, maxUsers;
        if (selectedList != null) {
            selectedLink = selectedList.get(0);
            maxUsers = selectedList.get(1);
        } else {
            selectedLink = "Select Link...";
            maxUsers = "";
        }
        this.linkComboBox.setPromptText(selectedLink);
        linkTextField.setText(selectedLink);
        userLimit.setText(maxUsers);
    }

    public void stop() {
        createLink.setOnAction(null);
        deleteLink.setOnAction(null);
        linkComboBox.setOnAction(null);
    }
}
