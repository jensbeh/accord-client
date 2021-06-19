package util;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.ServerViewController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.PrivateChat;
import javafx.scene.image.Image;

import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static util.Constants.*;

public class ResourceManager {

    private static final String ROOT_PATH = "/de/uniks/stp";

    /**
     * load highScore from file
     */
    public static int loadHighScore(String currentUserName) {
        int highScore = 0;

        // if file not exists - create and put highScore = 0
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH));
            }
            if (!Files.exists(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore_" + currentUserName + ".json"))) {
                Files.createFile(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore_" + currentUserName + ".json"));
                BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore_" + currentUserName + ".json"));
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
            Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore_" + currentUserName + ".json"));
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


    /**
     * save highScore to file
     */
    public static void saveHighScore(String currentUserName, int highScore) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/highscore_" + currentUserName + ".json"));
            JsonObject obj = new JsonObject();
            obj.put("highScore", highScore);

            Jsoner.serialize(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load snakeGameIcons from file
     */
    public static Image loadSnakeGameIcon(String image) {
        return new Image(ResourceManager.class.getResource(ROOT_PATH + "/snake/" + image + ".png").toString());
    }

    /**
     * load muteGame state from file
     */
    public static boolean loadMuteGameState(String currentUserName) {
        // if file not exists - create and put highScore = 0
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH));
            }
            if (!Files.exists(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/muteSettings_" + currentUserName + ".json"))) {
                Files.createFile(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/muteSettings_" + currentUserName + ".json"));
                BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/muteSettings_" + currentUserName + ".json"));
                JsonObject obj = new JsonObject();
                obj.put("isGameMute", false);
                Jsoner.serialize(obj, writer);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load file and highScore
        boolean isGameMute = false;
        try {
            Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/muteSettings_" + currentUserName + ".json"));
            JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
            isGameMute = (boolean) parser.get("isGameMute");
            reader.close();

        } catch (JsonException |
                IOException e) {
            e.printStackTrace();
        }
        return isGameMute;
    }

    /**
     * save muteGame state to file
     */
    public static void saveMuteGameState(boolean isGameMute, String currentUserName) {
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH));
            }
            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + SNAKE_PATH + "/muteSettings_" + currentUserName + ".json"));
            JsonObject obj = new JsonObject();
            obj.put("isGameMute", isGameMute);

            Jsoner.serialize(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * save privateChat to file
     */
    public static void savePrivatChat(String currentUserName, String chatPartnerName, Message message) {
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH));
            }

            JsonArray parser = new JsonArray();
            File f = new File(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH + "/chat_" + currentUserName + "_" + chatPartnerName + ".json");
            if(f.exists()){
                Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH + "/chat_" + currentUserName + "_" + chatPartnerName + ".json"));
                parser = (JsonArray) Jsoner.deserialize(reader);
            }
            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH + "/chat_" + currentUserName + "_" + chatPartnerName + ".json"));


            JsonObject obj = new JsonObject();
            obj.put("currentUserName", message.getFrom());
            obj.put("chatPartnerName", chatPartnerName);
            obj.put("message", message.getMessage());
            obj.put("timestamp", message.getTimestamp());
            parser.add(obj);

            System.out.println("savePrivatChat: " + message);
            Jsoner.serialize(parser, writer);
            writer.close();
        } catch (IOException | JsonException e) {
            e.printStackTrace();
        }
    }

    /**
     * load privateChat from file
     */
    public static ArrayList<Message> loadPrivatChat(String currentUserName, String chatPartnerName, PrivateChat privateChat) throws IOException, JsonException {
        JsonArray parser;
        ArrayList<Message> messageList = new ArrayList<>();

        File f = new File(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH + "/chat_" + currentUserName + "_" + chatPartnerName + ".json");
        if(f.exists()){
            Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH + "/chat_" + currentUserName + "_" + chatPartnerName + ".json"));
            parser = (JsonArray) Jsoner.deserialize(reader);
            for(Object jsonObject : parser){
                Message message = new Message();
                JsonObject jsonObject1 = (JsonObject) jsonObject;
                message.setMessage((String) jsonObject1.get("message"));
                message.setFrom((String) jsonObject1.get("currentUserName"));
                message.setPrivateChat(privateChat);
                message.setTimestamp(((BigDecimal) jsonObject1.get("timestamp")).longValue());
                messageList.add(message);
            }
        }
        return messageList;
    }
}
