package com.gamestore.controller;

import com.gamestore.entity.Game;
import com.gamestore.service.GameService;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @GetMapping("/list")
    public Result<?> getGameList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy) {
        // 只返回上架的游戏（用户端使用）
        return Result.success(gameService.getGamePage(page, size, keyword, categoryId, sortBy));
    }
    
    @GetMapping("/admin/list")
    public Result<?> getGameListForAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        // 验证管理员权限
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        // 返回所有游戏（包括下架的，管理后台使用）
        return Result.success(gameService.getGamePageForAdmin(page, size, keyword));
    }
    
    @GetMapping("/detail/{id}")
    public Result<?> getGameDetail(@PathVariable Long id) {
        Game game = gameService.getGameDetail(id);
        if (game == null) {
            return Result.error("游戏不存在");
        }
        return Result.success(game);
    }
    
    @GetMapping("/top")
    public Result<?> getTopGames(@RequestParam(defaultValue = "6") Integer limit) {
        List<Game> games = gameService.getTopGames(limit);
        return Result.success(games);
    }
    
    @GetMapping("/new")
    public Result<?> getNewGames(@RequestParam(defaultValue = "6") Integer limit) {
        List<Game> games = gameService.getNewGames(limit);
        return Result.success(games);
    }
    
    @PostMapping("/add")
    public Result<?> addGame(@RequestBody Game game, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (gameService.save(game)) {
            return Result.success("添加成功", null);
        }
        return Result.error("添加失败");
    }
    
    @PutMapping("/update")
    public Result<?> updateGame(@RequestBody Game game, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (gameService.updateById(game)) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteGame(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (gameService.removeById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
