package com.choco.smartlf.service;

import com.choco.smartlf.entity.dto.CommentAddDTO;
import com.choco.smartlf.entity.pojo.ItemComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.vo.ItemCommentNotificationVO;
import com.choco.smartlf.entity.vo.ItemCommentVO;

import java.util.List;

/**
* @author renpe
* @description 针对表【item_comment(失物信息留言表)】的数据库操作Service
* @createDate 2026-04-10 12:55:01
*/
public interface ItemCommentService extends IService<ItemComment> {

    void addComment(CommentAddDTO dto);

    List<ItemCommentVO> getCommentsByItemId(Long itemId);

    List<ItemCommentNotificationVO> getCommentNotifications();
}
