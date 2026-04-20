package com.gamestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamestore.entity.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface GameMapper extends BaseMapper<Game> {
    
    @Select("SELECT g.*, c.name as category_name FROM game g LEFT JOIN category c ON g.category_id = c.id WHERE g.id = #{id} AND g.deleted = 0 AND g.status = 1")
    Game getGameDetail(Long id);
    
    List<Game> selectGameList(Page<Game> page, 
                              @Param("keyword") String keyword, 
                              @Param("categoryId") Long categoryId,
                              @Param("sortBy") String sortBy);
    
    List<Game> selectGameListForAdmin(Page<Game> page, 
                                      @Param("keyword") String keyword);
    
    @Select("SELECT g.*, c.name as category_name FROM game g LEFT JOIN category c ON g.category_id = c.id WHERE g.deleted = 0 AND g.status = 1 ORDER BY g.sales DESC LIMIT #{limit}")
    List<Game> selectTopGames(Integer limit);
    
    @Select("SELECT g.*, c.name as category_name FROM game g LEFT JOIN category c ON g.category_id = c.id WHERE g.deleted = 0 AND g.status = 1 AND g.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY g.create_time DESC LIMIT #{limit}")
    List<Game> selectNewGames(Integer limit);
    
    /**
     * 获取用户已购买游戏的分类权重
     */
    @Select("SELECT g.category_id as categoryId, COUNT(*) as count " +
            "FROM order_info o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "JOIN game g ON oi.game_id = g.id " +
            "WHERE o.user_id = #{userId} AND o.status = 1 " +
            "GROUP BY g.category_id")
    List<Map<String, Object>> selectUserCategoryWeights(Long userId);
    
    /**
     * 获取候选推荐游戏（上架且未购买）
     */
    @Select("SELECT g.*, c.name as category_name " +
            "FROM game g " +
            "LEFT JOIN category c ON g.category_id = c.id " +
            "LEFT JOIN (SELECT DISTINCT oi.game_id FROM order_info o " +
            "           JOIN order_item oi ON o.id = oi.order_id " +
            "           WHERE o.user_id = #{userId} AND o.status = 1) bought ON g.id = bought.game_id " +
            "WHERE g.deleted = 0 AND g.status = 1 AND bought.game_id IS NULL")
    List<Game> selectRecommendCandidates(Long userId);
    
    @Update("UPDATE game SET rating = #{rating} WHERE id = #{gameId}")
    int updateRating(@Param("gameId") Long gameId, @Param("rating") Double rating);

    @Update("UPDATE game SET stock = stock - #{quantity}, sales = sales + #{quantity} " +
            "WHERE id = #{gameId} AND stock >= #{quantity}")
    int reserveStock(@Param("gameId") Long gameId, @Param("quantity") Integer quantity);

    @Update("UPDATE game SET stock = stock + #{quantity}, " +
            "sales = CASE WHEN sales >= #{quantity} THEN sales - #{quantity} ELSE 0 END " +
            "WHERE id = #{gameId}")
    int releaseStock(@Param("gameId") Long gameId, @Param("quantity") Integer quantity);
}
