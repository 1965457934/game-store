package com.gamestore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gamestore.entity.Comment;

public interface CommentService extends IService<Comment> {
    
    Page<Comment> getCommentPage(Integer page, Integer size, Long gameId, Long userId);
    
    boolean addComment(Comment comment);
    
    boolean updateComment(Comment comment);
    
    boolean deleteComment(Long id, Long userId);
    
    boolean adminDeleteComment(Long id);
    
    Double getAverageRating(Long gameId);
    
    /**
     * 检查用户是否已评论过该游戏
     */
    boolean hasUserCommented(Long userId, Long gameId);
    
    /**
     * 获取用户对该游戏的评论
     */
    Comment getUserComment(Long userId, Long gameId);
}
