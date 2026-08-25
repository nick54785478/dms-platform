package com.example.validation.presentation.rest;

import com.example.validation.application.port.in.CreateValidationPolicyUseCase;
import com.example.validation.application.port.in.DeleteValidationPolicyUseCase;
import com.example.validation.application.port.in.GetValidationPolicyUseCase;
import com.example.validation.application.port.in.ListValidationPolicyUseCase;
import com.example.validation.application.port.in.UpdateValidationPolicyUseCase;
import com.example.validation.application.shared.command.CreateValidationPolicyCommand;
import com.example.validation.application.shared.command.DeleteValidationPolicyCommand;
import com.example.validation.application.shared.command.UpdateValidationPolicyCommand;
import com.example.validation.application.shared.dto.ValidationPolicyGottenResult;
import com.example.validation.application.shared.dto.ValidationPolicySearchedResult;
import com.example.validation.application.shared.query.GetValidationPolicyQuery;
import com.example.validation.application.shared.query.ListValidationPolicyQuery;
import com.example.validation.presentation.assembler.ValidationPolicyResourceAssembler;
import com.example.validation.presentation.resource.in.CreateValidationPolicyResource;
import com.example.validation.presentation.resource.in.UpdateValidationPolicyResource;
import com.example.validation.presentation.resource.out.ValidationPolicyCreatedResource;
import com.example.validation.presentation.resource.out.ValidationPolicyRetrievedResource;
import com.example.validation.presentation.resource.out.ValidationPolicySearchedResource;
import com.example.validation.presentation.resource.out.ValidationPolicyUpdatedResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;

@Tag(name = "Validation Policy API", description = "範本驗證規則管理 API")
@RestController
@RequestMapping("/validation-policies")
@RequiredArgsConstructor
public class ValidationPolicyController {

    private final CreateValidationPolicyUseCase createValidationPolicyUseCase;
    private final UpdateValidationPolicyUseCase updateValidationPolicyUseCase;
    private final DeleteValidationPolicyUseCase deleteValidationPolicyUseCase;
    private final GetValidationPolicyUseCase getValidationPolicyUseCase;
    private final ListValidationPolicyUseCase listValidationPolicyUseCase;
    private final ValidationPolicyResourceAssembler assembler;

    @Operation(summary = "建立驗證規則", description = "新增一筆新的範本驗證規則")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValidationPolicyCreatedResource create(@RequestBody CreateValidationPolicyResource resource) {
        CreateValidationPolicyCommand command = assembler.toCommand(resource);
        Long id = createValidationPolicyUseCase.create(command);
        return new ValidationPolicyCreatedResource(id);
    }

    @Operation(summary = "更新驗證規則", description = "修改既有的範本驗證規則")
    @PutMapping("/{id}")
    public ValidationPolicyUpdatedResource update(@PathVariable Long id, @RequestBody UpdateValidationPolicyResource resource) {
        UpdateValidationPolicyCommand command = assembler.toCommand(id, resource);
        updateValidationPolicyUseCase.update(command);
        return new ValidationPolicyUpdatedResource(true);
    }

    @Operation(summary = "取得單筆驗證規則", description = "透過 ID 查詢單筆驗證規則詳細資料")
    @GetMapping("/{id}")
    public ValidationPolicyRetrievedResource get(@PathVariable Long id) {
        GetValidationPolicyQuery query = new GetValidationPolicyQuery(id);
        ValidationPolicyGottenResult result = getValidationPolicyUseCase.get(query);
        return assembler.toResource(result);
    }

    @Operation(summary = "查詢驗證規則列表", description = "查詢驗證規則，可透過範本代碼過濾")
    @GetMapping
    public List<ValidationPolicySearchedResource> list(@RequestParam(required = false) String code) {
        ListValidationPolicyQuery query = new ListValidationPolicyQuery(code);
        List<ValidationPolicySearchedResult> results = listValidationPolicyUseCase.list(query);
        return results.stream()
                .map(assembler::toResource)
                .collect(Collectors.toList());
    }

    @Operation(summary = "刪除驗證規則", description = "透過 ID 刪除指定的驗證規則")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        DeleteValidationPolicyCommand command = new DeleteValidationPolicyCommand(id);
        deleteValidationPolicyUseCase.delete(command);
    }
}
