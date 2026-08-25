package com.example.validation.presentation.aspect;

import com.example.validation.application.port.out.ExcelValidatorPort;
import com.example.validation.application.shared.command.UploadTemplateCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 針對 upload 方法的攔截切面，會在此進行客製驗證
 */
@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class CustomValidationAspect {

	private ExcelValidatorPort excelValidatorAdapter;

	/**
	 * 定義切入點，針對 UploadTemplateUseCase 的 upload 方法進行切入。
	 */
	@Pointcut("execution(* com.example.validation.application.port.in.UploadTemplateUseCase.upload(..))")
	public void pointCut() {

	}

	/**
	 * 執行客製驗證
	 *
	 * @param joinPoint 切入點
	 * @return 方法執行結果
	 * @throws Throwable 例外
	 */
	@Around("pointCut()")
	public Object validateExcelData(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		UploadTemplateCommand command = (UploadTemplateCommand) args[0];

		// 執行客製驗證
		if (command.fileContent() != null) {
			excelValidatorAdapter.validateExcelData(command.name(), command.fileContent());
		}

		// 執行後續流程
		return joinPoint.proceed();
	}
}
