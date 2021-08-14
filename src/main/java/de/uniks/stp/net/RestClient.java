package de.uniks.stp.net;

import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONObject;

import static de.uniks.stp.util.Constants.*;

public class RestClient {

    public void signIn(String username, String password, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("password", password).accumulate("name", username);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH).body(body);
        sendRequest(request, callback);
    }

    public void login(String username, String password, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", username).accumulate("password", password);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + LOGIN_PATH).body(body);
        sendRequest(request, callback);
    }

    public void loginTemp(Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + TEMP_USER_PATH);
        sendRequest(request, callback);
    }

    public void logout(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + LOGOUT_PATH).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void getServers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void getUsers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.get(REST_SERVER_URL + API_PREFIX + USERS_PATH).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void getServerUsers(String serverId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId;
        HttpRequest<?> postRequest = Unirest.get(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    public void getServerCategories(String serverId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH;
        HttpRequest<?> postRequest = Unirest.get(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    public void getCategoryChannels(String serverId, String categoryId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + categoryId + SERVER_CHANNELS_PATH;
        HttpRequest<?> postRequest = Unirest.get(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    public void postServerLeave(String serverId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + LEAVE_PATH;
        HttpRequest<?> postRequest = Unirest.post(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    public JsonNode postServer(String userKey, String serverName, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", serverName);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH).body(body).header("userKey", userKey);
        sendRequest(request, callback);
        return null;
    }

    public void updateChannel(String serverId, String categoryId, String channelId, String userKey, String channelName, boolean privilege, String[] Members, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", channelName).accumulate("privileged", privilege).accumulate("members", Members);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + categoryId + SERVER_CHANNELS_PATH + "/" + channelId).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void putServer(String serverId, String serverName, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", serverName);
        String body = JSONObject.valueToString(jsonObj);
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId;
        HttpRequest<?> postRequest = Unirest.put(url).header("userKey", userKey).body(body);
        sendRequest(postRequest, callback);
    }

    public void deleteServer(String serverId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId;
        HttpRequest<?> postRequest = Unirest.delete(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    public void createChannel(String serverId, String categoryId, String userKey, String channelName, String type, boolean privileged, String[] members, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", channelName).accumulate("type", type).accumulate("privileged", privileged).accumulate("members", members);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + categoryId + SERVER_CHANNELS_PATH).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void deleteChannel(String serverId, String categoryId, String channelId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + categoryId + SERVER_CHANNELS_PATH + "/" + channelId;
        HttpRequest<?> postRequest = Unirest.delete(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }

    public void createTempLink(String type, Integer max, String serverId, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("type", type);
        if (type.equals("count")) {
            jsonObj.accumulate("max", max);
        }
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_INVITES).header("userKey", userKey).body(body);
        sendRequest(request, callback);
    }

    public void getInvLinks(String serverId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_INVITES;
        HttpRequest<?> postRequest = Unirest.get(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    public void deleteInvLink(String serverId, String invId, String userKey, Callback<JsonNode> callback) {
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_INVITES + "/" + invId;
        HttpRequest<?> postRequest = Unirest.delete(url).header("userKey", userKey);
        sendRequest(postRequest, callback);
    }

    public void joinServer(String serverId, String inviteId, String username, String password, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", username).accumulate("password", password);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_INVITES + "/" + inviteId).header("userKey", userKey).body(body);
        sendRequest(request, callback);
    }

    public void createCategory(String serverId, String categoryName, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", categoryName);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void updateCategory(String serverId, String categoryId, String categoryName, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", categoryName);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + categoryId).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void deleteCategory(String serverId, String categoryId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.delete(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + categoryId).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void getChannelMessages(long timestamp, String serverId, String catId, String channelId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + catId + SERVER_CHANNELS_PATH + "/" + channelId + SERVER_MESSAGES_PATH + timestamp).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void joinVoiceChannel(String serverId, String catId, String channelId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + catId + SERVER_CHANNELS_PATH + "/" + channelId + SERVER_AUDIO_JOIN).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void leaveVoiceChannel(String serverId, String catId, String channelId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + catId + SERVER_CHANNELS_PATH + "/" + channelId + SERVER_AUDIO_LEAVE).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void updateMessage(String serverId, String catId, String channelId, String msgId, String text, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("text", text);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId
                + SERVER_CATEGORIES_PATH + "/" + catId + SERVER_CHANNELS_PATH + "/" + channelId + SERVER_MESSAGE_PATH
                + "/" + msgId).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void deleteMessage(String serverId, String catId, String channelId, String msgId, String text, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("text", text);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.delete(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId
                + SERVER_CATEGORIES_PATH + "/" + catId + SERVER_CHANNELS_PATH + "/" + channelId + SERVER_MESSAGE_PATH
                + "/" + msgId).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void resolveVanityID(String vanityURL, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.get(STEAM_API_BASE_URL + STEAM_API_STEAM_USER + STEAM_API_RESOLVE_VANITY + STEAM_API_KEY + "&vanityurl=" + vanityURL);
        sendRequest(request, callback);
    }

    public void updateDescription(String userId, String description, String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("text", description);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + "/" + userId + SERVER_USER_DESCRIPTION).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void getCurrentGame(String steamToken, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.get(STEAM_API_BASE_URL + STEAM_API_STEAM_USER + STEAM_API_PLAYER_SUMMARIES + STEAM_API_KEY + "&steamids=" + steamToken);
        sendRequest(request, callback);
    }
}
