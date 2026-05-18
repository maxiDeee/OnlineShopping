package com.qiuzhitech.onlineshopping_09.controller;

import com.qiuzhitech.onlineshopping_09.db.po.MyUser;
import com.qiuzhitech.onlineshopping_09.service.JwtService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private Map<Integer, MyUser> myUsers = new HashMap<>();

    @Resource(name = "Lyon")
    private MyUser defaultUser;

    @Resource
    JwtService jwtService;

    @PostMapping("/users")
    public String addUser(@RequestParam int id,
                          @RequestParam String name,
                          @RequestParam int age,
                          @RequestParam String email,
                          Map<String, Object> map)
    {
        MyUser newUser = new MyUser(id, name, age, email);
        String token = jwtService.encryptUser(newUser);
        String decryptedUserId = jwtService.decryptUserId(token) ;
        myUsers.put(id, newUser);
        map.put("user", newUser);
        map.put("jwtToken", token);
        map.put("jwtUserId", decryptedUserId);
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

    @PutMapping("/users/{id}")
    public String updateUser(@PathVariable int id,
                             @RequestParam String name,
                             @RequestParam int age,
                             @RequestParam String email,
                             Map<String, Object> map) {
        MyUser user = myUsers.getOrDefault(id, defaultUser);
        if (user != null) {
            user.setName(name);
            user.setAge(age);
            user.setEmail(email);
            myUsers.put(id, user);
        }
        map.put("user", user);
        return "user_detail";
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable int id,
                             Map<String, Object> map) {
        MyUser user = myUsers.remove(id);
        map.put("user", user);
        return "user_detail";
    }
}
