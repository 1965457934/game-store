package com.gamestore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gamestore.entity.Game;

import java.util.List;

public interface GameService extends IService<Game> {
    
    Game getGameDetail(Long id);
    
    Page<Game> getGamePage(Integer page, Integer size, String keyword, Long categoryId, String sortBy);
    
    Page<Game> getGamePageForAdmin(Integer page, Integer size, String keyword);
    
    List<Game> getTopGames(Integer limit);
    
    List<Game> getNewGames(Integer limit);
    
    List<Game> getRecommendGames(Long userId, Integer limit);
    
    boolean updateStock(Long gameId, Integer stock);
    
    boolean updateSales(Long gameId, Integer sales);
}
