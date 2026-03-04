package com.gamestore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.Cart;
import com.gamestore.entity.Game;
import com.gamestore.entity.Order;
import com.gamestore.entity.OrderItem;
import com.gamestore.mapper.CartMapper;
import com.gamestore.mapper.GameMapper;
import com.gamestore.mapper.OrderItemMapper;
import com.gamestore.mapper.OrderMapper;
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
            if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
                throw new RuntimeException("订单商品数量异常");
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

            int reserved = gameMapper.reserveStock(cart.getGameId(), cart.getQuantity());
            if (reserved <= 0) {
                throw new RuntimeException("《" + cart.getGameName() + "》库存不足，请返回购物车刷新后重试");
            }
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
        int updated = orderMapper.payOrder(orderNo);
        if (updated <= 0) {
            throw new RuntimeException("订单状态已变更，请刷新后重试");
        }
        return true;
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
    @Transactional
    public boolean cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("只能取消未支付的订单");
        }

        boolean cancelled = lambdaUpdate()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, 0)
                .set(Order::getStatus, 2)
                .update();
        if (!cancelled) {
            throw new RuntimeException("订单状态已变更，请刷新后重试");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            int released = gameMapper.releaseStock(item.getGameId(), item.getQuantity());
            if (released <= 0) {
                throw new RuntimeException("库存回滚失败，请稍后重试");
            }
        }
        return true;
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
