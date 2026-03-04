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

    @GetMapping("/user-comment")
    public Result<?> getUserComment(@RequestParam Long gameId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(commentService.getUserComment(userId, gameId));
    }

    @PostMapping("/add")
    public Result<?> addComment(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }

            Object gameIdObj = params.get("gameId");
            if (gameIdObj == null) {
                return Result.error("游戏ID不能为空");
            }
            Long gameId = Long.valueOf(gameIdObj.toString());

            String content = (String) params.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }

            Double rating = normalizeRating(params.get("rating"));

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
            return Result.error("参数格式错误: " + e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("评论失败: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public Result<?> updateComment(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }

            String content = (String) params.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }

            Object gameIdObj = params.get("gameId");
            Long gameId = gameIdObj != null ? Long.valueOf(gameIdObj.toString()) : null;
            Double rating = normalizeRating(params.get("rating"));

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
            return Result.error("参数格式错误: " + e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("评论更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        try {
            if (commentService.deleteComment(id, userId)) {
                return Result.success("删除成功", null);
            }
            return Result.error("删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<?> adminDeleteComment(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权操作");
        }
        try {
            if (commentService.adminDeleteComment(id)) {
                return Result.success("删除成功", null);
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        return userIdObj == null ? null : Long.valueOf(userIdObj.toString());
    }

    private Double normalizeRating(Object ratingObj) {
        double rating = 5.0;
        if (ratingObj != null) {
            if (ratingObj instanceof Number) {
                rating = ((Number) ratingObj).doubleValue();
            } else {
                rating = Double.parseDouble(ratingObj.toString());
            }
        }
        BigDecimal ratingBd = BigDecimal.valueOf(rating);
        rating = ratingBd.setScale(1, RoundingMode.HALF_UP).doubleValue();
        if (rating < 0.5) {
            rating = 0.5;
        }
        if (rating > 5.0) {
            rating = 5.0;
        }
        return Math.round(rating * 2) / 2.0;
    }
}
