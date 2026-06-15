package com.example.xiangmu1.service;

public interface UserService {

    void register(String username, String password);

    String login(String username, String password);
}
