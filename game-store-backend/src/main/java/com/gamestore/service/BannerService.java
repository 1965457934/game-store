package com.gamestore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gamestore.entity.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {
    
    List<Banner> getActiveBanners();
}
