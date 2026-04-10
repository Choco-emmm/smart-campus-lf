package com.choco.smartlf.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.choco.smartlf.entity.Result;
import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.ReportRecord;
import com.choco.smartlf.entity.pojo.TopApplyRecord;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.AdminReportDetailVO;
import com.choco.smartlf.entity.vo.AdminStatsVO;
// 注意根据你的实际包路径导入对应的 Service
import com.choco.smartlf.entity.vo.AdminUserInfoVO;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.ReportRecordService;
import com.choco.smartlf.service.TopApplyRecordService;
import com.choco.smartlf.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
    // ================== 1. 平台数据看板 ==================

    @Operation(summary = "获取平台全局统计数据", description = "包含发帖量、找回率，以及指定时间段内的活跃人数等")
    @GetMapping("/stats/overview")
    public Result<AdminStatsVO> getStatsOverview(
            @Parameter(description = "开始时间 (格式: yyyy-MM-dd HH:mm:ss，不传默认统计近7天)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,

            @Parameter(description = "结束时间 (格式: yyyy-MM-dd HH:mm:ss，不传默认当前时间)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 传递给 Service 层处理
        AdminStatsVO stats = itemInfoService.getPlatformStats(startTime, endTime);
        return Result.success(stats);
    }

    // ================== 2. 举报审核 ==================
    @Operation(summary = "管理员查看帖子详情 ", description = "无视逻辑删除和下架状态，强制查看物品底表数据")
    @GetMapping("/item/detail/{itemId}")
    public Result<ItemDetailVO> getItemDetailByAdmin(@PathVariable Long itemId) {
        ItemDetailVO detail = itemInfoService.getItemDetailByAdmin(itemId);
        return Result.success(detail);
    }

    @Operation(summary = "分页查询举报列表", description = "支持按状态/举报理由筛选，按举报时间倒序排序")
    @PostMapping("/report/page")
    public Result<IPage<ReportRecord>> pageReport(@Validated @RequestBody AdminReportPageDTO dto) {
        IPage<ReportRecord> page = reportRecordService.pageQuery(dto);
        return Result.success(page);
    }

    @Operation(summary = "查看举报单详情", description = "包含举报人、理由及原帖跳转所需ID加举报人的ID和昵称")
    @GetMapping("/report/{reportId}")
    public Result<AdminReportDetailVO> getReportDetail(@Parameter(description = "举报单ID") @PathVariable Long reportId) {
        AdminReportDetailVO vo = reportRecordService.getReportDetail(reportId);
        return Result.success(vo);
    }

    @Operation(summary = "处理举报 (联动下架)", description = "核实举报属实后，会自动将帖子修改为违规下架状态，同时取消可能的置顶状态")
    @PutMapping("/report/resolve")
    public Result<Void> resolveReport(@Validated @RequestBody AdminReportResolveDTO dto) {
        reportRecordService.resolveReport(dto);
        return Result.success();
    }

    // ================== 3. 置顶审核 ==================

    @Operation(summary = "分页查询置顶申请列表")
    @PostMapping("/top-apply/page")
    public Result<IPage<TopApplyRecord>> pageTopApply(@Validated @RequestBody AdminTopPageDTO dto) {
        IPage<TopApplyRecord> page = topApplyRecordService.pageQuery(dto);
        return Result.success(page);
    }

    @Operation(summary = "审批置顶申请 (联动主表)", description = "同意后自动将 ItemInfo 设为置顶，并计算过期时间")
    @PutMapping("/top-apply/resolve")
    public Result<Void> resolveTopApply(@Validated @RequestBody AdminTopResolveDTO dto) {
        topApplyRecordService.resolveTopApply(dto);
        return Result.success();
    }

    // ================== 4. 紧急治理 (一键封禁) ==================

    @Operation(summary = "一键违规下架失物贴", description = "不走举报，管理员直接强制下架")
    @PutMapping("/item/ban/{itemId}")
    public Result<Void> banItem(@Parameter(description = "物品主键ID") @PathVariable @NotNull Long itemId) {
        itemInfoService.banItemByAdmin(itemId);
        return Result.success();
    }
    // ================== 5. 用户管理 ==================

    @Operation(summary = "查看用户详情 (管理员视角)", description = "获取用户基础信息，以及状态、违规次数、最后活跃时间等敏感数据")
    @GetMapping("/user/{userId}")
    public Result<AdminUserInfoVO> getUserDetail(@Parameter(description = "用户主键ID") @PathVariable Long userId) {
        AdminUserInfoVO vo = userService.getUserDetailByAdmin(userId);
        return Result.success(vo);
    }

    @Operation(summary = "分页查询用户列表", description = "支持按关键字搜索和按状态筛选")
    @PostMapping("/user/page")
    public Result<IPage<User>> pageUser(@Validated @RequestBody AdminUserPageDTO dto) {
        IPage<User> page = userService.pageQueryUserByAdmin(dto);
        return Result.success(page);
    }

    @Operation(summary = "封禁/解封用户账号", description = "列表页和详情页的封禁/解封按钮共用此接口，" +
            "封禁的同时通过userId把redis里的token设为'banned'并沿用TTL,在拦截器里要是看到这个就直接提示被封禁" +
            "并且管理员无法封禁管理员，管理员最多只能下架别的管理员的帖子（以防事态发酵的时候发帖的管理员不在）")
    @PutMapping("/user/status/{userId}")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户主键ID") @PathVariable @NotNull Long userId,
            @Parameter(description = "状态 (0:解封, 1:封禁)") @RequestParam @NotNull Integer status) {
        userService.updateUserStatusByAdmin(userId, status);
        return Result.success();
    }
}