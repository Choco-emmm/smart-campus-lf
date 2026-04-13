package com.choco.smartlf.controller;

import com.choco.smartlf.entity.Result;
import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.entity.vo.ChatSessionVO;
import com.choco.smartlf.entity.vo.ClaimRecordVO;
import com.choco.smartlf.entity.vo.ItemCommentNotificationVO;
import com.choco.smartlf.entity.vo.ItemCommentVO;
import com.choco.smartlf.service.ClaimRecordService;
import com.choco.smartlf.service.ItemCommentService;
import com.choco.smartlf.service.PrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. 互动联系模块 (留言与私聊)")
@RestController
@RequestMapping("/interact")
@RequiredArgsConstructor
public class InteractController {

    private final ItemCommentService itemCommentService;
    private final PrivateMessageService privateMessageService;
    private final ClaimRecordService claimRecordService;

    @Operation(summary = "发表留言")
    @PostMapping("/comment/add")
    public Result<Void> addComment(@Validated @RequestBody CommentAddDTO dto) {
        itemCommentService.addComment(dto);
        return Result.success();
    }

    @Operation(summary = "查看某帖子的所有留言")
    @GetMapping("/comment/list/{itemId}")
    public Result<List<ItemCommentVO>> getComments(@PathVariable Long itemId) {
        List<ItemCommentVO> list = itemCommentService.getCommentsByItemId(itemId);
        return Result.success(list);
    }



    @Operation(summary = "获取与某人的聊天记录 (会自动将对方发给我的未读消息设为已读)")
    @GetMapping("/message/history/{targetUserId}")
    public Result<List<PrivateMessage>> getChatHistory(@PathVariable Long targetUserId) {
        List<PrivateMessage> history = privateMessageService.getChatHistory(targetUserId);
        return Result.success(history);
    }

    @Operation(summary = "获取消息页的会话列表")
    @GetMapping("/message/sessions")
    public Result<List<ChatSessionVO>> getSessionList() {
        List<ChatSessionVO> list = privateMessageService.getSessionList();
        return Result.success(list);
    }

    @Operation(summary = "获取留言提醒列表", description = "返回哪些帖子有未读留言及其数量")
    @GetMapping("/comment/notifications")
    public Result<List<ItemCommentNotificationVO>> getCommentNotifications() {
        List<ItemCommentNotificationVO> list = itemCommentService.getCommentNotifications();
        return Result.success(list);
    }
    @Operation(summary = "获取私信提醒", description = "返回未读私信总数")
    @GetMapping("/message/notifications")
    public Result<Integer> getPrivateMessageNotifications() {
        Integer unreadCount = privateMessageService.getPrivateMessageNotifications();
        return Result.success(unreadCount);
    }
    // ================= 认领申请功能 =================
    @Operation(summary = "提交认领申请")
    @PostMapping("/claim/submit")
    public Result<Void> submitClaim(@Validated @RequestBody ClaimSubmitDTO dto) {
        claimRecordService.submitClaim(dto);
        return Result.success();
    }
    // src/main/java/com/choco/smartlf/controller/InteractController.java

    @Operation(summary = "认领者提交补充证据")
    @PostMapping("/claim/supplement")
    public Result<Void> supplementClaim(@Validated @RequestBody ClaimSupplementDTO dto) {
        claimRecordService.supplementClaim(dto);
        return Result.success();
    }

//    @Operation(summary = "审核认领申请")
//    @PostMapping("/claim/audit")
//    public Result<Void> auditClaim(@Validated @RequestBody ClaimAuditDTO dto) {
//        claimRecordService.auditClaim(dto);
//        return Result.success("审核操作成功");
//    }
//
//    @Operation(summary = "我收到的认领申请")
//    @GetMapping("/claim/received")
//    public Result<List<ClaimRecordVO>> getMyReceivedClaims() {
//        return Result.success(claimRecordService.getMyReceivedClaims());
//    }
//
//    @Operation(summary = "我发出的认领申请")
//    @GetMapping("/claim/submitted")
//    public Result<List<ClaimRecordVO>> getMySubmittedClaims() {
//        return Result.success(claimRecordService.getMySubmittedClaims());
//    }
}