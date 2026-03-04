package com.gamestore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamestore.entity.User;
import com.gamestore.service.UserService;
import com.gamestore.utils.JwtUtil;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        try {
            User newUser = userService.register(user);
            return Result.success("注册成功", newUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            User user = userService.login(username, password);
            String token = jwtUtil.generateToken(user);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", userService.getUserInfo(user.getId()));
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }
    
    @PutMapping("/update")
    public Result<?> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        if (userService.updateUserInfo(user)) {
            return Result.success("更新成功", userService.getUserInfo(userId));
        }
        return Result.error("更新失败");
    }
    
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        try {
            if (userService.updatePassword(userId, oldPassword, newPassword)) {
                return Result.success("密码修改成功", null);
            }
            return Result.error("密码修改失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权操作");
        }
        Page<User> pageInfo = userService.page(new Page<>(page, size));
        return Result.success(pageInfo);
    }
    
    @PostMapping("/freeze/{id}")
    public Result<?> freezeUser(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (id.equals(userId)) {
            return Result.error("不能冻结自己的账号");
        }
        if (userService.freezeUser(id)) {
            return Result.success("冻结成功", null);
        }
        return Result.error("冻结失败");
    }
    
    @PostMapping("/unfreeze/{id}")
    public Result<?> unfreezeUser(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role != 1) {
            return Result.error(403, "无权操作");
        }
        if (userService.unfreezeUser(id)) {
            return Result.success("解冻成功", null);
        }
        return Result.error("解冻失败");
    }
}
