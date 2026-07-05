package com.qiuzhitech.onlineshopping_09.config;

import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import com.qiuzhitech.onlineshopping_09.service.EsService;
import com.qiuzhitech.onlineshopping_09.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class RedisPreHeatRunner implements ApplicationRunner {
    @Resource
    private OnlineShoppingCommodityDao commodityDao;

    @Resource
    private RedisService redisService;

    @Resource
    private EsService esService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<OnlineShoppingCommodity> onlineShoppingCommodities = commodityDao.listItems();
        for (OnlineShoppingCommodity commodity: onlineShoppingCommodities) {
            String key = "online_shopping_commodity:" + commodity.getCommodityId();
            redisService.setValue(key, commodity.getAvailableStock().toString());
            esService.insertCommodity(commodity);
            log.info("preHeat Starting: Initialize redis stock with commodityId: " + commodity.getCommodityId());
        }
    }
}
