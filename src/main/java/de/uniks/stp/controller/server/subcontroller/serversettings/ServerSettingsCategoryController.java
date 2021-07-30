package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.SubSetting;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import kong.unirest.JsonNode;
import org.json.JSONObject;

import java.util.Objects;
import java.util.ResourceBundle;

public class ServerSettingsCategoryController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final RestClient restClient;
    private ComboBox<Categories> categoriesSelector;
    private TextField changeCategoryNameTextField;
    private Button changeCategoryNameButton;
    private Button deleteCategoryButton;
    private TextField createCategoryNameTextField;
    private Button createCategoryButton;
    private VBox root;

    private final Server currentServer;
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
        selectedCategory = this.categoriesSelector.getValue();
        System.out.println("Selected Category: " + selectedCategory);
    }

    /**
     * changes the name of an existing category when button change is clicked
     */
    private void changeCategoryName(ActionEvent actionEvent) {
        if (selectedCategory != null && !changeCategoryNameTextField.getText().isEmpty()) {
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
        }
    }

    /**
     * deletes an existing and chosen category when button delete is clicked
     */
    private void deleteCategory(ActionEvent actionEvent) {
        if (selectedCategory != null) {
            if (builder.getCurrentServer().getCategories().get(0) == selectedCategory) {
                ResourceBundle lang = StageManager.getLangBundle();
                Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
                alert.setTitle(lang.getString("label.error"));
                alert.setHeaderText(lang.getString("label.alertDefaultCat"));
                setAlertStyle(alert);
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
                    }
                });
            }
        }
    }

    public void setAlertStyle(Alert alert) {
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().get(0).setId("okButton");
        Stage stageIcon = (Stage) alert.getDialogPane().getScene().getWindow();
        stageIcon.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));
        if (builder.getTheme().equals("Bright")) {
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Alert.css")).toExternalForm());
        } else {
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Alert.css")).toExternalForm());
        }
        alert.getDialogPane().getStyleClass().add("AlertStyle");
        alert.showAndWait();
    }

    /**
     * creates a new category when button create is clicked
     */
    private void createCategory(ActionEvent actionEvent) {
        if (!createCategoryNameTextField.getText().isEmpty()) {
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
        }
    }

    public void stop() {
        this.changeCategoryNameButton.setOnMouseClicked(null);
        this.deleteCategoryButton.setOnMouseClicked(null);
        this.createCategoryButton.setOnMouseClicked(null);
        this.categoriesSelector.setOnAction(null);
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