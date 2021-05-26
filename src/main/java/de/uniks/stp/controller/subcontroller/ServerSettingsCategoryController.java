package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ServerSettingsCategoryController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private ComboBox categoriesSelector;
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

        categoriesSelector = (ComboBox) view.lookup("#editCategoriesSelector");
        categoryNameTextField = (TextField) view.lookup("#editCategoryNameTextField");
        changeCategoryNameButton = (Button) view.lookup("#changeCategoryNameButton");
        deleteCategoryButton = (Button) view.lookup("#deleteCategoryButton");
        createCategoryNameTextField = (TextField) view.lookup("#createCategoryNameTextField");
        createCategoryButton = (Button) view.lookup("#createCategoryButton");

        changeCategoryNameButton.setOnAction(this::changeCategoryName);
        deleteCategoryButton.setOnAction(this::deleteCategory);
        createCategoryButton.setOnAction(this::createCategory);

        categoryNameTextField.setPromptText("Change name..."); //to set the hint text
        categoryNameTextField.getParent().requestFocus();
        createCategoryNameTextField.setPromptText("Create new one..."); //to set the hint text
        createCategoryNameTextField.getParent().requestFocus();

        // load categories
        this.categoriesSelector.getItems().clear();
        for (Categories category : builder.getCurrentServer().getCategories()) {
            this.categoriesSelector.getItems().add(category.getName());
        }
        categoriesSelector.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                System.out.println("Selected: " + newValue);
            }
        });
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
        this.categoriesSelector.valueProperty().addListener((InvalidationListener) null);
    }
}
