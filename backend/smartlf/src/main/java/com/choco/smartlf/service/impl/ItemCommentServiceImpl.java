package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.CommentAddDTO;
import com.choco.smartlf.entity.pojo.ItemComment;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.ItemCommentVO;
import com.choco.smartlf.entity.vo.ItemDetailVO;
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
        //查帖主，将帖主id保存到数据库中（就算帖主注销也能继续留言，只要帖子还存在的话）
        comment.setTargetUserId(itemDetail.getUserId());
        comment.setItemId(dto.getItemId());// 物品ID
        comment.setUserId(UserContext.getUserId()); // 当前登录用户
        comment.setContent(dto.getContent());
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
        log.info("查询到{}条留言！", list.size());
        return list;
    }
}




