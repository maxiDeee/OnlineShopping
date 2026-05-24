package com.qiuzhitech.onlineshopping_09.service.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.LockSupport;

@Component
@RocketMQMessageListener(topic = "consumerTopic", consumerGroup = "consumerGroup")
@Slf4j
public class TestConsumerListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Override
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody());
        long nanos = 1_000_000_000L;
        LockSupport.parkNanos(nanos);
        //        if (messageExt.getReconsumeTimes() <= 1) {
        //            throw new RuntimeException("reconsume times <= 1");
        //        } else {
        //            log.info("this is " + messageExt.getReconsumeTimes() + " times");
        //            log.info("Message received: " + message);
        //        }
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeThreadMax(1);
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeTimeout(1);
        consumer.setMaxReconsumeTimes(2);
    }
}
