package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.SubSetting;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.util.Alerts;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import kong.unirest.JsonNode;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerSettingsCategoryController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final RestClient restClient;
    private final Server currentServer;
    private ComboBox<Categories> categoriesSelector;
    private TextField changeCategoryNameTextField;
    private Button changeCategoryNameButton;
    private Button deleteCategoryButton;
    private TextField createCategoryNameTextField;
    private Button createCategoryButton;
    private VBox root;
    private Stage stage;
    private Button okButton;
    private Categories selectedCategory;

    public ServerSettingsCategoryController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.currentServer = server;
        this.restClient = builder.getRestClient();
    }

    @SuppressWarnings("unchecked")
    public void init() {
        root = (VBox) view.lookup("#rootCategory");
        categoriesSelector = (ComboBox<Categories>) view.lookup("#editCategoriesSelector");
        changeCategoryNameTextField = (TextField) view.lookup("#editCategoryNameTextField");
        changeCategoryNameButton = (Button) view.lookup("#changeCategoryNameButton");
        deleteCategoryButton = (Button) view.lookup("#deleteCategoryButton");
        createCategoryNameTextField = (TextField) view.lookup("#createCategoryNameTextField");
        createCategoryButton = (Button) view.lookup("#createCategoryButton");

        changeCategoryNameButton.setOnAction(this::changeCategoryName);
        deleteCategoryButton.setOnAction(this::deleteCategory);
        createCategoryButton.setOnAction(this::createCategory);

        deleteCategoryButton.setDisable(true);

        ResourceBundle lang = StageManager.getLangBundle();
        this.categoriesSelector.setPromptText(lang.getString("comboBox.selectCategory"));
        this.categoriesSelector.getItems().clear();
        this.categoriesSelector.setOnAction(this::onCategoryClicked);

        for (Categories category : currentServer.getCategories()) {
            this.categoriesSelector.getItems().add(category);
            this.categoriesSelector.setConverter(new StringConverter<>() {
                @Override
                public String toString(Categories categoryToString) {
                    return categoryToString.getName();
                }

                @Override
                public Categories fromString(String string) {
                    return null;
                }
            });
        }
    }

    /**
     * Sets the selected category when clicked on it in comboBox
     */
    private void onCategoryClicked(Event event) {
        deleteCategoryButton.setDisable(false);
        selectedCategory = this.categoriesSelector.getValue();
        System.out.println("Selected Category: " + selectedCategory);
    }

    /**
     * changes the name of an existing category when button change is clicked
     */
    private void changeCategoryName(ActionEvent actionEvent) {
        Matcher whiteSpaceMatcher = Pattern.compile("^( )*$").matcher(changeCategoryNameTextField.getText());
        if (!whiteSpaceMatcher.find() && selectedCategory != null && !changeCategoryNameTextField.getText().isEmpty()) {
            String newCategoryName = changeCategoryNameTextField.getText();
            if (!selectedCategory.getName().equals(newCategoryName)) {

                restClient.updateCategory(currentServer.getId(), selectedCategory.getId(), newCategoryName, builder.getPersonalUser().getUserKey(), response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        for (Categories category : currentServer.getCategories()) {
                            if (category.getId().equals(selectedCategory.getId())) {
                                selectedCategory = category;
                            }
                        }
                        currentServer.withoutCategories(selectedCategory);
                        selectedCategory.setName(newCategoryName);
                        currentServer.withCategories(selectedCategory);

                        Platform.runLater(() -> {
                            categoriesSelector.getItems().clear();
                            categoriesSelector.getItems().addAll(currentServer.getCategories());
                        });

                        Platform.runLater(() -> changeCategoryNameTextField.setText(""));
                    }
                });
            }
        } else {
            if (selectedCategory != null) {
                Alerts.invalidNameAlert(builder);
            }
        }
    }

    /**
     * deletes an existing and chosen category when button delete is clicked
     */
    private void deleteCategory(ActionEvent actionEvent) {
        if (selectedCategory != null) {
            if (builder.getCurrentServer().getCategories().get(0) == selectedCategory) {
                try {
                    ResourceBundle lang = StageManager.getLangBundle();

                    Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("alert/DeleteDefault.fxml")), StageManager.getLangBundle());
                    stage = new Stage();
                    stage.initStyle(StageStyle.TRANSPARENT);
                    Scene scene = new Scene(root);
                    stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

                    // DropShadow of Scene
                    scene.setFill(Color.TRANSPARENT);
                    scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/DropShadow/DropShadow.css")).toExternalForm());

                    // create titleBar
                    HBox titleBarBox = (HBox) root.lookup("#titleBarBox");
                    Parent titleBarView = null;
                    try {
                        titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBarView.fxml")), StageManager.getLangBundle());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    titleBarBox.getChildren().add(titleBarView);
                    TitleBarController titleBarController = new TitleBarController(stage, titleBarView, builder);
                    titleBarController.init();
                    titleBarController.setTheme();
                    titleBarController.setMaximizable(false);
                    titleBarController.setTitle(lang.getString("label.error"));

                    stage.setScene(scene);
                    stage.show();

                    okButton = (Button) root.lookup("#okButton");
                    okButton.setText("OK");
                    okButton.setOnAction(this::closeStage);
                    if (builder.getTheme().equals("Bright")) {
                        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Alert.css")).toExternalForm());
                    } else {
                        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Alert.css")).toExternalForm());
                    }
                    Label errorLabel = (Label) root.lookup("#errorLabel");
                    errorLabel.setText(lang.getString("label.alertDefaultCat"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // disconnect from audioChannel
                if (builder.getAudioStreamClient() != null && selectedCategory.getChannel().contains(builder.getCurrentAudioChannel())) {
                    builder.getServerSystemWebSocket().getServerViewController().onAudioDisconnectClicked(new ActionEvent());
                }
                restClient.deleteCategory(currentServer.getId(), selectedCategory.getId(), builder.getPersonalUser().getUserKey(), response -> {
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        Platform.runLater(() -> categoriesSelector.getItems().remove(selectedCategory));
                        Platform.runLater(() -> categoriesSelector.getSelectionModel().clearSelection());
                        Platform.runLater(() -> deleteCategoryButton.setDisable(true));
                    }
                });
            }
        }
    }

    private void closeStage(ActionEvent actionEvent) {
        stage.close();
    }

    /**
     * creates a new category when button create is clicked
     */
    private void createCategory(ActionEvent actionEvent) {
        Matcher whiteSpaceMatcher = Pattern.compile("^( )*$").matcher(createCategoryNameTextField.getText());
        if (!whiteSpaceMatcher.find() && !createCategoryNameTextField.getText().isEmpty()) {
            String categoryName = createCategoryNameTextField.getText();

            restClient.createCategory(currentServer.getId(), categoryName, builder.getPersonalUser().getUserKey(), response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {
                    JSONObject data = body.getObject().getJSONObject("data");
                    String categoryId = data.getString("id");
                    String name = data.getString("name");

                    Categories newCategory = new Categories().setId(categoryId).setName(name);
                    Platform.runLater(() -> categoriesSelector.getItems().add(newCategory));
                    createCategoryNameTextField.setText("");
                }
            });
        } else {
            Alerts.invalidNameAlert(builder);
        }
    }

    public void stop() {
        this.changeCategoryNameButton.setOnMouseClicked(null);
        this.deleteCategoryButton.setOnMouseClicked(null);
        this.createCategoryButton.setOnMouseClicked(null);
        this.categoriesSelector.setOnAction(null);
        if (okButton != null) {
            this.okButton.setOnAction(null);
        }
        if (stage != null) {
            this.stage.close();
            stage = null;
        }
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ServerSettings.css")).toExternalForm());
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ServerSettings.css")).toExternalForm());
    }
}