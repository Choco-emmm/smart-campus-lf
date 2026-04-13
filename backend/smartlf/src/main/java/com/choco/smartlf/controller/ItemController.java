package com.choco.smartlf.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.choco.smartlf.entity.Result;
import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import com.choco.smartlf.entity.vo.ItemListVO;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.ReportRecordService;
import com.choco.smartlf.service.TopApplyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "02. 失物招领模块", description = "处理物品的发布、查询、详情展示")
@RequiredArgsConstructor
@RestController
@RequestMapping("/item")
public class ItemController {

    private final ItemInfoService itemInfoService; // 这里用统一个门面 Service 管理三张表
    private final TopApplyRecordService topApplyRecordService;
    private final ReportRecordService reportRecordService;

    @Operation(summary = "发布失物/招领")
    @PostMapping("/publish")
    public Result<Long> publishItem(@Validated @RequestBody ItemPublishDTO dto) {
        // 让 service 返回插入后的主键ID
        Long newItemId = itemInfoService.publishItem(dto);
        return Result.success(newItemId);
    }


    @Operation(summary = "获取信息详情")
    @GetMapping("/{id}")
    public Result<ItemDetailVO> getItemDetail(@PathVariable Long id) {
        ItemDetailVO vo = itemInfoService.getItemDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "修改失物信息(仅限发帖人)")
    @PutMapping("/update")
    public Result<Void> updateItem(@Validated @RequestBody ItemUpdateDTO dto) {
        itemInfoService.updateItem(dto);
        return Result.success();
    }

    @Operation(summary = "删除失物信息 (仅限发帖人)")
    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(
            @Schema(description = "要删除的物品ID", required = true) @PathVariable Long id) {
        itemInfoService.deleteItem(id);
        return Result.success();
    }

    @Operation(summary = "申请置顶 (需管理员审核)")
    @PostMapping("/top/apply")
    public Result<Void> applyTop(@Validated @RequestBody ItemTopApplyDTO dto) {
        topApplyRecordService.applyTop(dto);
        return Result.success();
    }

    @Operation(summary = "修改失物信息状态 (联动取消置顶)")
    @PutMapping("/status")
    public Result<Void> updateStatus(@Validated @RequestBody ItemStatusUpdateDTO dto) {
        itemInfoService.updateStatus(dto);
        return Result.success();
    }

    @Operation(summary = "分页查询失物信息列表广场")
    @GetMapping("/page")
    public Result<IPage<ItemListVO>> getPage(ItemPageQueryDTO dto) {
        IPage<ItemListVO> pageResult = itemInfoService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @Operation(summary = "举报失物信息")
    @PostMapping("/report")
    public Result<Void> reportItem(@Validated @RequestBody ItemReportDTO dto) {
        // 交给独立的举报 Service 处理
        reportRecordService.submitReport(dto);
        return Result.success();
    }


    @Operation(summary = "上传失物/招领图片", description = "发布/修改帖子前，先调用此接口上传图片换取URL")
    @PostMapping("/image")
    public Result<String> uploadItemImage(
            @Schema(description = "图片文件", required = true) @RequestParam("file") MultipartFile file) {

        // 调用 Service 层处理文件保存，并返回网络访问路径
        String imageUrl = itemInfoService.uploadImage(file);

        // 前端拿到这个 URL 后，把它塞进 ItemPublishDTO 的 imagesUrlList 数组里，最后随表单一起提交
        return Result.success(imageUrl);
    }

    @Operation(summary = "分页查询-我的发布", description = "获取当前登录用户发布的所有帖子（包含违规下架的）")
    @GetMapping("/my-page")
    public Result<IPage<ItemListVO>> getMyPage(
            @Schema(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Schema(description = "每页记录数", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<ItemListVO> pageResult = itemInfoService.myPublishPage(pageNum, pageSize);
        return Result.success(pageResult);
    }
    @Operation(summary = "分页查询-他人的发布", description = "获取其他用户发布的所有帖子（不包含违规下架的）")
    @GetMapping("/others-page/{userId}")
    public Result<IPage<ItemListVO>> getOthersPage(
            @Schema(description = "目标用户ID", required = true) @PathVariable Long userId,
            @Schema(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Schema(description = "每页记录数", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<ItemListVO> pageResult = itemInfoService.othersPublishPage(userId, pageNum, pageSize);
        return Result.success(pageResult);
    }
    @Operation(summary = "AI 一键生成/润色物品描述", description = "多次调用会覆盖上一次的AI生成结果")
    @PostMapping("/ai/generate-desc/{itemId}")
    public Result<String> generateAiDesc(@PathVariable Long itemId) {
        itemInfoService.generateAIDesc(itemId);
        return Result.success("AI 润色任务已提交后台处理。生成完成后系统将发送通知提示您！");
    }

    @Operation(summary = "获取修改帖子时的回显数据", description = "包含敏感的暗号数据，仅限发帖人调用")
    @GetMapping("/edit-echo/{id}")
    public Result<ItemUpdateDTO> getEditEcho(@PathVariable Long id) {
        ItemUpdateDTO dto = itemInfoService.getEditEcho(id);
        return Result.success(dto);
    }

}