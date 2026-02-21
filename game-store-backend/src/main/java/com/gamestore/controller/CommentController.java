package com.gamestore.controller;

import com.gamestore.entity.Comment;
import com.gamestore.service.CommentService;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @GetMapping("/list")
    public Result<?> getCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) Long userId) {
        return Result.success(commentService.getCommentPage(page, size, gameId, userId));
    }
    
    /**
     * 获取当前用户对某游戏的评论（用于判断是否已评论）
     */
    @GetMapping("/user-comment")
    public Result<?> getUserComment(@RequestParam Long gameId, HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        Comment comment = commentService.getUserComment(userId, gameId);
        return Result.success(comment);
    }
    
    @PostMapping("/add")
    public Result<?> addComment(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            // 获取用户ID
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.error(401, "请先登录");
            }
            Long userId = Long.valueOf(userIdObj.toString());
            
            // 获取游戏ID
            Object gameIdObj = params.get("gameId");
            if (gameIdObj == null) {
                return Result.error("游戏ID不能为空");
            }
            Long gameId = Long.valueOf(gameIdObj.toString());
            
            // 获取评论内容
            String content = (String) params.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }
            
            // 获取评分，默认为5，确保精度正确
            Object ratingObj = params.get("rating");
            Double rating = 5.0;
            if (ratingObj != null) {
                // 处理不同类型的评分值
                if (ratingObj instanceof Number) {
                    rating = ((Number) ratingObj).doubleValue();
                } else {
                    rating = Double.parseDouble(ratingObj.toString());
                }
                // 使用 BigDecimal 确保精度（保留一位小数）
                BigDecimal ratingBd = BigDecimal.valueOf(rating);
                rating = ratingBd.setScale(1, RoundingMode.HALF_UP).doubleValue();
                // 确保评分在 0.5-5.0 范围内，并且是 0.5 的倍数
                if (rating < 0.5) rating = 0.5;
                if (rating > 5.0) rating = 5.0;
                // 确保是 0.5 的倍数
                rating = Math.round(rating * 2) / 2.0;
                System.out.println("接收评分: " + ratingObj + " -> 处理后: " + rating);
            }
            
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setGameId(gameId);
            comment.setContent(content.trim());
            comment.setRating(rating);
            
            if (commentService.addComment(comment)) {
                return Result.success("评论成功", null);
            }
            return Result.error("评论失败");
        } catch (NumberFormatException e) {
            return Result.error("参数格式错误：" + e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("评论失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新评论
     */
    @PutMapping("/update/{id}")
    public Result<?> updateComment(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            // 获取用户ID
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.error(401, "请先登录");
            }
            Long userId = Long.valueOf(userIdObj.toString());
            
            // 获取评论内容
            String content = (String) params.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }
            
            // 获取评分，确保精度正确
            Object ratingObj = params.get("rating");
            Double rating = 5.0;
            if (ratingObj != null) {
                // 处理不同类型的评分值
                if (ratingObj instanceof Number) {
                    rating = ((Number) ratingObj).doubleValue();
                } else {
                    rating = Double.parseDouble(ratingObj.toString());
                }
                // 使用 BigDecimal 确保精度
                BigDecimal ratingBd = BigDecimal.valueOf(rating);
                rating = ratingBd.setScale(1, RoundingMode.HALF_UP).doubleValue();
                // 确保评分在 0.5-5.0 范围内，并且是 0.5 的倍数
                if (rating < 0.5) rating = 0.5;
                if (rating > 5.0) rating = 5.0;
                rating = Math.round(rating * 2) / 2.0;
            }
            
            // 获取游戏ID
            Object gameIdObj = params.get("gameId");
            Long gameId = gameIdObj != null ? Long.valueOf(gameIdObj.toString()) : null;
            
            Comment comment = new Comment();
            comment.setId(id);
            comment.setUserId(userId);
            comment.setGameId(gameId);
            comment.setContent(content.trim());
            comment.setRating(rating);
            
            if (commentService.updateComment(comment)) {
                return Result.success("评论更新成功", null);
            }
            return Result.error("评论更新失败");
        } catch (NumberFormatException e) {
            return Result.error("参数格式错误：" + e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("评论更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除评论（用户删除自己的评论）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        
        try {
            if (commentService.deleteComment(id, userId)) {
                return Result.success("删除成功", null);
            }
            return Result.error("删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 管理员删除评论
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result<?> adminDeleteComment(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        // 只有管理员可以删除评论
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (commentService.removeById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
