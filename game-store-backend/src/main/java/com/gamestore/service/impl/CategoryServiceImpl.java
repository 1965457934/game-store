package com.gamestore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.Category;
import com.gamestore.mapper.CategoryMapper;
import com.gamestore.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
