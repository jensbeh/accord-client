package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.util.Constants;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class GeneralController extends SubSetting {

    private final Parent view;
    private final String PATH_FILE_SETTINGS = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH + Constants.SETTINGS_FILE;
    private final ModelBuilder builder;
    Map<String, String> languages = new HashMap<>();
    Map<String, Locale> locales = new HashMap<>();
    private ComboBox<String> languageSelector;
    private Label selectLanguageLabel;
    private Label selectThemeLabel;
    private Locale currentLocale;
    private VBox darkModeBox;
    private VBox brightModeBox;

    public GeneralController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void setup() {
        // load language from Settings
        Properties prop = new Properties();
        try {
            FileInputStream ip = new FileInputStream(PATH_FILE_SETTINGS);
            prop.load(ip);
            currentLocale = new Locale(prop.getProperty("LANGUAGE"));
            Locale.setDefault(currentLocale);
            builder.getStageManager().resetLangBundle();
        } catch (Exception e) {
            System.err.println(e + "");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void init() {
        // add languages
        languages.put("en", "English");
        languages.put("de", "Deutsch");

        for (Map.Entry<String, String> language : languages.entrySet()) {
            String tmp = language.getKey();
            locales.put(tmp, new Locale(tmp));
        }

        // init view
        this.languageSelector = (ComboBox<String>) view.lookup("#comboBox_langSelect");
        selectLanguageLabel = (Label) view.lookup("#label_langSelect");
        selectThemeLabel = (Label) view.lookup("#label_themeSelect");

        this.languageSelector.setPromptText(languages.get(currentLocale.toString()));
        for (Map.Entry<String, String> language : languages.entrySet()) {
            this.languageSelector.getItems().add(language.getValue());
        }

        this.languageSelector.setOnAction(this::onLanguageChanged);

        this.brightModeBox = (VBox) view.lookup("#brightMode");
        this.brightModeBox.setOnMouseClicked(this::setBright);
        this.darkModeBox = (VBox) view.lookup("#darkMode");
        this.darkModeBox.setOnMouseClicked(this::setDark);


    }

    /**
     * when the user changes the language from the comboBox then switch application language and save into user local settings
     *
     * @param mouseEvent the mouse click event
     */
    private void setBright(MouseEvent mouseEvent) {
        // get selected language and change
        if (mouseEvent.getClickCount() == 1) {
            String selectedTheme = "Bright";
            builder.setTheme(selectedTheme);
            builder.saveSettings();
            builder.getStageManager().setTheme();
        }
    }

    /**
     * when the user changes the language from the comboBox then switch application language and save into user local settings
     *
     * @param mouseEvent the mouse click event
     */
    private void setDark(MouseEvent mouseEvent) {
        // get selected language and change
        if (mouseEvent.getClickCount() == 1) {
            String selectedTheme = "Dark";
            builder.setTheme(selectedTheme);
            builder.saveSettings();
            builder.getStageManager().setTheme();
        }
    }

    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        languageSelector.setOnAction(null);
        darkModeBox.setOnMouseClicked(null);
        brightModeBox.setOnMouseClicked(null);
    }

    /**
     * when the user changes the language from the comboBox then switch application language and save into user local settings
     *
     * @param actionEvent the mouse click event
     */
    private void onLanguageChanged(ActionEvent actionEvent) {
        // get selected language and change
        String selectedLanguage = this.languageSelector.getValue();
        String language = getKey(languages, selectedLanguage);
        currentLocale = locales.get(language);
        Locale.setDefault(currentLocale);
        builder.getStageManager().onLanguageChanged();
        onLanguageChanged();

        // save in Settings
        Properties prop = new Properties();
        try {
            FileOutputStream op = new FileOutputStream(PATH_FILE_SETTINGS);
            prop.setProperty("LANGUAGE", language);
            prop.store(op, null);
        } catch (Exception e) {
            System.err.println(e + "");
            e.printStackTrace();
        }
    }

    private <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = builder.getStageManager().getLangBundle();
        selectLanguageLabel.setText(lang.getString("label.select_language"));
        selectThemeLabel.setText(lang.getString("label.select_theme"));
    }
}
