package de.uniks.stp.net;

import javax.json.JsonStructure;
import javax.websocket.CloseReason;
import javax.websocket.Session;

public interface WSCallback {
    void handleMessage(JsonStructure msg);

    void onClose(Session session, CloseReason closeReason);
}