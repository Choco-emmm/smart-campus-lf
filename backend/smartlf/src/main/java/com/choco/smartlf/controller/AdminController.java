package com.choco.smartlf.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.choco.smartlf.entity.Result;
import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.ReportRecord;
import com.choco.smartlf.entity.pojo.TopApplyRecord;
import com.choco.smartlf.entity.vo.AdminStatsVO;
// 注意根据你的实际包路径导入对应的 Service
import com.choco.smartlf.entity.vo.AdminUserInfoVO;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.ReportRecordService;
import com.choco.smartlf.service.TopApplyRecordService;
import com.choco.smartlf.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "03. 管理员模块", description = "仅管理员可访问的全局控制接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ReportRecordService reportRecordService;
    private final TopApplyRecordService topApplyRecordService;
    private final ItemInfoService itemInfoService;
    private final UserService userService;
//
//    // ================== 1. 平台数据看板 ==================
//
//    @Operation(summary = "获取平台全局统计数据", description = "包含发帖量、找回率、活跃人数等")
//    @GetMapping("/stats/overview")
//    public Result<AdminStatsVO> getStatsOverview() {
//        // TODO: 明天在 itemInfoService 里写个 getPlatformStats() 方法
//        AdminStatsVO stats = itemInfoService.getPlatformStats();
//        return Result.success(stats);
//    }
//
//    // ================== 2. 举报审核闭环 ==================
//
//    @Operation(summary = "分页查询举报列表")
//    @PostMapping("/report/page")
//    public Result<IPage<ReportRecord>> pageReport(@Validated @RequestBody AdminReportPageDTO dto) {
//        // TODO: 明天在 reportRecordService 实现按状态分页
//        IPage<ReportRecord> page = reportRecordService.pageQuery(dto);
//        return Result.success(page);
//    }
//
//    @Operation(summary = "处理举报 (联动下架)", description = "核实举报属实后，会自动将帖子修改为违规下架状态")
//    @PutMapping("/report/resolve")
//    public Result<Void> resolveReport(@Validated @RequestBody AdminReportResolveDTO dto) {
//        // TODO: 明天在 reportRecordService 写一个带有 @Transactional 的方法处理状态联动
//        reportRecordService.resolveReport(dto);
//        return Result.success();
//    }
//
//    // ================== 3. 置顶审核闭环 ==================
//
//    @Operation(summary = "分页查询置顶申请列表")
//    @PostMapping("/top-apply/page")
//    public Result<IPage<TopApplyRecord>> pageTopApply(@Validated @RequestBody AdminTopPageDTO dto) {
//        // TODO: 明天在 topApplyRecordService 实现按状态分页
//        IPage<TopApplyRecord> page = topApplyRecordService.pageQuery(dto);
//        return Result.success(page);
//    }
//
//    @Operation(summary = "审批置顶申请 (联动主表)", description = "同意后自动将 ItemInfo 设为置顶，并计算过期时间")
//    @PutMapping("/top-apply/resolve")
//    public Result<Void> resolveTopApply(@Validated @RequestBody AdminTopResolveDTO dto) {
//        // TODO: 明天在 topApplyRecordService 写一个带有 @Transactional 的方法处理置顶联动
//        topApplyRecordService.resolveTopApply(dto);
//        return Result.success();
//    }
//
//    // ================== 4. 紧急治理 (一键封禁) ==================
//
//    @Operation(summary = "一键违规下架失物贴", description = "不走举报，管理员直接强制下架")
//    @PutMapping("/item/ban/{itemId}")
//    public Result<Void> banItem(@Parameter(description = "物品主键ID") @PathVariable Long itemId) {
//        // TODO: 直接把 item_info 的 status 改为 3
//        itemInfoService.banItemByAdmin(itemId);
//        return Result.success();
//    }
//
//    @Operation(summary = "封禁/解封用户账号", description = "封禁后该用户无法发帖/登录")
//    @PutMapping("/user/status/{userId}")
//    public Result<Void> updateUserStatus(
//            @Parameter(description = "用户主键ID") @PathVariable Long userId,
//            @Parameter(description = "状态 (0:正常, 1:封禁)") @RequestParam Integer status) {
//        // TODO: 修改 user 表的 status 字段
//        userService.updateStatusByAdmin(userId, status);
//        return Result.success();
//    }

    // ================== 5. 用户管理 ==================

//    @Operation(summary = "查看用户详情 (管理员视角)", description = "获取用户基础信息，以及状态、违规次数、最后活跃时间等敏感数据")
//    @GetMapping("/user/{userId}")
//    public Result<AdminUserInfoVO> getUserDetail(@Parameter(description = "用户主键ID") @PathVariable Long userId) {
//        // TODO: 明天在 userService 里实现这个聚合查询方法
//        AdminUserInfoVO vo = userService.getUserDetailByAdmin(userId);
//        return Result.success(vo);
//    }

//    @Operation(summary = "分页查询用户列表", description = "支持按关键字搜索和按状态筛选")
//    @PostMapping("/user/page")
//    public Result<IPage<User>> pageUser(@Validated @RequestBody AdminUserPageDTO dto) {
//        // TODO: 明天在 userService 里用 MyBatis-Plus 的 LambdaQueryWrapper 写个带 like 和 eq 的条件查询
//        IPage<User> page = userService.pageQueryUser(dto);
//        return Result.success(page);
//    }
//
//    @Operation(summary = "查看用户详情", description = "点击列表详情按钮时调用，获取包含违规次数等敏感数据")
//    @GetMapping("/user/{userId}")
//    public Result<AdminUserInfoVO> getUserDetail(@Parameter(description = "用户主键ID") @PathVariable Long userId) {
//        AdminUserInfoVO vo = userService.getUserDetailByAdmin(userId);
//        return Result.success(vo);
//    }
//
//    @Operation(summary = "封禁/解封用户账号", description = "列表页和详情页的封禁/解封按钮共用此接口")
//    @PutMapping("/user/status/{userId}")
//    public Result<Void> updateUserStatus(
//            @Parameter(description = "用户主键ID") @PathVariable Long userId,
//            @Parameter(description = "状态 (0:正常, 1:封禁)") @RequestParam Integer status) {
//        // TODO: update user set status = #{status} where id = #{userId}
//        userService.updateStatusByAdmin(userId, status);
//        return Result.success();
//    }
}