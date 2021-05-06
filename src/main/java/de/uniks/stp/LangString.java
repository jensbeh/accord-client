package de.uniks.stp;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static javax.swing.JComponent.getDefaultLocale;

public class LangString {

    // current locale / language
    private static final ObjectProperty<Locale> locale;

    static {
        locale = new SimpleObjectProperty<>(getDefaultLocale());
        locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    // create String binding to a localized String
    // example usage: arg0 = "Hello %s" arg1 = name
    public static StringBinding lStr(final String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), locale);
    }

    // gets the string with the given key
    public static String get(final String key, final Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("de/uniks/stp/LangBundle", getLocale());
        return MessageFormat.format(bundle.getString(key), args);
    }

    // get locale / language
    public static Locale getLocale() {
        return locale.get();
    }

    // set locale / language
    public static void setLocale(Locale locale) {
        localeProperty().set(locale);
        Locale.setDefault(locale);
    }

    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

}
