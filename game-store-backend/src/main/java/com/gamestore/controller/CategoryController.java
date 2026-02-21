package com.gamestore.controller;

import com.gamestore.entity.Category;
import com.gamestore.service.CategoryService;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("/list")
    public Result<?> getCategoryList() {
        List<Category> list = categoryService.list();
        return Result.success(list);
    }
    
    @PostMapping("/add")
    public Result<?> addCategory(@RequestBody Category category, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (categoryService.save(category)) {
            return Result.success("添加成功", null);
        }
        return Result.error("添加失败");
    }
    
    @PutMapping("/update")
    public Result<?> updateCategory(@RequestBody Category category, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (categoryService.updateById(category)) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (categoryService.removeById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
