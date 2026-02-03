package com.april.rag.exception;

import org.springframework.http.HttpStatus;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/8 16:20
 * Description: 自定义异常
 */

public class CustomException extends RuntimeException{
    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
