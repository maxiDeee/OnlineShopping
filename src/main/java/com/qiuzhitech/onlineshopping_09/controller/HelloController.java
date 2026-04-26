package com.qiuzhitech.onlineshopping_09.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {

    @Resource
    private Dependency dependency;

    public HelloController(Dependency dependency) {
        this.dependency = dependency;
    }

    public int sumPlus2(int a, int b) {
        return dependency.sum(a, b) + 2;
    }

    @RequestMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @RequestMapping("/echo/{abc}")
    public String echo(@PathVariable String abc) {
        return "Hello From " + abc;
    }
}