package com.gamestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamestore.entity.Order;
import com.gamestore.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    @Select("SELECT o.*, u.username FROM order_info o LEFT JOIN user u ON o.user_id = u.id WHERE o.id = #{id}")
    Order getOrderDetail(Long id);
    
    List<Order> selectOrderList(Page<Order> page, 
                                @Param("userId") Long userId, 
                                @Param("status") Integer status);
    
    List<OrderItem> selectOrderItems(Long orderId);
    
    @Update("UPDATE order_info SET status = 1, pay_time = NOW() WHERE order_no = #{orderNo}")
    int payOrder(String orderNo);
    
    /**
     * 检查用户是否购买了指定游戏（已完成支付的订单）
     */
    @Select("SELECT COUNT(*) FROM order_info o INNER JOIN order_item oi ON o.id = oi.order_id " +
            "WHERE o.user_id = #{userId} AND oi.game_id = #{gameId} AND o.status = 1")
    int checkUserBoughtGame(@Param("userId") Long userId, @Param("gameId") Long gameId);
}
