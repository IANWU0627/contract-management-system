package com.contracthub.dto;

public class ApiResponse<T> {
    private int code = 200;
    private String message = "success";
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
    
    public static <T> ApiResponse<T> error(int code, String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }
    
    public static <T> ApiResponse<T> error(String msg, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(400);
        r.setMessage(msg);
        r.setData(data);
        return r;
    }
    
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
