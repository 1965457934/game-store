package com.gamestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamestore.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    List<Comment> selectCommentList(Page<Comment> page, 
                                    @Param("gameId") Long gameId, 
                                    @Param("userId") Long userId);
    
    @Select("SELECT AVG(rating) FROM comment WHERE game_id = #{gameId}")
    Double getAverageRating(Long gameId);
    
    /**
     * 检查用户是否已评论过该游戏
     */
    @Select("SELECT COUNT(*) FROM comment WHERE user_id = #{userId} AND game_id = #{gameId}")
    int checkUserCommented(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    /**
     * 获取用户对该游戏的评论
     */
    @Select("SELECT * FROM comment WHERE user_id = #{userId} AND game_id = #{gameId} LIMIT 1")
    Comment getUserComment(@Param("userId") Long userId, @Param("gameId") Long gameId);
}
