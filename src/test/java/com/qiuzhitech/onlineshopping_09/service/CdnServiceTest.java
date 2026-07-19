package com.qiuzhitech.onlineshopping_09.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.FileNotFoundException;


@SpringBootTest
class CdnServiceTest {
    @Resource
    private CdnService cdnService;

    @Test
    void createHTML() throws FileNotFoundException {
        cdnService.createHTML(124L);
    }
}