package de.uniks.stp.controller.home.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The class CreateServerController is about showing the createServerView. After a server is
 * created it is closed in HoweViewController. It is called after the + Button is clicked.
 */
public class CreateJoinServerController {

    private final RestClient restClient;
    private final ModelBuilder builder;
    private final Parent view;
    private final Stage stage;
    private TextField serverName;
    private Button createServer;
    private Runnable create;
    private String error;
    private TextField linkTextField;
    private Button joinServer;
    private Runnable join;
    private Boolean serverIdTrue = false;
    private TabPane tapPane;
    private Tab create_tab;
    private Tab join_tab;
    private Label create_errorLabel;
    private Label join_errorLabel;
    private String last_error_type;
    private TitleBarController titleBarController;


    /**
     * "The class CreateServerController takes the parameters Parent view, ModelBuilder builder.
     */
    public CreateJoinServerController(Parent view, ModelBuilder builder, Stage stage) {
        this.stage = stage;
        this.builder = builder;
        this.view = view;
        restClient = builder.getRestClient();
    }

    /**
     * Initialise all view parameters
     */
    public void init() {
        // create titleBar
        HBox titleBarBox = (HBox) view.lookup("#titleBarBox");
        titleBarController = new TitleBarController(stage, titleBarBox, builder);
        titleBarController.init();
        titleBarController.setTheme();
        titleBarController.setMaximizable(false);

        tapPane = (TabPane) view.lookup("#tabView");
        create_tab = tapPane.getTabs().get(0);
        join_tab = tapPane.getTabs().get(1);
        create_tab.setOnSelectionChanged(this::changeEnter);
        tapPane.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                createServer.fire();
            }
        });
        try {
            load_tab_content();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onLanguageChanged();
    }

    private void changeEnter(Event event) {
        if (tapPane.getTabs().get(0).isSelected()) {
            tapPane.setOnKeyReleased(key -> {
                if (key.getCode() == KeyCode.ENTER) {
                    createServer.fire();
                }
            });
        } else {
            tapPane.setOnKeyReleased(key -> {
                if (key.getCode() == KeyCode.ENTER) {
                    joinServer.fire();
                }
            });
        }
    }

    private void load_tab_content() throws IOException {
        Parent createView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/homeview/CreateServer.fxml")), StageManager.getLangBundle());
        Parent joinView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/homeview/JoinServer.fxml")), StageManager.getLangBundle());
        create_tab.setContent(createView);
        join_tab.setContent(joinView);
        create_errorLabel = (Label) view.lookup("#create_errorLabel");
        serverName = (TextField) view.lookup("#serverName");
        createServer = (Button) view.lookup("#createServer");
        createServer.setOnAction(this::onCreateServerClicked);
        join_errorLabel = (Label) view.lookup("#join_errorLabel");
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
                        setError("error.create_server_failure", "create");
                    }
                });
            } else {
                setError("error.server_name_field_empty", "create");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("java.net.NoRouteToHostException: No route to host: connect")) {
                setError("error.create_server_no_connection", "create");
            }
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        titleBarController.setTitle(lang.getString("window_title_create"));
        createServer.setText(lang.getString("button.create_server"));
        joinServer.setText(lang.getString("button.join_server"));
        create_tab.setText(lang.getString("tabPane.create_server"));
        join_tab.setText(lang.getString("tabPane.join_server"));
        if (serverName != null)
            serverName.setPromptText(lang.getString("textField.server_name"));
        if (linkTextField != null) {
            linkTextField.setPromptText(lang.getString("textField.invite_link"));
        }

        if (error != null && !error.equals("")) {
            if (last_error_type.equals("join")) {
                join_errorLabel.setText(lang.getString(error));
            }
            if (last_error_type.equals("create")) {
                create_errorLabel.setText(lang.getString(error));
            }
        }
    }

    public void joinNewServer(Runnable change) {
        this.join = change;
    }

    private void onServerJoinClicked(ActionEvent actionEvent) {
        Platform.runLater(() -> join_errorLabel.setText(""));
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
                    Platform.runLater(() -> join_errorLabel.setText(message));
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
            Platform.runLater(() -> setError(finalError, "join"));
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
    private void setError(String errorMsg, String selector) {
        last_error_type = selector;
        ResourceBundle lang = StageManager.getLangBundle();
        error = errorMsg;
        if (last_error_type.equals("join")) {
            join_errorLabel.setText(lang.getString(error));
        }
        if (last_error_type.equals("create")) {
            create_errorLabel.setText(lang.getString(error));
        }
    }

    public void stop() {
        createServer.setOnAction(null);
        joinServer.setOnAction(null);
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        tapPane.getStylesheets().clear();
        tapPane.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/CreateJoinView.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }

    private void setDarkMode() {
        tapPane.getStylesheets().clear();
        tapPane.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/CreateJoinView.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }
}
