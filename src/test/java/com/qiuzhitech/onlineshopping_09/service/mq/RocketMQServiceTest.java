package com.qiuzhitech.onlineshopping_09.service.mq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RocketMQServiceTest {
    @Resource
    private RocketMQService rocketMQService;


    @Test
    void sendMessage() {
        int i = 10;
        while (i>0) {
            i--;
            rocketMQService.sendMessage("consumerTopic",
                    "message " + i + " : Today is " + new Date());
        }
    }

    @Test
    void sendFIFOMessage() {
        int i = 1;
        while (i>0) {
            i--;
            rocketMQService.sendFIFOMessage("consumerTopic",
                    "message " + i + " : Today is " + new Date());
        }
    }
}