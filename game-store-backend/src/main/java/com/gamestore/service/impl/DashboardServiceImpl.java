package com.gamestore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gamestore.entity.Order;
import com.gamestore.entity.User;
import com.gamestore.entity.Game;
import com.gamestore.mapper.OrderMapper;
import com.gamestore.mapper.UserMapper;
import com.gamestore.mapper.GameMapper;
import com.gamestore.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private GameMapper gameMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        Long userCount = userMapper.selectCount(null);
        stats.put("userCount", userCount);
        
        // 游戏总数
        Long gameCount = gameMapper.selectCount(null);
        stats.put("gameCount", gameCount);
        
        // 订单总数
        Long orderCount = orderMapper.selectCount(null);
        stats.put("orderCount", orderCount);
        
        // 总销售额（已支付的订单）
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        BigDecimal totalAmount = orderMapper.selectList(wrapper).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalAmount", totalAmount);
        
        return stats;
    }
}
