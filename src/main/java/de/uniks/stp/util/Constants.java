package de.uniks.stp.util;

@SuppressWarnings("ALL")
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
    public static final String SERVER_MESSAGE_PATH = "/messages";
    public static final String SERVER_INVITES = "/invites";
    public static final String SERVER_AUDIO_JOIN = "/join";
    public static final String SERVER_AUDIO_LEAVE = "/leave";
    public static final String SERVER_USER_DESCRIPTION = "/description";

    // Client
    public static final String REST_SERVER_URL = "https://ac.uniks.de";
    public static final String WS_SERVER_URL = "wss://ac.uniks.de";
    public static final String STEAM_API_PLAYER_SUMMARIES = "/GetPlayerSummaries/v0002";
    // Rescources
    public static final String ROOT_PATH = "/de/uniks/stp";
    // Local user
    public static String APPDIR_ACCORD_PATH;
    public static String CONFIG_PATH = "/config";
    public static String SAVES_PATH = "/saves";
    public static String SNAKE_PATH = "/snake";
    public static String TEMP_PATH = "/temp";
    public static String EMOJIS_PATH = "/emojis";
    public static String PRIVATE_CHAT_PATH = "/private";
    public static String BLOCKEDUSERS_PATH = "/blockedUsers";
    public static String SETTINGS_FILE = "/Settings.properties";
    public static String USERDATA_FILE = "/userData.txt";
    public static String NOTIFICATION_PATH = "/soundNotifications";
    // AudioStream
    public static String AUDIO_STREAM_ADDRESS = "cranberry.uniks.de";
    public static int AUDIO_STREAM_PORT = 33100;
    public static float AUDIO_BITRATE = 48000.0f;
    public static int AUDIO_SAMPLE_SIZE = 16;
    public static int AUDIO_CHANNELS = 1;
    public static boolean AUDIO_SIGNING = true;
    public static boolean AUDIO_BYTE_ORDER = false;
    public static int AUDIO_DATAGRAM_PAKET_SIZE = 1279;

    // Steam
    public static String STEAM_API_BASE_URL = "http://api.steampowered.com";
    public static String STEAM_API_STEAM_USER = "/ISteamUser";
    public static String STEAM_API_RESOLVE_VANITY = "/ResolveVanityURL/v0001";
    public static String STEAM_API_KEY = "/?key=EF900FD8AD0781BF2B21710D5F173577";

    // Accord Version
    public static String ACCORD_VERSION_NR = "4.0.0";
}