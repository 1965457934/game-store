package com.gamestore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long gameId;
    
    private String content;
    
    @TableField(value = "rating", jdbcType = org.apache.ibatis.type.JdbcType.DECIMAL)
    private Double rating; // 评分 0.5-5，支持半星
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String avatar;
    
    @TableField(exist = false)
    private String gameName;
}
