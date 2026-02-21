package com.gamestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamestore.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BannerMapper extends BaseMapper<Banner> {
    
    @Select("SELECT * FROM banner WHERE status = 0 ORDER BY sort ASC")
    List<Banner> selectActiveBanners();
}
