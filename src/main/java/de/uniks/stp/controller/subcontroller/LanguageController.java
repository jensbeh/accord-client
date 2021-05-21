package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.LangString;
import de.uniks.stp.StageManager;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import util.Constants;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class LanguageController extends SubSetting {

    private Parent view;
    private ComboBox<String> languageSelector;
    private static Label selectLanguageLabel;
    private static final String PATH_FILE_SETTINGS = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH + Constants.SETTINGS_FILE;
    ;
    Map<String, String> languages = new HashMap<>();
    Map<String, Locale> locales = new HashMap<>();

    public static void setup() {
        // load language from Settings
        Properties prop = new Properties();
        try {
            FileInputStream ip = new FileInputStream(PATH_FILE_SETTINGS);
            prop.load(ip);
            LangString.setLocale(new Locale(prop.getProperty("LANGUAGE")));
            StageManager.resetLangBundle();
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public LanguageController(Parent view) {
        this.view = view;
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

        this.languageSelector.setPromptText(languages.get((LangString.getLocale().toString())));
        for (Map.Entry<String, String> language : languages.entrySet()) {
            this.languageSelector.getItems().add(language.getValue());
        }

        this.languageSelector.setOnAction(this::onLanguageChanged);
    }

    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        languageSelector.setOnAction(null);
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
        LangString.setLocale(locales.get(language));
        StageManager.onLanguageChanged();

        // save in Settings
        Properties prop = new Properties();
        try {
            FileOutputStream op = new FileOutputStream(PATH_FILE_SETTINGS);
            prop.setProperty("LANGUAGE", language);
            prop.store(op, null);
        } catch (Exception e) {
            System.err.println(e);
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
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        selectLanguageLabel.setText(lang.getString("label.select_language"));
    }

}
