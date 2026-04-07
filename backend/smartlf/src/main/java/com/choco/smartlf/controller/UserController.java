package com.choco.smartlf.controller;

import cn.hutool.core.util.StrUtil;
import com.choco.smartlf.entity.dto.UserLoginDTO;
import com.choco.smartlf.entity.dto.UserRegisterDTO;
import com.choco.smartlf.entity.Result;
import com.choco.smartlf.entity.dto.UserUpdateDTO;
import com.choco.smartlf.entity.vo.UserInfoVO;
import com.choco.smartlf.entity.vo.UserLoginVO;
import com.choco.smartlf.enums.CheckType;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@Tag(name = "01. 用户模块", description = "负责用户的注册、登录与个人信息管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "通用唯一性校验 ，供输入失焦后校验")
    @GetMapping("/isExist")
    public Result<Boolean> checkUnique(
            @Schema(description = "校验类型: USERNAME/PHONE/EMAIL", required = true) @RequestParam CheckType type,
            @Schema(description = "要校验的值", required = true) @RequestParam String value) {
        // 1. 参数非空校验
        if (StrUtil.isBlank(value)) {
            throw new BusinessException(SC_BAD_REQUEST, "校验参数不能为空");
        }
        // 2. 调用 Service 层进行查询看是否已有value这个字段
        boolean isRegistered = userService.isExist(type, value);
        // 3. 返回结果: true 代表已被注册，false 代表未被注册(可用)
        return Result.success(isRegistered);
    }

    @Operation(summary = "用户注册", description = "管理员注册需提供密钥")
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Validated @RequestBody UserLoginDTO dto) {
        UserLoginVO vo = userService.login(dto);
        return Result.success(vo);
    }

    @Operation(summary = "查看个人信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        // 从 ThreadLocal 中获取当前登录用户的 ID
        Long userId = UserContext.getUserId();
        UserInfoVO vo = userService.getUserInfo(userId);
        return Result.success(vo);
    }

    @Operation(summary = "修改基本信息", description = "支持修改昵称、邮箱、手机号")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@Validated @RequestBody UserUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        userService.updateUserInfo(userId, dto);
        return Result.success();
    }

}