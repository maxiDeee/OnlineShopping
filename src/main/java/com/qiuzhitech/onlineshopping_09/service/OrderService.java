package com.qiuzhitech.onlineshopping_09.service;

import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingOrderDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingOrder;
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

    public OnlineShoppingOrder createOnlineShoppingOrderOriginal(long userId,
                                                                  long commodityId) {
        OnlineShoppingCommodity onlineShoppingCommodity = commodityDao.selectByCommodityId(commodityId);
        Integer availableStock = onlineShoppingCommodity.getAvailableStock();
        if (availableStock > 0) {
            availableStock--;
            onlineShoppingCommodity.setAvailableStock(availableStock);
            int result = commodityDao.updateCommodity(onlineShoppingCommodity);
            if (result > 0) {
                return createOrder(userId, commodityId);
            }
        }
        log.warn("commodity out of stock, commodityId: " + commodityId);
        return null;
    }

    public OnlineShoppingOrder createOnlineShoppingOrderOneSQL(long userId,
                                                                long commodityId) {
        int retSuccess = commodityDao.deductStock(commodityId); // 0: fail to deduct  1: success to deduct
        if (retSuccess > 0) {
            return createOrder(userId, commodityId);
        }
        log.warn("commodity out of stock, commodityId: " + commodityId);
        return null;
    }

    private OnlineShoppingOrder createOrder(long userId,
                        long commodityId) {
        OnlineShoppingOrder order = OnlineShoppingOrder.builder()
                .userId(userId)
                .commodityId(commodityId)
                .orderNo(UUID.randomUUID().toString())
                .orderAmount(1L)
                .createTime(new Date())
                .orderStatus(1)
                .build();
        orderDao.insertOrder(order);
        return order;
    }
}
