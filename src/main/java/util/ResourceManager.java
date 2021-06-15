package util;

import javafx.scene.image.Image;

public class ResourceManager {

    private static final String ROOT_PATH = "/de/uniks/stp";

    public static int loadHighScore() {
        return 0;
    }

    public static void saveHighScore(int highScore) {

    }

    public static Image loadSnakeGameIcon(String image) {
        return new Image(ResourceManager.class.getResource(ROOT_PATH + "/snake/" + image + ".png").toString());
    }
}
