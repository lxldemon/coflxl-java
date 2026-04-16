package com.coflxl.api.admin.controller.system;

import com.coflxl.api.admin.dto.CodeGenRequest;
import com.coflxl.api.admin.service.CodeGenService;
import com.coflxl.api.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/codegen")
public class AdminCodeGenController {

    @Autowired
    private CodeGenService codeGenService;

    @PostMapping("/generate")
    public ApiResponse<Map<String, String>> generate(@RequestBody CodeGenRequest request) {
        return ApiResponse.success(codeGenService.generateCode(request));
    }
}
