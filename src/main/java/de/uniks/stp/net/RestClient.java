package de.uniks.stp.net;

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

    public void getServerUsers(String serverId, String userKey, Callback<JsonNode> callback){
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + serverId;
        HttpRequest<?> postRequest = Unirest.get(url).header("userKey",userKey);
        sendRequest(postRequest, callback);
    }

    public JsonNode postServer(String userKey, String serverName) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", serverName);
        HttpResponse<JsonNode> response = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH).body(jsonBody).header("userKey",userKey).asJson();
        return response.getBody();
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }
}
