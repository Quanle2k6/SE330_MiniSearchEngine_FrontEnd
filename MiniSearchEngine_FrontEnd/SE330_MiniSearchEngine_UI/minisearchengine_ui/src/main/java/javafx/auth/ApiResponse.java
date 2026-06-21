package javafx.auth;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

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
            Gson gson = new Gson();
            // First parse as generic ApiResponse with JsonElement data
            ApiResponse<JsonElement> genericResponse = gson.fromJson(json, com.google.gson.reflect.TypeToken.getParameterized(ApiResponse.class, JsonElement.class).getType());
            
            if (genericResponse == null) {
                return null;
            }

            // Then convert data to specific type
            ApiResponse<T> result = new ApiResponse<>();
            result.statusCode = genericResponse.statusCode;
            result.message = genericResponse.message;
            result.error = genericResponse.error;
            
            if (genericResponse.data != null && !genericResponse.data.isJsonNull()) {
                result.data = gson.fromJson(genericResponse.data, dataClass);
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("Failed to parse ApiResponse: " + e.getMessage());
            return null;
        }
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
        if (error != null && !error.isEmpty()) {
            return error;
        }
        if (message != null && !message.isEmpty()) {
            return message;
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
