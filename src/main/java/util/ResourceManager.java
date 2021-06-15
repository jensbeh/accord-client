package util;

import javafx.scene.image.Image;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static util.Constants.*;

public class ResourceManager {

    private static final String ROOT_PATH = "/de/uniks/stp";

    public static int loadHighScore() {
        int highScore = 0;
        JSONParser jsonParser = new JSONParser();
        try {
            Object obj = jsonParser.parse(new FileReader(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore.json"));
            JSONObject jsonObject = (JSONObject)obj;
            String str = jsonObject.get("highScore").toString();
            highScore = Integer.parseInt(str);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }


        return highScore;
    }


    public static void saveHighScore(int highScore) {
        try (FileWriter file = new FileWriter(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore.json")) {
            //JSONObject jsonObject = (JSONObject) new JSONObject().put("highScore", highScore);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("highScore", highScore);


            file.write(jsonObject.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Image loadSnakeGameIcon(String image) {
        return new Image(ResourceManager.class.getResource(ROOT_PATH + "/snake/" + image + ".png").toString());
    }
}
