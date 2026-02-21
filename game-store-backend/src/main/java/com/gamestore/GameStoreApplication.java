package com.gamestore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gamestore.mapper")
public class GameStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameStoreApplication.class, args);
        System.out.println("========================================");
        System.out.println("      游戏商城系统启动成功！");
        System.out.println("      访问地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
