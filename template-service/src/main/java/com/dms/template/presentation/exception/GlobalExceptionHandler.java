package com.dms.template.presentation.exception;

import com.dms.template.domain.template.exception.TemplateNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全域例外處理器
 */
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(String error, String message) {}

    /**
     * 處理領域邏輯驗證錯誤 (例如: 版號找不到、參數錯誤)
     * 回傳 HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Bad Request", ex.getMessage()));
    }

    /**
     * 處理狀態不合法的操作 (例如: 只能更新 DRAFT)
     * 回傳 HTTP 409 Conflict 或 400 Bad Request
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Conflict", ex.getMessage()));
    }

    /**
     * 處理資源找不到的情境 (例如: Template 不存在)
     * 回傳 HTTP 404 Not Found
     */
    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTemplateNotFoundException(TemplateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Not Found", ex.getMessage()));
    }
    
    /**
     * 處理所有未捕捉的例外 (兜底)
     * 回傳 HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("d:/error.log", true));
            ex.printStackTrace(pw);
            pw.close();
        } catch (Exception e) {}
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "系統發生未預期錯誤，請聯絡管理員"));
    }
}
