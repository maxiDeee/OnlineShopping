package com.qiuzhitech.onlineshopping_09.controller;

import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingOrderDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingOrder;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingUser;
import com.qiuzhitech.onlineshopping_09.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Controller
@Slf4j
public class OrderController {
    @Resource
    private OnlineShoppingCommodityDao commodityDao;

    @Resource
    private OnlineShoppingOrderDao orderDao;

    @Resource
    private OrderService orderService;

    @RequestMapping("/commodity/buy/{userId}/{commodityId}")
    public String BuyCommodity(@PathVariable long userId,
                               @PathVariable long commodityId,
                               Map<String, Object> resultMap) {
        // OnlineShoppingOrder order = orderService.createOnlineShoppingOrderOriginal(userId, commodityId);
        // OnlineShoppingOrder order = orderService.createOnlineShoppingOrderOneSQL(userId, commodityId);
        OnlineShoppingOrder order = orderService.createOnlineShoppingOrderRedis(userId, commodityId);
        if (order != null) {
            resultMap.put("orderNo", order.getOrderNo());
            resultMap.put("resultInfo", "Place order success, orderNo: " + order.getOrderNo());
        } else {
            resultMap.put("orderNo", "");
            resultMap.put("resultInfo", "Place order failed, check log for detail");
        }

        return "order_result";
    }

    @GetMapping("/commodity/orderQuery/{orderNum}")
    public String OrderCheck(@PathVariable String orderNum,
                               Map<String, Object> resultMap) {
        OnlineShoppingOrder order = orderDao.queryOrderByOrderNum(orderNum);
        resultMap.put("order", order);
        OnlineShoppingCommodity commodity = commodityDao.selectByCommodityId(order.getCommodityId());
        resultMap.put("commodity", commodity);
        return "order_check";
    }


    @GetMapping("/commodity/payOrder/{orderNum}")
    public String PayOrder(@PathVariable String orderNum,
                             Map<String, Object> resultMap) {
        OnlineShoppingOrder order = orderDao.queryOrderByOrderNum(orderNum);
        order.setPayTime(new Date());
        order.setOrderStatus(2);
        orderDao.updateOrder(order);
        return OrderCheck(orderNum, resultMap);
    }
}
