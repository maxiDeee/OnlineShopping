package com.qiuzhitech.onlineshopping_09;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.qiuzhitech.onlineshopping_09.db.mappers")
@SpringBootApplication
public class OnlineShopping09Application {

    public static void main(String[] args) {
        SpringApplication.run(OnlineShopping09Application.class, args);
    }

}
