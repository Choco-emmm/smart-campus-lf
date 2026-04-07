package com.choco.smartlf.controller;

import com.choco.smartlf.entity.Result;
import com.choco.smartlf.entity.dto.ItemPublishDTO;
import com.choco.smartlf.entity.dto.ItemReportDTO;
import com.choco.smartlf.entity.dto.ItemTopApplyDTO;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import com.choco.smartlf.entity.vo.ItemListVO;
import com.choco.smartlf.service.ItemInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "02. 失物招领模块", description = "处理物品的发布、查询、详情展示")
@RequiredArgsConstructor
@RestController
@RequestMapping("/item")
public class ItemController {

    private final ItemInfoService itemInfoService; // 这里用统一个门面 Service 管理三张表

    @Operation(summary = "发布失物/招领")
    @PostMapping("/publish")
    public Result<Void> publishItem(@Validated @RequestBody ItemPublishDTO dto) {
        itemInfoService.publishItem(dto);
        return Result.success();
    }

// TODO: 分页查询列表接口 (后续结合 MyBatis-Plus 分页插件实现)

//    @Operation(summary = "获取物品详情")
//    @GetMapping("/{id}")
//    public Result<ItemDetailVO> getItemDetail(@PathVariable Long id) {
//        ItemDetailVO vo = itemInfoService.getItemDetail(id);
//        return Result.success(vo);
//    }
//@Operation(summary = "举报物品信息")
//@PostMapping("/report")
//public Result<Void> reportItem(@Validated @RequestBody ItemReportDTO dto) {
//    // 交给独立的举报 Service 处理，符合单一职责原则
//    reportRecordService.submitReport(dto);
//    return Result.success();
//}
//
//    @Operation(summary = "申请置顶 (需管理员审核)")
//    @PostMapping("/top/apply")
//    public Result<Void> applyTop(@Validated @RequestBody ItemTopApplyDTO dto) {
//        itemInfoService.applyTop(dto);
//        return Result.success();
//    }
//
@Operation(summary = "上传失物/招领图片", description = "发布帖子前，先调用此接口上传图片换取URL")
@PostMapping("/image")
public Result<String> uploadItemImage(
        @Schema(description = "图片文件", required = true) @RequestParam("file") MultipartFile file) {

    // 调用 Service 层处理文件保存，并返回网络访问路径
    String imageUrl = itemInfoService.uploadImage(file);

    // 前端拿到这个 URL 后，把它塞进 ItemPublishDTO 的 imagesUrlList 数组里，最后随表单一起提交
    return Result.success(imageUrl);
}

}