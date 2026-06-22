package javafx.auth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Utility class để handle REST API responses
 */
public class ApiResponse<T> {
    public int statusCode;
    public String message;
    public String error;
    public T data;

    /**
     * Parse JSON response string to ApiResponse object
     */
    public static <T> ApiResponse<T> fromJson(String json, Class<T> dataClass) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }

            Gson gson = new Gson();
            JsonElement rootElement = JsonParser.parseString(json);
            if (rootElement == null || rootElement.isJsonNull() || !rootElement.isJsonObject()) {
                return null;
            }

            JsonObject root = rootElement.getAsJsonObject();
            ApiResponse<T> result = new ApiResponse<>();
            result.statusCode = getInt(root, "statusCode");
            result.message = getText(root.get("message"), gson);
            result.error = getText(root.get("error"), gson);

            JsonElement data = root.get("data");
            if (data != null && !data.isJsonNull() && dataClass != Void.class) {
                result.data = gson.fromJson(data, dataClass);
            }

            return result;
        } catch (Exception e) {
            System.err.println("Failed to parse ApiResponse: " + e.getMessage());
            return null;
        }
    }

    private static int getInt(JsonObject object, String memberName) {
        JsonElement value = object.get(memberName);
        if (value == null || value.isJsonNull()) {
            return 0;
        }
        return value.getAsInt();
    }

    private static String getText(JsonElement value, Gson gson) {
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (value.isJsonPrimitive()) {
            return value.getAsString();
        }
        if (value.isJsonArray()) {
            return joinMessages(value.getAsJsonArray(), gson);
        }
        return gson.toJson(value);
    }

    private static String joinMessages(JsonArray messages, Gson gson) {
        StringBuilder builder = new StringBuilder();
        for (JsonElement message : messages) {
            String text = getText(message, gson);
            if (text == null || text.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(text);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    /**
     * Check if response is successful (status code 2xx)
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Check if response is error (status code 4xx or 5xx)
     */
    public boolean isError() {
        return statusCode >= 400;
    }

    /**
     * Get error message
     */
    public String getErrorMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        if (error != null && !error.isEmpty()) {
            return error;
        }
        return "Unknown error";
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}
