package com.gamestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamestore.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    
    @Select("SELECT c.*, g.name as game_name, g.cover as game_cover FROM cart c LEFT JOIN game g ON c.game_id = g.id WHERE c.user_id = #{userId}")
    List<Cart> selectCartList(Long userId);
    
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND game_id = #{gameId}")
    Cart selectByUserIdAndGameId(Long userId, Long gameId);
}
