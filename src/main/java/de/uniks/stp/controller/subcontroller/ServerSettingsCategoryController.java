package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import kong.unirest.JsonNode;
import org.json.JSONObject;

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
                    } else {
                        System.out.println(status);
                        System.out.println(body.getObject().getString("message"));
                    }
                });
            } else {
                System.out.println("--> ERR: New name equals old name");
            }
        } else {
            System.out.println("--> ERR: No Category selected OR Field is empty");
        }
    }

    /**
     * deletes an existing and chosen category when button delete is clicked
     */
    private void deleteCategory(ActionEvent actionEvent) {
        if (selectedCategory != null) {
            restClient.deleteCategory(currentServer.getId(), selectedCategory.getId(), builder.getPersonalUser().getUserKey(), response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {
                    Platform.runLater(() -> categoriesSelector.getItems().remove(selectedCategory));
                    Platform.runLater(() -> categoriesSelector.getSelectionModel().clearSelection());
                } else {
                    System.out.println(status);
                    System.out.println(body.getObject().getString("message"));
                }
            });
        } else {
            System.out.println("--> ERR: No Category selected");
        }
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
                } else {
                    System.out.println(status);
                    System.out.println(body.getObject().getString("message"));
                }
            });
        } else {
            System.out.println("--> ERR: Field is empty");
        }
    }

    public void stop() {
        this.changeCategoryNameButton.setOnMouseClicked(null);
        this.deleteCategoryButton.setOnMouseClicked(null);
        this.createCategoryButton.setOnMouseClicked(null);
        this.categoriesSelector.setOnAction(null);
    }
}