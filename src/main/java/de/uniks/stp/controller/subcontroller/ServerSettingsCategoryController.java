package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class ServerSettingsCategoryController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private ComboBox<Categories> categoriesSelector;
    private TextField categoryNameTextField;
    private Button changeCategoryNameButton;
    private Button deleteCategoryButton;
    private TextField createCategoryNameTextField;
    private Button createCategoryButton;


    public ServerSettingsCategoryController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
    }

    public void init() {

        categoriesSelector = (ComboBox<Categories>) view.lookup("#editCategoriesSelector");
        categoryNameTextField = (TextField) view.lookup("#editCategoryNameTextField");
        changeCategoryNameButton = (Button) view.lookup("#changeCategoryNameButton");
        deleteCategoryButton = (Button) view.lookup("#deleteCategoryButton");
        createCategoryNameTextField = (TextField) view.lookup("#createCategoryNameTextField");
        createCategoryButton = (Button) view.lookup("#createCategoryButton");

        changeCategoryNameButton.setOnAction(this::changeCategoryName);
        deleteCategoryButton.setOnAction(this::deleteCategory);
        createCategoryButton.setOnAction(this::createCategory);

        // load categories
        this.categoriesSelector.getItems().clear();
        this.categoriesSelector.setOnAction(this::onCategoryClicked);

        for (Categories category : builder.getCurrentServer().getCategories()) {
            this.categoriesSelector.getItems().add(category);
            categoriesSelector.setConverter(new StringConverter<Categories>() {
                @Override
                public String toString(Categories object) {
                    if (object == null) {
                        return "Select category...";
                    }
                    return category.getName();
                }

                @Override
                public Categories fromString(String string) {
                    return null;
                }
            });
        }
        Platform.runLater(() -> {
            categoriesSelector.getSelectionModel().clearSelection();
        });
    }

    private void onCategoryClicked(Event event) {
        Categories selectedCategory = this.categoriesSelector.getValue();
        System.out.println("Selected Category: " + selectedCategory);
    }

    /**
     * changes the name of an existing category
     */
    private void changeCategoryName(ActionEvent actionEvent) {

    }


    /**
     * deletes an existing and chosen category
     */

    private void deleteCategory(ActionEvent actionEvent) {

    }

    /**
     * creates a new category
     */
    private void createCategory(ActionEvent actionEvent) {

    }

    public void stop() {
        this.changeCategoryNameButton.setOnMouseClicked(null);
        this.deleteCategoryButton.setOnMouseClicked(null);
        this.createCategoryButton.setOnMouseClicked(null);
        this.categoriesSelector.setOnAction(null);
    }
}