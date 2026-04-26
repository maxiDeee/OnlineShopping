package com.qiuzhitech.onlineshopping_09.controller;

import org.springframework.stereotype.Service;

@Service
public class Dependency {
    public int sum(int a, int b) {
        return a + b;
    }
}
