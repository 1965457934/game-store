package com.gamestore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gamestore.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {
    
    List<Cart> getCartList(Long userId);
    
    boolean addToCart(Long userId, Long gameId, Integer quantity);
    
    boolean updateCart(Long cartId, Integer quantity);
    
    boolean clearCart(Long userId);
    
    boolean removeFromCart(Long cartId);
}
