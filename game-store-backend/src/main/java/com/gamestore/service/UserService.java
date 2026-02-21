package com.gamestore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gamestore.entity.User;

public interface UserService extends IService<User> {
    
    User login(String username, String password);
    
    User register(User user);
    
    User getUserInfo(Long userId);
    
    boolean updateUserInfo(User user);
    
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    
    boolean freezeUser(Long userId);
    
    boolean unfreezeUser(Long userId);
}
