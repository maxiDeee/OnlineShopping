package com.qiuzhitech.onlineshopping_09.service;

import com.alibaba.fastjson.JSON;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingOrderDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingOrder;
import com.qiuzhitech.onlineshopping_09.service.mq.RocketMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {
    @Resource
    private OnlineShoppingCommodityDao commodityDao;

    @Resource
    private OnlineShoppingOrderDao orderDao;

    @Resource
    private RedisService redisService;

    @Resource
    private RocketMQService rocketMQService;

    public OnlineShoppingOrder createOnlineShoppingOrderOriginal(long userId,
                                                                  long commodityId) {
        OnlineShoppingCommodity onlineShoppingCommodity = commodityDao.selectByCommodityId(commodityId);
        Integer availableStock = onlineShoppingCommodity.getAvailableStock();
        if (availableStock > 0) {
            availableStock--;
            onlineShoppingCommodity.setAvailableStock(availableStock);
            int result = commodityDao.updateCommodity(onlineShoppingCommodity);
            if (result > 0) {
                return createOrder(userId, commodityId, true);
            }
        }
        log.warn("commodity out of stock, commodityId: " + commodityId);
        return null;
    }

    public OnlineShoppingOrder createOnlineShoppingOrderOneSQL(long userId,
                                                                long commodityId) {
        int retSuccess = commodityDao.deductStock(commodityId); // 0: fail to deduct  1: success to deduct
        if (retSuccess > 0) {
            return createOrder(userId, commodityId, true);
        }
        log.warn("commodity out of stock, commodityId: " + commodityId);
        return null;
    }

    public OnlineShoppingOrder createOnlineShoppingOrderRedis(long userId,
                                                              long commodityId) {
        String redisKey = "online_shopping_commodity:" + commodityId;
        long remainStock = redisService.deductStock(redisKey);
        if (remainStock >= 0) {
            return createOnlineShoppingOrderOriginal(userId, commodityId);
        } else {
            log.warn("commodity out of stock, commodityId:" + commodityId);
            return null;
        }
    }

    public OnlineShoppingOrder createOnlineShoppingOrderDistributedLock(long userId,
                                                                        long commodityId) {
        String requestId = UUID.randomUUID().toString();
        boolean lockAcquired = redisService.tryToGetDistributeLock(
                String.valueOf(commodityId), requestId, 5000);
        if (!lockAcquired) {
            log.warn("failed to acquire distributed lock, commodityId:" + commodityId);
            return null;
        }
        try {
            return createOnlineShoppingOrderOriginal(userId, commodityId);
        } finally {
            redisService.releaseDistributedLock(String.valueOf(commodityId), requestId);
        }
    }

    public OnlineShoppingOrder createOnlineShoppingOrderFinal(long userId,
                                                              long commodityId) {
        if (redisService.isInDenyList(String.valueOf(userId), String.valueOf(commodityId))) {
            log.info("Each user have only one quote for this commodity {}", commodityId );
            return null;
        }

        String redisKey = "online_shopping_commodity:" + commodityId;
        long remainStock = redisService.deductStock(redisKey);   // 1W
        if (remainStock >= 0) {
            // 1W
            OnlineShoppingOrder order = createOrder(userId, commodityId, false);
            rocketMQService.sendFIFOMessage("createOrder", JSON.toJSONString(order));
            log.info("Place order successfully");
            return order;
//            // 1K
//            int ret = commodityDao.deductStock(commodityId);// 0: fail to deduct 1: success to deduct
//            if (ret > 0) {
//                return createOrder(userId, commodityId);
        }
        log.warn("commodity out of stock, commodityId:" + commodityId);
        return null;
    }

    private OnlineShoppingOrder createOrder(long userId,
                        long commodityId, boolean shouldSaveToDB) {
        // 0. Invalid order, Since no available stock
        // 1. already create order, pending for payment
        // 2. finished payment
        // 99. invalid order due to payment proceed overtime
        OnlineShoppingOrder order = OnlineShoppingOrder.builder()
                .userId(userId)
                .commodityId(commodityId)
                .orderNo(UUID.randomUUID().toString())
                .orderAmount(1L)
                .createTime(new Date())
                .orderStatus(1)
                .build();
        if (shouldSaveToDB) {
            orderDao.insertOrder(order);
        }
        return order;
    }
}
