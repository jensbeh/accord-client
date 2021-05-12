package de.uniks.stp.net;

import javax.json.JsonStructure;

public interface WSCallback {
    void handleMessage(JsonStructure msg);
}