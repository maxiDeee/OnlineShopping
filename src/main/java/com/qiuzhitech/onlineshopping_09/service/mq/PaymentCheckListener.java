package com.qiuzhitech.onlineshopping_09.service.mq;

import com.alibaba.fastjson.JSON;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingOrderDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingOrder;
import com.qiuzhitech.onlineshopping_09.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.locks.LockSupport;

@Component
@RocketMQMessageListener(topic = "paymentCheck", consumerGroup = "paymentCheckGroup")
@Slf4j
public class PaymentCheckListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Resource
    OnlineShoppingCommodityDao onlineShoppingCommodityDao;

    @Resource
    OnlineShoppingOrderDao onlineShoppingOrderDao;

    @Resource
    RocketMQService rocketMQService;

    @Resource
    RedisService redisService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String orderString = new String(messageExt.getBody());
        log.info("Received order message: {}", orderString);
        OnlineShoppingOrder originalOrder = JSON.parseObject(orderString, OnlineShoppingOrder.class);
        OnlineShoppingOrder updatedOrder = onlineShoppingOrderDao.queryOrderByOrderNum(originalOrder.getOrderNo());
        if (updatedOrder == null) {
            log.error("Can't find order in DB");
            return;
        }
        // 1. check current Order status in DB
        // Status as below:
        // 0. Invalid order, Since no available stock
        // 1. already create order, pending for payment
        // 2. finished payment
        // 99. invalid order due to payment proceed overtime
        if (updatedOrder.getOrderStatus() == 2) {
            log.info("Order has finished payment for orderNo: " + originalOrder.getOrderNo());
        } else {
            // 2. change order status to 99, invalid the order
            log.info("Didn't pay the order on time, order number: {}", updatedOrder.getOrderNo());
            updatedOrder.setOrderStatus(99);
            onlineShoppingOrderDao.updateOrder(updatedOrder);
            // 3. Update Commodity Table
            onlineShoppingCommodityDao.reverseStock(updatedOrder.getCommodityId());
            // 4. Update Redis stock
            String key = "online_shopping_commodity:" + updatedOrder.getCommodityId();
            long currentStock = redisService.revertStock(key);
            log.info("Redis revert commodityId {}, current available count: {}",
                    updatedOrder.getCommodityId(),
                    currentStock);
            // 4. Remove from Redis denyList
            redisService.removeFromDenyList(String.valueOf(updatedOrder.getUserId()),
                    String.valueOf(updatedOrder.getCommodityId()));;
        }
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeThreadMax(1);
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeTimeout(1);
        consumer.setMaxReconsumeTimes(2);
    }
}
