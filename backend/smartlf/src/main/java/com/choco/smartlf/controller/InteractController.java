package com.choco.smartlf.controller;

import com.choco.smartlf.entity.Result;
import com.choco.smartlf.entity.dto.CommentAddDTO;
import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.entity.vo.ItemCommentVO;
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

    @Operation(summary = "发送私信")
    @PostMapping("/message/send")
    public Result<Void> sendMessage(@Validated @RequestBody MessageSendDTO dto) {
        privateMessageService.sendMessage(dto);
        return Result.success();
    }

//    @Operation(summary = "获取与某人的聊天记录 (会自动将对方发给我的未读消息设为已读)")
//    @GetMapping("/message/history/{targetUserId}")
//    public Result<List<PrivateMessage>> getChatHistory(@PathVariable Long targetUserId) {
//        List<PrivateMessage> history = privateMessageService.getChatHistory(targetUserId);
//        return Result.success(history);
//    }
}