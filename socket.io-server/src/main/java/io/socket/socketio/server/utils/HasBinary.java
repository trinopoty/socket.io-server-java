package io.socket.socketio.server.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface HasBinary {

    Logger logger = Logger.getLogger(HasBinary.class.getName());

    static boolean hasBinary(Object data) {
        if (data == null) return false;

        if (data instanceof byte[]) {
            return true;
        }

        if (data instanceof JSONArray) {
            JSONArray _obj = (JSONArray)data;
            int length = _obj.length();
            for (int i = 0; i < length; i++) {
                Object v;
                try {
                    v = _obj.isNull(i) ? null : _obj.get(i);
                } catch (JSONException e) {
                    logger.log(Level.WARNING, "An error occured while retrieving data from JSONArray", e);
                    return false;
                }
                if (hasBinary(v)) {
                    return true;
                }
            }
        } else if (data instanceof JSONObject) {
            JSONObject _obj = (JSONObject)data;
            Iterator<String> keys = _obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object v;
                try {
                    v = _obj.get(key);
                } catch (JSONException e) {
                    logger.log(Level.WARNING, "An error occured while retrieving data from JSONObject", e);
                    return false;
                }
                if (hasBinary(v)) {
                    return true;
                }
            }
        }

        return false;
    }
}
