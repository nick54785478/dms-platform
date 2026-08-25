package com.example.validation.presentation.exception;

import com.example.validation.infrastructure.cv.shared.ValidateErrorProperty;
import com.example.validation.infrastructure.exception.ExcelValidationException;
import com.example.validation.infrastructure.exception.ValidationException;
import com.example.validation.infrastructure.util.BaseDataTransformer;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全域例外處理器
 */
@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<BaseExceptionResponse> handleValidationException(ValidationException e) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseDataTransformer.transformData(e, BaseExceptionResponse.class));
	}

	@ResponseBody
	@ExceptionHandler(ExcelValidationException.class)
	public ResponseEntity<BaseExceptionsResponse> handleExcelValidationException(
			final ExcelValidationException e) {
		List<String> errMessageList = e.getVepList().stream().map(ValidateErrorProperty::getMessage)
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK)
				.body(new BaseExceptionsResponse("VALIDATE_FAILED", errMessageList));
	}

	/**
	 * 回傳訊息定義
	 * */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BaseExceptionResponse {

		private String code;

		private String message;

	}

	/**
	 * 回傳訊息定義
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BaseExceptionsResponse {

		private String code;

		private List<String> messages;

	}

}
