package util;

public class Constants {
    // Server
    public static final String WEBSOCKET_PATH = "/ws";
    public static final String CHAT_WEBSOCKET_PATH = "/chat?user=";
    public static final String SYSTEM_WEBSOCKET_PATH = "/system";
    public static final String SERVER_SYSTEM_WEBSOCKET_PATH = "/system?serverId=";
    public static final String SERVER_WEBSOCKET_PATH = "&serverId=";
    public static final String API_PREFIX = "/api";
    public static final String USERS_PATH = "/users";
    public static final String LOGIN_PATH = "/users/login";
    public static final String LOGOUT_PATH = "/users/logout";
    public static final String TEMP_USER_PATH = "/users/temp";
    public static final String SERVER_PATH = "/servers";
    public static final String LEAVE_PATH = "/leave";
    public static final String SERVER_CATEGORIES_PATH = "/categories";
    public static final String SERVER_CHANNELS_PATH = "/channels";
    public static final String SERVER_MESSAGES_PATH = "/messages?timestamp=";
    public static final String SERVER_INVITES = "/invites";


    // Communication
    public static final String COM_USERKEY = "userKey";
    public static final String COM_NOOP = "noop";

    // Client
    public static final String REST_SERVER_URL = "https://ac.uniks.de";
    public static final String WS_SERVER_URL = "wss://ac.uniks.de";

    // Local user
    public static String APPDIR_ACCORD_PATH;
    public static String CONFIG_PATH = "/config";
    public static String SAVES_PATH = "/saves";
    public static String SNAKE_PATH = "/snake";
    public static String TEMP_PATH = "/temp";
    public static String EMOJIS_PATH = "/emojis";
    public static String PRIVATE_CHAT_PATH = "/private";
    public static String SETTINGS_FILE = "/Settings.properties";
    public static String USERDATA_FILE = "/userData.txt";
}