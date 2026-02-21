package com.gamestore.controller;

import com.gamestore.entity.Cart;
import com.gamestore.service.CartService;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping("/list")
    public Result<?> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Cart> list = cartService.getCartList(userId);
        return Result.success(list);
    }
    
    @PostMapping("/add")
    public Result<?> addToCart(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long gameId = Long.valueOf(params.get("gameId").toString());
        Integer quantity = Integer.valueOf(params.getOrDefault("quantity", 1).toString());
        try {
            if (cartService.addToCart(userId, gameId, quantity)) {
                return Result.success("加入购物车成功", null);
            }
            return Result.error("加入购物车失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/update/{id}")
    public Result<?> updateCart(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer quantity = params.get("quantity");
        if (cartService.updateCart(id, quantity)) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteCart(@PathVariable Long id) {
        if (cartService.removeFromCart(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (cartService.clearCart(userId)) {
            return Result.success("清空成功", null);
        }
        return Result.error("清空失败");
    }
}
