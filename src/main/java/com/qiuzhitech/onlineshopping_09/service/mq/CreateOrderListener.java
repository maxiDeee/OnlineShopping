package com.qiuzhitech.onlineshopping_09.service.mq;

import com.alibaba.fastjson.JSON;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingOrderDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingOrder;
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
@RocketMQMessageListener(topic = "createOrder", consumerGroup = "createOrderGroup")
@Slf4j
public class CreateOrderListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Resource
    OnlineShoppingCommodityDao onlineShoppingCommodityDao;

    @Resource
    OnlineShoppingOrderDao onlineShoppingOrderDao;

    @Resource
    RocketMQService rocketMQService;

    @Override
    public void onMessage(MessageExt messageExt) {
        // 1. check details of order from message
        String orderString = new String(messageExt.getBody());
        log.info("Received order message: {}", orderString);
        long nanos = 1_000_000_000L; // seconds in nanoseconds
        LockSupport.parkNanos(nanos);
        OnlineShoppingOrder order = JSON.parseObject(orderString, OnlineShoppingOrder.class);
        // 2. reduce stock available and increase lock number
        int res = onlineShoppingCommodityDao.deductStock(order.getCommodityId());
        if (res > 0) {
            // 1. already create order, pending for payment
            order.setCreateTime(new Date());
            // 2. Insert into DAO layer
            onlineShoppingOrderDao.insertOrder(order);
            log.info("Create order has been successfully updated in DB:" + order);
            // 3. Send delay message to MQ for check payment is overtime or not
            // messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
            rocketMQService.sendDelayMessage("paymentCheck", JSON.toJSONString(order), 4);
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
