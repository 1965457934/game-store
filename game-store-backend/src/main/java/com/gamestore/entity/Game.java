package com.gamestore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("game")
public class Game {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String cover;
    
    private String description;
    
    private String requirements;
    
    private String screenshots;
    
    private BigDecimal price;
    
    private BigDecimal originalPrice;
    
    private Long categoryId;
    
    private Integer stock;
    
    private Integer sales;
    
    private Integer status; // 0-上架 1-下架
    
    private Double rating; // 平均评分 0-5
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    
    // 非数据库字段
    @TableField(exist = false)
    private String categoryName;
}
