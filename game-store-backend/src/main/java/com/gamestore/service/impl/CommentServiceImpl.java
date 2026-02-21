package com.gamestore.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.Comment;
import com.gamestore.entity.Game;
import com.gamestore.mapper.CommentMapper;
import com.gamestore.mapper.GameMapper;
import com.gamestore.mapper.OrderMapper;
import com.gamestore.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private GameMapper gameMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Override
    public Page<Comment> getCommentPage(Integer page, Integer size, Long gameId, Long userId) {
        Page<Comment> pageInfo = new Page<>(page, size);
        return pageInfo.setRecords(commentMapper.selectCommentList(pageInfo, gameId, userId));
    }
    
    @Override
    @Transactional
    public boolean addComment(Comment comment) {
        // 检查用户是否已评论过该游戏
        if (hasUserCommented(comment.getUserId(), comment.getGameId())) {
            throw new RuntimeException("您已经评论过该游戏，请修改已有评论");
        }
        
        // 检查用户是否购买了该游戏
        if (!checkUserBoughtGame(comment.getUserId(), comment.getGameId())) {
            throw new RuntimeException("请先购买游戏后再进行评论");
        }
        
        // 调试日志：打印插入前的评分值
        System.out.println("[Service] 准备插入评论，评分值: " + comment.getRating());
        
        // 插入评论
        boolean success = commentMapper.insert(comment) > 0;
        
        // 调试日志：打印插入后的评分值
        System.out.println("[Service] 插入评论" + (success ? "成功" : "失败") + "，ID: " + comment.getId() + "，评分: " + comment.getRating());
        if (success && comment.getGameId() != null) {
            // 计算并更新游戏平均评分
            updateGameRating(comment.getGameId());
        }
        return success;
    }
    
    @Override
    @Transactional
    public boolean updateComment(Comment comment) {
        // 检查评论是否存在且属于当前用户
        Comment existing = commentMapper.selectById(comment.getId());
        if (existing == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!existing.getUserId().equals(comment.getUserId())) {
            throw new RuntimeException("无权修改他人评论");
        }
        
        // 调试日志：打印更新前的评分值
        System.out.println("[Service] 准备更新评论，ID: " + comment.getId() + "，新评分: " + comment.getRating());
        
        // 更新评论内容和评分
        boolean success = commentMapper.updateById(comment) > 0;
        
        // 调试日志：打印更新结果
        System.out.println("[Service] 更新评论" + (success ? "成功" : "失败") + "，ID: " + comment.getId());
        if (success && comment.getGameId() != null) {
            updateGameRating(comment.getGameId());
        }
        return success;
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long id, Long userId) {
        // 检查评论是否存在且属于当前用户
        Comment existing = commentMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人评论");
        }
        
        Long gameId = existing.getGameId();
        boolean success = commentMapper.deleteById(id) > 0;
        if (success && gameId != null) {
            updateGameRating(gameId);
        }
        return success;
    }
    
    /**
     * 检查用户是否购买了指定游戏
     */
    private boolean checkUserBoughtGame(Long userId, Long gameId) {
        return orderMapper.checkUserBoughtGame(userId, gameId) > 0;
    }
    
    @Override
    public boolean hasUserCommented(Long userId, Long gameId) {
        return commentMapper.checkUserCommented(userId, gameId) > 0;
    }
    
    @Override
    public Comment getUserComment(Long userId, Long gameId) {
        return commentMapper.getUserComment(userId, gameId);
    }
    
    private void updateGameRating(Long gameId) {
        try {
            Double avgRating = commentMapper.getAverageRating(gameId);
            // 如果没有评论，avgRating 为 null，此时应将评分设为 0
            double newRating = (avgRating != null) ? Math.round(avgRating * 10) / 10.0 : 0.0;
            gameMapper.updateRating(gameId, newRating);
        } catch (Exception e) {
            // 记录错误但不影响评论提交
            System.err.println("更新游戏评分失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Double getAverageRating(Long gameId) {
        return commentMapper.getAverageRating(gameId);
    }
}
