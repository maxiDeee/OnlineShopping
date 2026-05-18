package com.qiuzhitech.onlineshopping_09.config;

import com.qiuzhitech.onlineshopping_09.db.po.MyUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean(name = "Lyon")
    public MyUser user() {
        return new MyUser(1,"Lyon", 111, "Lyon@hotmail.com");
    }

    @Bean(name = "Z3")
    public MyUser userZ3() {
        return new MyUser(3,"Z3", 18, "z3@hotmail.com");
    }
}