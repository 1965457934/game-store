package com.gamestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamestore.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
