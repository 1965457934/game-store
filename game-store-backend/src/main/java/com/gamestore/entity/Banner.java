package com.gamestore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("banner")
public class Banner {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String image;
    
    private String link;
    
    private Integer sort;
    
    private Integer status; // 0-显示 1-隐藏
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
