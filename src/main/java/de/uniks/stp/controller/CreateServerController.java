package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.*;
import org.json.JSONArray;

import java.io.IOException;

public class CreateServerController {

        private final RestClient restClient;
        private final ModelBuilder builder;
        private Parent view;
        private VBox createServerBox;
        private TextField serverName;
        private Button createServer;
        private User personalUser;
        private Runnable change;
        private Label errorLabel;

        public CreateServerController(Parent view, ModelBuilder builder) {
                this.builder = builder;
                this.view = view;
                restClient = new RestClient();
        }

        public void init() {
                // Load all view references
                createServerBox = (VBox) view.lookup("#createServerBox");
                serverName = (TextField) view.lookup("#serverName");
                errorLabel = (Label) view.lookup("#errorLabel");
                createServer = (Button) view.lookup(("#createServer"));
                createServer.setOnAction(this::onCreateServerClicked);

        }

        public void showCreateServerView(Runnable change) {
                this.change = change;
        }

        public void onCreateServerClicked(ActionEvent event){
                this.personalUser = builder.getPersonalUser();
                String name = this.serverName.getText();
                if(name != null && !name.isEmpty()) {
                        JsonNode response = restClient.postServer(personalUser.getUserKey(), name);
                        String status = response.getObject().getString("status");
                        if (status.equals("success")) {
                                System.out.println("success");
                                String serverId = response.getObject().getJSONObject("data").getString("id");
                                String serverName = response.getObject().getJSONObject("data").getString("name");
                                Server newServer = new Server();
                                newServer.setId(serverId);
                                newServer.setName(serverName);
                                newServer.setOwner(personalUser.getId());
                                personalUser.withServer(newServer);
                                builder.buildServer(newServer);
                                change.run();
                        } else if (status.equals(("failure"))) {
                                errorLabel.setText(response.getObject().getString("message"));
                        }
                }
                else {
                        errorLabel.setText("Error: Server name cannot be empty");
                }
        }
}
