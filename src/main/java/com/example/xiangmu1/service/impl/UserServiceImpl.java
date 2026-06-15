package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.entity.User;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.UserService;
import com.example.xiangmu1.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public String login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }
}
