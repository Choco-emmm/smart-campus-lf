package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.CommentAddDTO;
import com.choco.smartlf.entity.pojo.ItemComment;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.ItemCommentNotificationVO;
import com.choco.smartlf.entity.vo.ItemCommentVO;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import com.choco.smartlf.enums.ReadStatusEnum;
import com.choco.smartlf.service.ItemCommentService;
import com.choco.smartlf.mapper.ItemCommentMapper;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author renpe
* @description 针对表【item_comment(失物信息留言表)】的数据库操作Service实现
* @createDate 2026-04-10 12:55:01
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class ItemCommentServiceImpl extends ServiceImpl<ItemCommentMapper, ItemComment>
    implements ItemCommentService{
    private final UserService userService;

    private final ItemInfoService itemInfoService;

    @Override
    public void addComment(CommentAddDTO dto) {
        ItemComment comment = new ItemComment();
        ItemDetailVO itemDetail = itemInfoService.getItemDetail(dto.getItemId());
        //帖子被逻辑删除了就查不到了
        if(!itemDetail.getId().equals(dto.getItemId())){
            throw new RuntimeException("失物信息不存在！");
        }
        //查帖主，将帖主id保存中（就算帖主注销也能继续留言，只要帖子还存在的话）
        comment.setTargetUserId(itemDetail.getUserId());
        comment.setItemId(dto.getItemId());// 物品ID
        comment.setUserId(UserContext.getUserId()); // 当前登录用户
        comment.setContent(dto.getContent());
        //如果帖主id和当前用户id相同，则更新为已读
        if(comment.getTargetUserId().equals(UserContext.getUserId())){
            comment.setIsRead(ReadStatusEnum.READ.getCode());
        }
        this.save(comment);
    }

    @Override
    public List<ItemCommentVO> getCommentsByItemId(Long itemId) {
       List<ItemCommentVO> list = new ArrayList<>();
       //通过帖子id把所有留言信息查出来
        List<ItemComment> comments = this.list(new LambdaQueryWrapper<ItemComment>()
                .eq(ItemComment::getItemId, itemId)
                .orderByAsc(ItemComment::getCreateTime));
        if(comments.isEmpty()){
            log.info("没有该帖子的留言！");
            return list;
        }
        //循环遍历，把每个评论信息转换成VO
        for (ItemComment comment : comments) {
            ItemCommentVO vo = new ItemCommentVO();
            //拷贝一下
            BeanUtil.copyProperties(comment, vo);
            //查出留言人
            User user = userService.getById(comment.getUserId());
            //设置留言人昵称和留言人头像
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            list.add(vo);
        }
        //通过itemId去查发帖人id，如果与当前用户的id相同，更新帖子下所有的留言为已读
        if(itemInfoService.getById(itemId).getUserId().equals(UserContext.getUserId())){
            this.update(new LambdaUpdateWrapper<ItemComment>()
                    .set(ItemComment::getIsRead, ReadStatusEnum.READ.getCode())        // 将 is_read 字段设置为 1 (已读)
                    .eq(ItemComment::getItemId, itemId)    // 条件 1：属于这个帖子
                    .eq(ItemComment::getIsRead, ReadStatusEnum.UNREAD.getCode())         // 条件 2：只更新那些目前是未读(0)的记录
            );
            log.info("将当前帖子的留言更新为已读！");
        }
        log.info("查询到{}条留言！", list.size());
        return list;
    }

    @Override
    public List<ItemCommentNotificationVO> getCommentNotifications() {
        Long myId = UserContext.getUserId();

        // 1. 查出所有发给我的未读留言 (is_read = 0)
        List<ItemComment> unreadComments = this.list(new LambdaQueryWrapper<ItemComment>()
                .eq(ItemComment::getTargetUserId, myId)
                .eq(ItemComment::getIsRead, ReadStatusEnum.UNREAD.getCode())
                .orderByDesc(ItemComment::getCreateTime));

        if (CollUtil.isEmpty(unreadComments)) {
            return new ArrayList<>();
        }

        // 2. 按照 itemId 进行分组。 Map<帖子ID, 该帖子的所有未读留言列表>
        Map<Long, List<ItemComment>> groupMap = unreadComments.stream()
                .collect(Collectors.groupingBy(ItemComment::getItemId));

        // 3. 组装 VO
        return groupMap.entrySet().stream().map(entry -> {
            Long itemId = entry.getKey();
            List<ItemComment> itemUnreadList = entry.getValue();

            ItemCommentNotificationVO vo = new ItemCommentNotificationVO();
            vo.setItemId(itemId);
            vo.setUnreadCount(itemUnreadList.size());
            // 取第一条作为最新留言摘要
            vo.setLastCommentContent(itemUnreadList.getFirst().getContent());

            // 查一下帖子的标题
            ItemInfo item = itemInfoService.getById(itemId);
            if (item != null) {
                vo.setItemTitle(item.getItemName());
            } else {
                vo.setItemTitle("已删除的帖子");
            }

            return vo;
        }).toList();
    }
}




