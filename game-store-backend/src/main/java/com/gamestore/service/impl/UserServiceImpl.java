package com.gamestore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.User;
import com.gamestore.mapper.UserMapper;
import com.gamestore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new RuntimeException("账号已被冻结");
        }
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }
    
    @Override
    public User register(User user) {
        // 检查用户名
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查手机号
        if (userMapper.findByPhone(user.getPhone()) != null) {
            throw new RuntimeException("手机号已被注册");
        }
        // MD5加密密码
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setStatus(0);
        user.setRole(0);
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }
    
    @Override
    public boolean updateUserInfo(User user) {
        // 不在这里更新密码，密码有单独的接口
        user.setPassword(null);
        return userMapper.updateById(user) > 0;
    }
    
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 验证旧密码
        String oldMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!oldMd5.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        // 更新新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        return userMapper.updateById(updateUser) > 0;
    }
    
    @Override
    public boolean freezeUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setStatus(1);
        return userMapper.updateById(user) > 0;
    }
    
    @Override
    public boolean unfreezeUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setStatus(0);
        return userMapper.updateById(user) > 0;
    }
}
