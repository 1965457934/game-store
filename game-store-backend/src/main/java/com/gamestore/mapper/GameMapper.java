package com.gamestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamestore.entity.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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
    
    @Select("SELECT g.*, c.name as category_name FROM game g LEFT JOIN category c ON g.category_id = c.id WHERE g.deleted = 0 AND g.status = 1 ORDER BY g.create_time DESC LIMIT #{limit}")
    List<Game> selectNewGames(Integer limit);
    
    @Update("UPDATE game SET rating = #{rating} WHERE id = #{gameId}")
    int updateRating(@Param("gameId") Long gameId, @Param("rating") Double rating);
}
