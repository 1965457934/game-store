package com.gamestore.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.Game;
import com.gamestore.mapper.CommentMapper;
import com.gamestore.mapper.GameMapper;
import com.gamestore.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl extends ServiceImpl<GameMapper, Game> implements GameService {
    
    @Autowired
    private GameMapper gameMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Override
    public Game getGameDetail(Long id) {
        Game game = gameMapper.getGameDetail(id);
        // 如果评分字段为空，实时计算评分
        if (game != null && (game.getRating() == null || game.getRating() == 0)) {
            Double avgRating = commentMapper.getAverageRating(id);
            if (avgRating != null) {
                game.setRating(Math.round(avgRating * 10) / 10.0);
            }
        }
        return game;
    }
    
    @Override
    public Page<Game> getGamePage(Integer page, Integer size, String keyword, Long categoryId, String sortBy) {
        Page<Game> pageInfo = new Page<>(page, size);
        List<Game> games = gameMapper.selectGameList(pageInfo, keyword, categoryId, sortBy);
        // 填充评分
        games.forEach(this::fillRating);
        return pageInfo.setRecords(games);
    }
    
    @Override
    public Page<Game> getGamePageForAdmin(Integer page, Integer size, String keyword) {
        Page<Game> pageInfo = new Page<>(page, size);
        List<Game> games = gameMapper.selectGameListForAdmin(pageInfo, keyword);
        // 填充评分
        games.forEach(this::fillRating);
        return pageInfo.setRecords(games);
    }
    
    @Override
    public List<Game> getTopGames(Integer limit) {
        List<Game> games = gameMapper.selectTopGames(limit);
        // 填充评分
        games.forEach(this::fillRating);
        return games;
    }
    
    @Override
    public List<Game> getNewGames(Integer limit) {
        List<Game> games = gameMapper.selectNewGames(limit);
        // 填充评分
        games.forEach(this::fillRating);
        return games;
    }
    
    private void fillRating(Game game) {
        if (game != null && (game.getRating() == null || game.getRating() == 0)) {
            Double avgRating = commentMapper.getAverageRating(game.getId());
            if (avgRating != null) {
                game.setRating(Math.round(avgRating * 10) / 10.0);
            }
        }
    }
    
    @Override
    public boolean updateStock(Long gameId, Integer stock) {
        Game game = new Game();
        game.setId(gameId);
        game.setStock(stock);
        return gameMapper.updateById(game) > 0;
    }
    
    @Override
    public boolean updateSales(Long gameId, Integer sales) {
        Game game = getById(gameId);
        if (game != null) {
            game.setSales(game.getSales() + sales);
            return gameMapper.updateById(game) > 0;
        }
        return false;
    }
}
