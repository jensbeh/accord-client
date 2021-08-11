package de.uniks.stp.util;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiParser;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.uniks.stp.util.Constants.*;

@SuppressWarnings("unchecked")
public class EmojiLoaderService {

    private final Parent view;
    private final ModelBuilder builder;
    private TabPane emojiTabPane;
    private ComboBox<Image> skinColorComboBox;

    public EmojiLoaderService(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        emojiTabPane = (TabPane) view.lookup("#emojiTabPane");
        emojiTabPane.setCache(true);
        emojiTabPane.setCacheHint(CacheHint.SPEED);
        skinColorComboBox = (ComboBox<Image>) view.lookup("#ComboBox_skinColor");
        TextField searchTextField = (TextField) view.lookup("#textField_search");
        ScrollPane searchScrollPane = (ScrollPane) view.lookup("#scrollPane_search");
        searchTextField.setPromptText("Search...");
        searchScrollPane.setId("emojiScrollPane");
        searchScrollPane.getStyleClass().add("emojiScrollPane");

        FlowPane searchFlowPane = (FlowPane) searchScrollPane.getContent();
        searchFlowPane.setPadding(new Insets(10, 0, 10, 10));
        searchScrollPane.setVisible(false);
        searchScrollPane.setCache(true);
        searchScrollPane.setCacheHint(CacheHint.SPEED);
        searchFlowPane.setCache(true);
        searchFlowPane.setCacheHint(CacheHint.SPEED);
        searchFlowPane.getStyleClass().add("emojiFlowPane");

        ObservableList<Image> tonesList = FXCollections.observableArrayList();

        for (int i = 1; i <= 5; i++) {
            Emoji emoji = EmojiParser.getInstance().getEmoji(":thumbsup_tone" + i + ":");
            Image image = getEmojiImage(emoji).getImage();
            tonesList.add(image);
        }

        skinColorComboBox.setItems(tonesList);
        skinColorComboBox.setCellFactory(e -> new ToneCell());
        skinColorComboBox.setButtonCell(new ToneCell());
        skinColorComboBox.getSelectionModel().selectedItemProperty().addListener(e -> refreshTabs());
        skinColorComboBox.setCache(true);
        skinColorComboBox.setCacheHint(CacheHint.SPEED);


        searchScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        searchFlowPane.prefWidthProperty().bind(searchScrollPane.widthProperty().subtract(5));
        searchFlowPane.setHgap(5);
        searchFlowPane.setVgap(5);

        searchTextField.textProperty().addListener(x -> {
            String text = searchTextField.getText();
            if (text.length() < 2) {
                searchFlowPane.getChildren().clear();
                searchScrollPane.setVisible(false);
                searchTextField.setPromptText("Search...");
            } else {
                searchScrollPane.setVisible(true);
                List<Emoji> results = EmojiParser.getInstance().search(text);
                searchFlowPane.getChildren().clear();
                results.forEach(emoji -> searchFlowPane.getChildren().add(setupImageStackPane(emoji)));
            }
        });


        for (Tab tab : emojiTabPane.getTabs()) {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            scrollPane.getStyleClass().add("emojiScrollPane");
            FlowPane pane = (FlowPane) scrollPane.getContent();
            pane.setPadding(new Insets(12, 0, 10, 14));
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            pane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(5));
            pane.setHgap(5);
            pane.setVgap(5);
            pane.getStyleClass().add("emojiFlowPane");

            ImageView icon = new ImageView();
            icon.setCache(true);
            icon.setCacheHint(CacheHint.SPEED);
            icon.setFitWidth(22);
            icon.setFitHeight(22);
            switch (tab.getId()) {
                case "frequently used":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":heart:")).getImage());
                    break;
                case "people":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":smiley:")).getImage());
                    break;
                case "nature":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":dog:")).getImage());
                    break;
                case "food":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":apple:")).getImage());
                    break;
                case "activity":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":soccer:")).getImage());
                    break;
                case "travel":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":airplane:")).getImage());
                    break;
                case "objects":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":bulb:")).getImage());
                    break;
                case "symbols":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":atom:")).getImage());
                    break;
                case "flags":
                    icon.setImage(getEmojiImage(EmojiParser.getInstance().getEmoji(":flag_eg:")).getImage());
                    break;
            }

            if (icon.getImage() != null) {
                tab.setText("");
                tab.setGraphic(icon);
            }
            tab.setTooltip(new Tooltip(tab.getId()));
        }
        skinColorComboBox.getSelectionModel().select(0);
        emojiTabPane.getSelectionModel().select(1);
    }

    private void refreshTabs() {
        Map<String, List<Emoji>> map = EmojiParser.getInstance().getCategorizedEmojis(skinColorComboBox.getSelectionModel().getSelectedIndex() + 1);
        for (Tab tab : emojiTabPane.getTabs()) {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            FlowPane pane = (FlowPane) scrollPane.getContent();
            pane.setCache(true);
            pane.setCacheHint(CacheHint.SPEED);
            pane.getChildren().clear();
            String category = tab.getId().toLowerCase();
            if (map.get(category) == null) continue;
            for (Emoji emoji : map.get(category)) {
                pane.getChildren().add(setupImageStackPane(emoji));
            }
        }
    }

    static class ToneCell extends ListCell<Image> {
        private final ImageView imageView;

        public ToneCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            imageView = new ImageView();
            imageView.setCache(true);
            imageView.setCacheHint(CacheHint.SPEED);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
        }

        @Override
        protected void updateItem(Image item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                imageView.setImage(item);
                setGraphic(imageView);
            }
        }
    }

    /**
     * creates StackPane for each image
     */
    private StackPane setupImageStackPane(Emoji emoji) {
        StackPane stackPane = new StackPane();
        stackPane.setCache(true);
        stackPane.setCacheHint(CacheHint.SPEED);
        stackPane.setMaxSize(32, 32);
        stackPane.setPrefSize(32, 32);
        stackPane.setMinSize(32, 32);
        stackPane.setPadding(new Insets(3));

        ImageView emojiImage = getEmojiImage(emoji);
        emojiImage.setCache(true);
        emojiImage.setCacheHint(CacheHint.SPEED);

        stackPane.getChildren().add(emojiImage);

        Tooltip tooltip = new Tooltip(emoji.getShortname());
        Tooltip.install(stackPane, tooltip);
        stackPane.setCursor(Cursor.HAND);
        ScaleTransition st = new ScaleTransition(Duration.millis(90), emojiImage);
        stackPane.setOnMouseClicked(event -> {
            System.out.println(emoji.getShortname());
            System.out.println(emoji.getUnicode());
            System.out.println(emoji.getHex());

            builder.getCurrentChatViewController().onEmojiClicked(emoji.getShortname());
        });

        stackPane.setOnMouseEntered(e -> {
            emojiImage.setEffect(new DropShadow());
            st.setToX(1.2);
            st.setToY(1.2);
            st.playFromStart();
        });
        stackPane.setOnMouseExited(e -> {
            emojiImage.setEffect(null);
            st.setToX(1.);
            st.setToY(1.);
            st.playFromStart();
        });
        return stackPane;
    }

    /**
     * get correct image to the hexStr and sets the textField if emoji clicked
     *
     * @param emoji is the emoji which should be load with image
     */
    private ImageView getEmojiImage(Emoji emoji) {
        ImageView imageView = new ImageView();
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);
        imageView.setId(emoji.getShortname());
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);

        String path = APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH + "/" + emoji.getHex() + ".png";
        File newFile = new File(path);
        Image image = new Image(newFile.toURI().toString(), true);
        imageView.setImage(image);
        return imageView;
    }

    public Parent getView() {
        return this.view;
    }


    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        view.getStylesheets().clear();
        view.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/EmojiView.css")).toExternalForm());
    }

    private void setDarkMode() {
        view.getStylesheets().clear();
        view.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/EmojiView.css")).toExternalForm());
    }
}
