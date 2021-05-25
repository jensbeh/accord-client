package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ServerSettingsController {
    private Parent view;
    private ModelBuilder builder;
    private Server server;
    private Button selectedButton;
    private Button overview;
    private Button channel;
    private Button category;
    private Button privilege;
    private ServerPrivilegeSettingsController serverprivilegesettingscontroller;
    private VBox serversettingsview;

    public ServerSettingsController(Parent view, ModelBuilder modelBuilder, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
    }

    public void init() {
        //init of sideButtons
        overview = (Button) view.lookup("#overview");
        channel = (Button) view.lookup("#channel");
        category = (Button) view.lookup("#category");
        privilege = (Button) view.lookup("#privilege");
        serversettingsview = (VBox) view.lookup("#serverSettingsContainer");

        //Highlight the OverviewButton
        newSelectedButton(overview);

        //ActionHandler
        overview.setOnAction(this::onOverViewClicked);
        channel.setOnAction(this::onChannelClicked);
        category.setOnAction(this::onCategoryClicked);
        privilege.setOnAction(this::onPrivilegeClicked);
    }


    private void onOverViewClicked(ActionEvent actionEvent) {
        if(selectedButton!=overview) {
            newSelectedButton(overview);
        }
    }

    private void onChannelClicked(ActionEvent actionEvent) {
        if(selectedButton!=channel) {
            newSelectedButton(channel);
        }
    }

    private void onCategoryClicked(ActionEvent actionEvent) {
        if(selectedButton!=category) {
            newSelectedButton(category);
        }
    }

    private void onPrivilegeClicked(ActionEvent actionEvent) {
        if(selectedButton!=privilege) {
            newSelectedButton(privilege);
            this.serversettingsview.getChildren().clear();
            try {
                //view
                Parent view = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettings_Privilege.fxml"));
                //Controller
                serverprivilegesettingscontroller = new ServerPrivilegeSettingsController(view, builder, server);
                serverprivilegesettingscontroller.init();
                this.serversettingsview.getChildren().add(view);
            } catch (Exception e) {
                System.err.println("Error on showing ServerSettings_Privilege");
                e.printStackTrace();
            }
        }
    }


    public void stop() {
        overview.setOnAction(null);
        channel.setOnAction(null);
        category.setOnAction(null);
        privilege.setOnAction(null);
    }

    private void newSelectedButton(Button button) {
        if(selectedButton!=null){
            selectedButton.setStyle("-fx-background-color: #333333;-fx-border-color:#333333");
        }
        button.setStyle("-fx-background-color: #5c5c5c;-fx-border-color:#1a1a1a");
        selectedButton = button;
    }
}
