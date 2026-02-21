package com.gamestore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private BigDecimal totalAmount;
    
    private Integer status; // 0-未支付 1-已支付 2-已取消
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime payTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private java.util.List<OrderItem> items;
}
