package com.gamestore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.Cart;
import com.gamestore.entity.Game;
import com.gamestore.mapper.CartMapper;
import com.gamestore.mapper.GameMapper;
import com.gamestore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private GameMapper gameMapper;
    
    @Override
    public List<Cart> getCartList(Long userId) {
        return cartMapper.selectCartList(userId);
    }
    
    @Override
    @Transactional
    public boolean addToCart(Long userId, Long gameId, Integer quantity) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        if (game.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        Cart existingCart = cartMapper.selectByUserIdAndGameId(userId, gameId);
        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            existingCart.setTotalPrice(game.getPrice().multiply(new BigDecimal(existingCart.getQuantity())));
            return cartMapper.updateById(existingCart) > 0;
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGameId(gameId);
            cart.setQuantity(quantity);
            cart.setPrice(game.getPrice());
            cart.setTotalPrice(game.getPrice().multiply(new BigDecimal(quantity)));
            return cartMapper.insert(cart) > 0;
        }
    }
    
    @Override
    public boolean updateCart(Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            return false;
        }
        cart.setQuantity(quantity);
        cart.setTotalPrice(cart.getPrice().multiply(new BigDecimal(quantity)));
        return cartMapper.updateById(cart) > 0;
    }
    
    @Override
    public boolean clearCart(Long userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return cartMapper.delete(wrapper) > 0;
    }
    
    @Override
    public boolean removeFromCart(Long cartId) {
        return cartMapper.deleteById(cartId) > 0;
    }
}
