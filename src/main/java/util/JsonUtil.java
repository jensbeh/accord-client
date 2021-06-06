package util;

import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;

public class JsonUtil {
    public static JsonObject parse(JSONObject hereticalJsonObject) {
        return Json.createReader(new StringReader(hereticalJsonObject.toString())).readObject();
    }

    public static JsonObject parse(String json) {
        return Json.createReader(new StringReader(json)).readObject();
    }

}
