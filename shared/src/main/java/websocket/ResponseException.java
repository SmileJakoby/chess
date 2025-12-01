package websocket;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ResponseException extends Exception {

    public enum Code {
        ServerError,
        ClientError,
    }

    final private Code code;

    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }
    public static ResponseException fromJson(String json) {
        var map = new Gson().fromJson(json, HashMap.class);
        var status = Code.valueOf(map.get("status").toString());
        String message = map.get("message").toString();
        return new ResponseException(status, message);
    }
}