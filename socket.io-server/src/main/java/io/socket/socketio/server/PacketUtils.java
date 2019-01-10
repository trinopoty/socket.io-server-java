package io.socket.socketio.server;

import io.socket.parser.Packet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
final class PacketUtils {

    private static final Object[] EMPTY_ARGS = new Object[0];

    /**
     * Validate args and create packet.
     *
     * @param type Type of packet to create.
     * @param event Name of event.
     * @param args Data to set.
     * @return Created packet.
     * @throws IllegalArgumentException If args contain any invalid data type.
     */
    @SuppressWarnings("SameParameterValue")
    static Packet createDataPacket(int type, String event, Object[] args) throws IllegalArgumentException {
        if (args == null) {
            args = EMPTY_ARGS;
        }

        final JSONArray array = new JSONArray();
        if (event != null) {
            array.put(event);
        }
        for (Object arg : args) {
            array.put(arg);
        }
        if (!PacketUtils.isPacketDataValid(array)) {
            throw new IllegalArgumentException("args contain invalid data type.");
        }

        final Packet packet = new Packet();
        packet.type = type;
        packet.data = array;
        return packet;
    }

    @SuppressWarnings("Duplicates")
    private static boolean isPacketDataValid(JSONArray array) {
        try {
            for (int idx = 0; idx < array.length(); idx++) {
                final Object item = array.get(idx);

                if (!isPacketDataValidType(item)) {
                    return false;
                }
                if (item == null) {
                    array.put(idx, JSONObject.NULL);
                }
                if ((item instanceof JSONArray) && !isPacketDataValid((JSONArray)item)) {
                    return false;
                }
                if ((item instanceof JSONObject) && !isPacketDataValid((JSONObject)item)) {
                    return false;
                }
            }

            return true;
        } catch (JSONException ignore) {
        }
        return false;
    }

    @SuppressWarnings("Duplicates")
    private static boolean isPacketDataValid(JSONObject object) {
        try {
            final Iterator keys = object.keys();
            while (keys.hasNext()) {
                final Object keyObj = keys.next();
                if (!(keyObj instanceof String)) {
                    return false;
                }

                final String key = (String)keyObj;
                final Object item = object.get(key);

                if (!isPacketDataValidType(item)) {
                    return false;
                }
                if (item == null) {
                    object.put(key, JSONObject.NULL);
                }
                if ((item instanceof JSONArray) && !isPacketDataValid((JSONArray)item)) {
                    return false;
                }
                if ((item instanceof JSONObject) && !isPacketDataValid((JSONObject)item)) {
                    return false;
                }
            }

            return true;
        } catch (JSONException ignore) {
        }
        return false;
    }

    private static boolean isPacketDataValidType(Object object) {
        return ((object == null) ||
                (object == JSONObject.NULL) ||
                (object instanceof JSONObject) ||
                (object instanceof JSONArray) ||
                (object instanceof String) ||
                (object instanceof Integer) ||
                (object instanceof Long) ||
                (object instanceof Double) ||
                (object instanceof Boolean) ||
                (object instanceof byte[]));
    }
}