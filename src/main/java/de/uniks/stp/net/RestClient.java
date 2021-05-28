package de.uniks.stp.net;

import de.uniks.stp.builder.ModelBuilder;
import kong.unirest.*;
import org.json.JSONObject;

import static util.Constants.*;

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

    public JsonNode postServer(String userKey, String serverName) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", serverName);
        HttpResponse<JsonNode> response = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH).body(jsonBody).header("userKey", userKey).asJson();
        return response.getBody();
    }

    public void updateChannel(String serverId, String categoryId, String channelId, String userKey, String channelName, boolean privilege, String[] Members, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name", channelName).accumulate("privileged", privilege).accumulate("members", Members);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + "/" + serverId + SERVER_CATEGORIES_PATH + "/" + categoryId + SERVER_CHANNELS_PATH + "/" + channelId).body(body).header("userKey", userKey);
        sendRequest(request, callback);
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }

    public void createTempLink(String type, Integer max,String serverid,String userKey, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("type", type);
        if(type.equals("count")){
            jsonObj.accumulate("max",max);
        }
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH+"/"+serverid+ SERVER_INVITES).header("userKey", userKey).body(body);
        sendRequest(request, callback);
    }
}
