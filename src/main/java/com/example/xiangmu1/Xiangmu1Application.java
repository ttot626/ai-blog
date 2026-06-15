package com.example.xiangmu1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.xiangmu1.mapper")
public class Xiangmu1Application {

    public static void main(String[] args) {
        SpringApplication.run(Xiangmu1Application.class, args);
    }
}
