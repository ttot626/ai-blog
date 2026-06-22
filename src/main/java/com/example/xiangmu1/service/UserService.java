package com.example.xiangmu1.service;

import com.example.xiangmu1.vo.UserHomeVO;

public interface UserService {

    void register(String username, String password);

    String login(String username, String password);

    UserHomeVO home(Long userId);
}
