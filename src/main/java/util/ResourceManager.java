package util;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import javafx.scene.image.Image;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static util.Constants.*;

public class ResourceManager {

    private static final String ROOT_PATH = "/de/uniks/stp";

    public static int loadHighScore() {
        int highScore = 0;

        // if file not exists - create and put highScore = 0
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH));
            }
            if (!Files.exists(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore.json"))) {
                Files.createFile(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore.json"));
                BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore.json"));
                JsonObject obj = new JsonObject();
                obj.put("highScore", highScore);
                Jsoner.serialize(obj, writer);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load file and highScore
        try {
            Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore.json"));
            JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
            BigDecimal value = (BigDecimal) parser.get("highScore");
            highScore = value.intValue();
            reader.close();
        } catch (JsonException |
                IOException e) {
            e.printStackTrace();
        }

        return highScore;
    }


    public static void saveHighScore(int highScore) {

        try {
            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore.json"));
            JsonObject obj = new JsonObject();
            obj.put("highScore", highScore);

            Jsoner.serialize(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Image loadSnakeGameIcon(String image) {
        return new Image(ResourceManager.class.getResource(ROOT_PATH + "/snake/" + image + ".png").toString());
    }
}
