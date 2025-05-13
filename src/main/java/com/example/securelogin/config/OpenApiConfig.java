package com.example.securelogin.config; // Or any appropriate package

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// 這個類別用於移除 OpenAPI 文件中預設的錯誤響應，只保留用戶定義的響應
@Configuration
public class OpenApiConfig {

    @Bean
    public OperationCustomizer removeDefaultErrorResponsesCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // 獲取此操作已有的響應 (可能包含預設的和用戶定義的)
            ApiResponses existingResponses = operation.getResponses();
            if (existingResponses == null) {
                existingResponses = new ApiResponses();
            }

            // 收集用戶在 Controller 方法上明確定義的響應碼
            Set<String> explicitlyDefinedCodes = new HashSet<>();
            io.swagger.v3.oas.annotations.responses.ApiResponses responsesAnnotation = handlerMethod
                    .getMethodAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponses.class);
            if (responsesAnnotation != null) {
                for (io.swagger.v3.oas.annotations.responses.ApiResponse apiResponse : responsesAnnotation.value()) {
                    explicitlyDefinedCodes.add(apiResponse.responseCode());
                }
            }
            // 檢查是否有單獨的 @ApiResponse 註解 (通常用於主要的成功響應)
            io.swagger.v3.oas.annotations.responses.ApiResponse singleResponseAnnotation = handlerMethod
                    .getMethodAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponse.class);
            if (singleResponseAnnotation != null) {
                explicitlyDefinedCodes.add(singleResponseAnnotation.responseCode());
            }

            // 創建一個新的 ApiResponses 物件，只包含我們想要的響應
            ApiResponses newApiResponses = new ApiResponses();

            for (Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse> entry : existingResponses
                    .entrySet()) {
                String responseCode = entry.getKey();
                io.swagger.v3.oas.models.responses.ApiResponse apiResponse = entry.getValue();

                // 保留條件：
                // 1. 該響應碼是使用者明確定義的。
                // 2. 該響應碼是 2xx 成功響應 (通常我們總是希望顯示成功響應)。
                if (explicitlyDefinedCodes.contains(responseCode) || responseCode.startsWith("2")) {
                    newApiResponses.addApiResponse(responseCode, apiResponse);
                }
                // 其他情況 (例如，未明確定義的 401, 404, 500 等) 將被過濾掉
            }

            // 如果新的響應列表為空，但原始列表有內容 (例如只有一個2xx響應但用戶未註解)，
            // 確保至少保留2xx響應。上面的邏輯已經處理了這一點。
            if (newApiResponses.isEmpty() && !existingResponses.isEmpty()
                    && existingResponses.keySet().stream().anyMatch(key -> key.startsWith("2"))) {
                existingResponses.forEach((key, value) -> {
                    if (key.startsWith("2")) {
                        newApiResponses.addApiResponse(key, value);
                    }
                });
            }

            operation.setResponses(newApiResponses);
            return operation;
        };
    }
}
