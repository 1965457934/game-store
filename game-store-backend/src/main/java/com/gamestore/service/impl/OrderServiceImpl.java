package com.gamestore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.*;
import com.gamestore.mapper.*;
import com.gamestore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private GameMapper gameMapper;
    
    @Override
    @Transactional
    public Order createOrder(Long userId, String remark) {
        List<Cart> cartList = cartMapper.selectCartList(userId);
        if (cartList.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }
        
        for (Cart cart : cartList) {
            Game game = gameMapper.selectById(cart.getGameId());
            if (game == null) {
                throw new RuntimeException("存在无效游戏，无法创建订单");
            }
            if (game.getStock() == null || game.getStock() < cart.getQuantity()) {
                throw new RuntimeException("《" + cart.getGameName() + "》库存不足");
            }
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            totalAmount = totalAmount.add(cart.getTotalPrice());
        }
        
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setRemark(remark);
        orderMapper.insert(order);
        
        for (Cart cart : cartList) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setGameId(cart.getGameId());
            item.setGameName(cart.getGameName());
            item.setGameCover(cart.getGameCover());
            item.setQuantity(cart.getQuantity());
            item.setPrice(cart.getPrice());
            item.setTotalPrice(cart.getTotalPrice());
            orderItemMapper.insert(item);
            
            Game game = gameMapper.selectById(cart.getGameId());
            game.setStock(game.getStock() - cart.getQuantity());
            game.setSales(game.getSales() + cart.getQuantity());
            gameMapper.updateById(game);
        }
        
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        cartMapper.delete(wrapper);
        
        return order;
    }
    
    @Override
    public boolean payOrder(String orderNo) {
        Order order = lambdaQuery().eq(Order::getOrderNo, orderNo).one();
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("该订单状态不可支付");
        }
        return orderMapper.payOrder(orderNo) > 0;
    }
    
    @Override
    public Order getOrderDetail(Long orderId) {
        Order order = orderMapper.getOrderDetail(orderId);
        if (order != null) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
            order.setItems(items);
        }
        return order;
    }
    
    @Override
    public Page<Order> getOrderPage(Integer page, Integer size, Long userId, Integer status) {
        Page<Order> pageInfo = new Page<>(page, size);
        return pageInfo.setRecords(orderMapper.selectOrderList(pageInfo, userId, status));
    }
    
    @Override
    public boolean cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("只能取消未支付的订单");
        }
        order.setStatus(2);
        return orderMapper.updateById(order) > 0;
    }
    
    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
