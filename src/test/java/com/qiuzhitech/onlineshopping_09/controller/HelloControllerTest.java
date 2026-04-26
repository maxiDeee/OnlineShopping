package com.qiuzhitech.onlineshopping_09.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
class HelloControllerTest {

    @Resource
    HelloController helloController;

    @Mock
    Dependency dependency;

    @BeforeEach
    void setUp() {

    }

    @Test
    void sumPlus2() {
        helloController = new HelloController(new Dependency());
        int sum = helloController.sumPlus2(1, 2);
        assertEquals(5, sum);
    }

    @Test
    void sumPlus2withMock() {
        when(dependency.sum(anyInt(), anyInt())).thenReturn(100);
        helloController = new HelloController(dependency);
        int sum = helloController.sumPlus2(1, 2);
        assertEquals(102, sum);
    }
}