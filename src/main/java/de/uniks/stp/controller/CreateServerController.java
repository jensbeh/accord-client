package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.*;
import org.json.JSONArray;

import java.io.IOException;

public class CreateServerController {


        private final ModelBuilder builder;
        private Parent view;
        private VBox createServerBox;
        private TextField serverName;
        private Button createServer;
        private User personalUser;
        private Runnable change;


        public CreateServerController(Parent view, ModelBuilder builder) {
                this.builder = builder;
                this.view = view;
        }

        public void init() {
                // Load all view references
                createServerBox = (VBox) view.lookup("#createServerBox");
                serverName = (TextField) view.lookup("#serverName");
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
                        RestClient.postServer(personalUser.getUserKey(), name, response -> {
                                JsonNode body = response.getBody();
                                String status = body.getObject().getString("status");
                                if (status.equals("success")) {
                                        System.out.println("success");
                                        String serverId = body.getObject().getJSONObject("data").getString("id");
                                        String serverName = body.getObject().getJSONObject("data").getString("TestServer");
                                        Server newServer = new Server();
                                        newServer.setId(serverId);
                                        newServer.setName(serverName);
                                        newServer.setOwner(personalUser.getId());
                                        personalUser.withServer(newServer);
                                        builder.withServer(newServer);
                                } else if (status.equals(("failure"))) {
                                        System.out.println(body.getObject().getString("message"));
                                }
                        });
                }
                change.run();
        }
}
