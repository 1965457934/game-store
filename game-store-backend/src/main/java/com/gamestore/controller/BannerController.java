package com.gamestore.controller;

import com.gamestore.entity.Banner;
import com.gamestore.service.BannerService;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/banner")
public class BannerController {
    
    @Autowired
    private BannerService bannerService;
    
    @GetMapping("/list")
    public Result<?> getBannerList() {
        List<Banner> list = bannerService.getActiveBanners();
        return Result.success(list);
    }
    
    @GetMapping("/all")
    public Result<?> getAllBanners(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        return Result.success(bannerService.list());
    }
    
    @PostMapping("/add")
    public Result<?> addBanner(@RequestBody Banner banner, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (bannerService.save(banner)) {
            return Result.success("添加成功", null);
        }
        return Result.error("添加失败");
    }
    
    @PutMapping("/update")
    public Result<?> updateBanner(@RequestBody Banner banner, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (bannerService.updateById(banner)) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteBanner(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (bannerService.removeById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
