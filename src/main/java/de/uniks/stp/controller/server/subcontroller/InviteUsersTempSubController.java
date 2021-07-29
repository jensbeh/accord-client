package de.uniks.stp.controller.server.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.ResourceBundle;

public class InviteUsersTempSubController {
    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final RestClient restClient;
    private Button createLink;
    private Button copyLink;
    private Label inviteLinksLabel;
    private Button deleteLink;
    private Label linkLabel;
    private ComboBox<String> linkComboBox;
    private String selectedLink;
    private HashMap<String, String> links;
    private Label copied;


    public InviteUsersTempSubController(Parent view, ModelBuilder builder, Server server) {
        this.restClient = builder.getRestClient();
        this.view = view;
        this.builder = builder;
        this.server = server;
    }

    @SuppressWarnings("unchecked")
    public void init() {
        createLink = (Button) view.lookup("#createLink");
        copyLink = (Button) view.lookup("#button_copyLink");
        inviteLinksLabel = (Label) view.lookup("#inviteLinksLabel");
        deleteLink = (Button) view.lookup("#deleteLink");
        linkLabel = (Label) view.lookup("#linkLabel");
        linkComboBox = (ComboBox<String>) view.lookup("#LinkComboBox");
        copied = (Label) view.lookup("#copiedLabel");
        copied.setVisible(false);

        links = new HashMap<>();
        createLink.setOnAction(this::onCreateLinkClicked);
        copyLink.setOnAction(this::onCopyLinkClicked);
        deleteLink.setOnAction(this::onDeleteLinkClicked);
        linkComboBox.setOnAction(this::onLinkChanged);
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
                String id = inv.getString("id");
                if (type.equals("temporal")) {
                    linkComboBox.getItems().add(link);
                    links.put(link, id);
                }
            }
        });
    }

    /**
     * OnCreate clicked send restClient request to the server and handles the response accordingly.
     */
    private void onCreateLinkClicked(ActionEvent actionEvent) {
        System.out.println("onCreateLinkClicked");
        restClient.createTempLink("temporal", 0, server.getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                String link = body.getObject().getJSONObject("data").getString("link");
                String id = body.getObject().getJSONObject("data").getString("id");
                Platform.runLater(() -> linkLabel.setText(link));
                linkComboBox.getItems().add(link);
                links.put(link, id);
            } else if (status.equals("failure")) {
                System.out.println(body);
            }
        });
    }

    /**
     * when clicked the button the link text is copied to clipboard
     */
    private void onCopyLinkClicked(ActionEvent actionEvent) {
        if (!linkLabel.getText().equals("link...")) {
            copied.setVisible(true);
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), copied);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.play();
        }

        javafx.scene.input.Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(linkLabel.getText());
        clipboard.setContent(content);
    }

    /**
     * OnDelete clicked removes selected Link from the ComboBox
     */
    private void onDeleteLinkClicked(ActionEvent actionEvent) {
        if (selectedLink != null) {
            if (selectedLink.equals(linkLabel.getText())) {
                Platform.runLater(() -> linkLabel.setText("Links ..."));
            }
            String invId = links.get(selectedLink);
            restClient.deleteInvLink(server.getId(), invId, builder.getPersonalUser().getUserKey(), response -> {
            });
            linkComboBox.getItems().remove(selectedLink);
        }
    }


    /**
     * updates the selectedLink and the TextField
     */
    private void onLinkChanged(ActionEvent actionEvent) {
        selectedLink = this.linkComboBox.getSelectionModel().getSelectedItem();
        this.linkComboBox.setPromptText(selectedLink);
        Platform.runLater(() -> linkLabel.setText(selectedLink));
    }

    public void stop() {
        createLink.setOnAction(null);
        copyLink.setOnAction(null);
        deleteLink.setOnAction(null);
        linkComboBox.setOnAction(null);
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (createLink != null)
            createLink.setText(lang.getString("button.create"));

        if (inviteLinksLabel != null)
            inviteLinksLabel.setText(lang.getString("label.inviteLinks"));

        if (deleteLink != null)
            deleteLink.setText(lang.getString("button.delete"));

        if (copied != null)
            copied.setText(lang.getString("label.copied"));
    }
}
