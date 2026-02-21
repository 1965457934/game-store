package com.gamestore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gamestore.entity.Order;

public interface OrderService extends IService<Order> {
    
    Order createOrder(Long userId, String remark);
    
    boolean payOrder(String orderNo);
    
    Order getOrderDetail(Long orderId);
    
    Page<Order> getOrderPage(Integer page, Integer size, Long userId, Integer status);
    
    boolean cancelOrder(Long orderId, Long userId);
}
