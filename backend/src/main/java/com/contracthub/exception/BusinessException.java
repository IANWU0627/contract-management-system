package com.contracthub.exception;

import java.util.Map;

public class BusinessException extends RuntimeException {
    private final String messageKey;
    private final Map<String, Object> data;

    public BusinessException(String message) {
        super(message);
        this.messageKey = null;
        this.data = null;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.messageKey = null;
        this.data = null;
    }

    public BusinessException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
        this.data = null;
    }

    public BusinessException(String message, String messageKey, Map<String, Object> data) {
        super(message);
        this.messageKey = messageKey;
        this.data = data;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
