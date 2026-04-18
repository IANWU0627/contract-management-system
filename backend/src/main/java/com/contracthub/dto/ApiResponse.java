package com.contracthub.dto;

public class ApiResponse<T> {
    private int code = 200;
    private String message = "success";
    /** Optional i18n key for clients (e.g. vue-i18n). When set, UI may prefer this over {@link #message}. */
    private String messageKey;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setData(data);
        return r;
    }
    
    public static <T> ApiResponse<T> error(String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(500);
        r.setMessage(msg);
        return r;
    }

    /**
     * Error with optional i18n key. {@code message} remains human-readable (often Chinese for legacy clients).
     */
    public static <T> ApiResponse<T> error(String msg, String messageKey) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(500);
        r.setMessage(msg);
        r.setMessageKey(messageKey);
        return r;
    }
    
    public static <T> ApiResponse<T> error(int code, String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    /** HTTP-style error code with i18n key (e.g. 400/403/404). */
    public static <T> ApiResponse<T> error(int code, String msg, String messageKey) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(code);
        r.setMessage(msg);
        r.setMessageKey(messageKey);
        return r;
    }
    
    public static <T> ApiResponse<T> error(String msg, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(400);
        r.setMessage(msg);
        r.setData(data);
        return r;
    }

    /** Validation / field errors with i18n key for the summary message. */
    public static <T> ApiResponse<T> error(String msg, String messageKey, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(400);
        r.setMessage(msg);
        r.setMessageKey(messageKey);
        r.setData(data);
        return r;
    }

    /** Error with HTTP status, message key, and structured data (e.g. i18n interpolation). */
    public static <T> ApiResponse<T> error(int code, String msg, String messageKey, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(code);
        r.setMessage(msg);
        r.setMessageKey(messageKey);
        r.setData(data);
        return r;
    }
    
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMessageKey() { return messageKey; }
    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
