package com.qiuzhitech.onlineshopping_09.controller;

import com.qiuzhitech.onlineshopping_09.config.MyUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private Map<Integer, MyUser> myUsers = new HashMap<>();

    @Resource(name = "Lyon")
    private MyUser defaultUser;

    @PostMapping("/users")
    public String addUser(@RequestParam int id,
                          @RequestParam String name,
                          @RequestParam int age,
                          @RequestParam String email,
                          Map<String, Object> map)
    {
        MyUser newUser = new MyUser(id, name, age, email);
        myUsers.put(id, newUser);
        map.put("user", newUser);
        return "user_detail";
    }

    @GetMapping("/users/{id}")
    public String getUser(@PathVariable int id,
                          Map<String, Object> map)
    {
        MyUser user = myUsers.getOrDefault(id, defaultUser);
        map.put("user", user);
        return "user_detail";
    }
}
