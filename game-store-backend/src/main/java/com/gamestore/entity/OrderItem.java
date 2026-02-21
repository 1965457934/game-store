package com.gamestore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Long gameId;
    
    private String gameName;
    
    private String gameCover;
    
    private Integer quantity;
    
    private BigDecimal price;
    
    private BigDecimal totalPrice;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
