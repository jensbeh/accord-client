package util;

import org.json.JSONObject;

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    public static JsonObject parse(JSONObject hereticalJsonObject) {
        return Json.createReader(new StringReader(hereticalJsonObject.toString())).readObject();
    }

    public static JsonObject parse(String json) {
        return Json.createReader(new StringReader(json)).readObject();
    }

}
