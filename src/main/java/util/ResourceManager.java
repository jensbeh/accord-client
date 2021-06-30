package util;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.PrivateChat;
import javafx.scene.image.Image;

import java.io.*;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static util.Constants.*;

public class ResourceManager {

    private static final String ROOT_PATH = "/de/uniks/stp";
    private static String comboValue = "";

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
        return new Image(Objects.requireNonNull(ResourceManager.class.getResource(ROOT_PATH + "/snake/" + image + ".png")).toString());
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
            if (f.exists()) {
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
        if (f.exists()) {
            Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + PRIVATE_CHAT_PATH + "/chat_" + currentUserName + "_" + chatPartnerName + ".json"));
            parser = (JsonArray) Jsoner.deserialize(reader);
            for (Object jsonObject : parser) {
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

    public static void extractEmojis() {
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH));

                URL zipFileURL = Thread.currentThread().getContextClassLoader().getResource("de/uniks/stp/twemoji.zip");
                InputStream inputStream = zipFileURL.openStream();
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    String filePath = APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH + File.separator + entry.getName();
                    // if the entry is a file, extracts it
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                    byte[] bytesIn = new byte[4096];
                    int read;
                    while ((read = zipInputStream.read(bytesIn)) != -1) {
                        bos.write(bytesIn, 0, read);
                    }
                    bos.close();

                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                }
                zipInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * save sound
     */
    public static void saveNotifications(File file) {
        try {
            if (!Files.exists(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH + "/" + file.getName()))) {
                if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH))) {
                    Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH));
                }
                String targetPath = APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH + "/" + file.getName();
                copyFile(file, targetPath);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * get sound
     */
    public static List<File> getNotificationSoundFiles() {
        List<File> listOfFiles = new ArrayList<>();
        if (Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH))) {
            File folder = new File(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH);
            System.out.println(folder);
            listOfFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(folder.listFiles())));
            return listOfFiles;
        }
        return listOfFiles;
    }

    /**
     * delete sound
     */
    public static void deleteNotificationSound(String name) {
        File deleteFile = null;
        if (Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH))) {
            File folder = new File(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH);
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                String fileName = file.getName().substring(0, file.getName().length() - 4);
                if (fileName.equals(name)) {
                    deleteFile = file;
                    if (file.exists()) {
                        deleteFile.delete();
                    } else {
                        System.out.println("File does not exist!");
                    }
                }
            }
        }
    }

    /**
     * get value of comboBox
     */
    public static String getComboValue(String currentUserName) {
        comboValue = "";
        if (new File(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/" + currentUserName + ".json").exists()) {
            try {
                Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/" + currentUserName + ".json"));
                JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
                comboValue = (String) parser.get("fileName");
                reader.close();
            } catch (JsonException |
                    IOException e) {
                e.printStackTrace();
            }
        }
        return comboValue;
    }

    /**
     * set value of comboBox
     */
    public static void setComboValue(String userName, String comboValue) {
        ResourceManager.comboValue = comboValue;
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/"))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/"));
            }
            if (!Files.exists(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/" + comboValue + ".wav"))) {
                for (File file : Objects.requireNonNull(new File(APPDIR_ACCORD_PATH
                        + SAVES_PATH + NOTIFICATION_PATH).listFiles())) {
                    if (file.getName().substring(0, file.getName().length() - 4).equals(comboValue)) {
                        saveUserNameAndSound(userName, file.getName().substring(0, file.getName().length() - 4));
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * copy file
     */
    private static void copyFile(File file, String targetPath) throws IOException, URISyntaxException {
        FileChannel source = new FileInputStream(file).getChannel();
        FileChannel desti = new FileOutputStream(targetPath).getChannel();
        desti.transferFrom(source, 0, source.size());
        source.close();
        desti.close();
    }

    /**
     * save fileName
     */
    public static void saveUserNameAndSound(String currentUserName, String fileName) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/" + currentUserName + ".json"));
            JsonObject obj = new JsonObject();
            obj.put("fileName", fileName);
            Jsoner.serialize(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * save volume
     */
    public static void saveVolume(String currentUserName, Float volume) {
        try {
            if (!Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/Volume"))) {
                Files.createDirectories(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/Volume"));
            }
            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/Volume/" + currentUserName + ".json"));
            JsonObject obj = new JsonObject();
            obj.put("volume", volume);
            Jsoner.serialize(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * get value of comboBox
     */
    public static Float getVolume(String currentUserName) {
        Float volume = 0.0f;
        if (new File(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/" + currentUserName + ".json").exists()) {
            try {
                Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/Volume/" + currentUserName + ".json"));
                JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
                comboValue = (String) parser.get("volume");
                reader.close();
            } catch (JsonException |
                    IOException e) {
                e.printStackTrace();
            }
        }
        return volume;
    }
}
