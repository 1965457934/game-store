package com.gamestore.controller;

import com.gamestore.entity.Order;
import com.gamestore.service.OrderService;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/list")
    public Result<?> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        // 管理员可以查看所有订单，普通用户只能看自己的
        Long queryUserId = role == 1 ? null : userId;
        return Result.success(orderService.getOrderPage(page, size, queryUserId, status));
    }
    
    @GetMapping("/detail/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Order order = orderService.getOrderDetail(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if ((role == null || role != 1) && !order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        return Result.success(order);
    }
    
    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String remark = params.getOrDefault("remark", "");
        try {
            Order order = orderService.createOrder(userId, remark);
            return Result.success("创建订单成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/pay")
    public Result<?> payOrder(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String orderNo = params.get("orderNo");
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error("订单号不能为空");
        }
        
        Order order = orderService.lambdaQuery().eq(Order::getOrderNo, orderNo).one();
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if ((role == null || role != 1) && !order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        
        try {
            if (orderService.payOrder(orderNo)) {
                return Result.success("支付成功", null);
            }
            return Result.error("支付失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/cancel/{id}")
    public Result<?> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            if (orderService.cancelOrder(id, userId)) {
                return Result.success("取消成功", null);
            }
            return Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
