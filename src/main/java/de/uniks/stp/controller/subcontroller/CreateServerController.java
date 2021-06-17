package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import kong.unirest.JsonNode;
import org.json.JSONArray;

import java.util.ResourceBundle;

/**
 * The class CreateServerController is about showing the createServerView. After a server is
 * created it is closed in HoweViewController. It is called after the + Button is clicked.
 */
public class CreateServerController {

    private final RestClient restClient;
    private final ModelBuilder builder;
    private final Parent view;
    private static TextField serverName;
    private Button createServer;
    private Runnable create;
    private static Label errorLabel;
    private static String error;
    private TextField linkTextField;
    private Button joinServer;
    private Runnable join;
    private Boolean serverIdTrue = false;

    /**
     * "The class CreateServerController takes the parameters Parent view, ModelBuilder builder.
     */
    public CreateServerController(Parent view, ModelBuilder builder) {
        this.builder = builder;
        this.view = view;
        restClient = builder.getRestClient();
    }

    /**
     * Initialise all view parameters
     */
    public void init() {
        // Load all view references
        serverName = (TextField) view.lookup("#serverName");
        errorLabel = (Label) view.lookup("#errorLabel");
        createServer = (Button) view.lookup("#createServer");
        createServer.setOnAction(this::onCreateServerClicked);
        linkTextField = (TextField) view.lookup("#inviteLink");
        joinServer = (Button) view.lookup("#joinServer");
        joinServer.setOnAction(this::onServerJoinClicked);
    }

    /**
     * Set the Runnable parameter that is called after the Ok button is clicked
     *
     * @param change the userKey off the personalUser
     */
    public void showCreateServerView(Runnable change) {
        this.create = change;
    }

    /**
     * Create the server and change the currentView to the ServerView with the newly created server.
     *
     * @param event is called when the Ok button is clicked
     */
    public void onCreateServerClicked(ActionEvent event) {
        try {
            CurrentUser personalUser = builder.getPersonalUser();
            String name = serverName.getText();
            if (name != null && !name.isEmpty()) {
                //JsonNode response = restClient.postServer(personalUser.getUserKey(), name);
                restClient.postServer(personalUser.getUserKey(), name, response -> {
                    String status = response.getBody().getObject().getString("status");
                    if (status.equals("success")) {
                        String serverId = response.getBody().getObject().getJSONObject("data").getString("id");
                        String serverName = response.getBody().getObject().getJSONObject("data").getString("name");
                        builder.setCurrentServer(builder.buildServer(serverName, serverId));
                        create.run();
                    } else if (status.equals(("failure"))) {
                        setError("error.create_server_failure");
                    }
                });
            } else {
                setError("error.server_name_field_empty");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("java.net.NoRouteToHostException: No route to host: connect")) {
                setError("error.create_server_no_connection");
            }
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (serverName != null)
            serverName.setText(lang.getString("textfield.server_name"));

        if (error != null && !error.equals("")) {
            errorLabel.setText(lang.getString(error));
        }
    }

    public void joinNewServer(Runnable change) {
        this.join = change;
    }

    private void onServerJoinClicked(ActionEvent actionEvent) {
        Platform.runLater(() -> errorLabel.setText(""));
        serverIdTrue = false;
        String error = "";
        CurrentUser currentUser = builder.getPersonalUser();
        String link = linkTextField.getText();
        if (!linkTextField.getText().equals("")) {
            Character lastChar = link.charAt(link.length() - 1);
            if (link.indexOf("/") != link.lastIndexOf("/") && !lastChar.equals('/')) {
                String[] splitLink = link.split("/");
                String serverId = splitLink[splitLink.length - 3];
                String inviteId = splitLink[splitLink.length - 1];
                restClient.joinServer(serverId, inviteId, currentUser.getName(), currentUser.getPassword(), currentUser.getUserKey(), response -> {
                    serverIdTrue = true;
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    String message = body.getObject().getString("message");
                    Platform.runLater(() -> errorLabel.setText(message));
                    if (status.equals("success")) {
                        findNewServer(serverId);
                    }
                });
                for (Server server : builder.getServers()) {
                    if (server.getId().equals(serverId)) {
                        serverIdTrue = true;
                        break;
                    }
                }
                if (!serverIdTrue) {
                    error = "error.wrong_server_id";
                }
            } else {
                error = "error.invalid_invite_link";
            }
        } else {
            error = "error.insert_invite_link_first";
        }
        if (!error.equals("")) {
            String finalError = error;
            Platform.runLater(() -> setError(finalError));
        }
    }

    private void findNewServer(String Id) {
        restClient.getServers(builder.getPersonalUser().getUserKey(), response -> {
            JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
            //List to track the online users in order to remove old users that are now offline
            for (int i = 0; i < jsonResponse.length(); i++) {
                String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                Server server = builder.buildServer(serverName, serverId);
                if (serverId.equals(Id)) {
                    builder.setCurrentServer(server);
                    join.run();
                    break;
                }
            }
        });
    }

    /**
     * set the error text in label placeholder
     *
     * @param errorMsg the error text
     */
    private void setError(String errorMsg) {
        ResourceBundle lang = StageManager.getLangBundle();
        error = errorMsg;
        errorLabel.setText(lang.getString(error));
    }

    public void stop() {
        createServer.setOnAction(null);
        joinServer.setOnAction(null);
    }
}
