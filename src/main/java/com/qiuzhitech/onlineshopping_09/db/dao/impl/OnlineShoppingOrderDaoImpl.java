package com.qiuzhitech.onlineshopping_09.db.dao.impl;

import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingOrderDao;
import com.qiuzhitech.onlineshopping_09.db.mappers.OnlineShoppingOrderMapper;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingOrder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class OnlineShoppingOrderDaoImpl implements OnlineShoppingOrderDao {
    @Resource
    OnlineShoppingOrderMapper orderMapper;

    @Override
    public int insertOrder(OnlineShoppingOrder onlineShoppingOrder) {
        return orderMapper.insert(onlineShoppingOrder);
    }

    @Override
    public OnlineShoppingOrder selectByOrderId(long orderId) {
        return null;
    }


    @Override
    public List<OnlineShoppingOrder> listOrders() {
        return orderMapper.listOrders();
    }

    @Override
    public int updateOrder(OnlineShoppingOrder onlineShoppingOrder) {
        return orderMapper.updateByPrimaryKey(onlineShoppingOrder);
    }

    @Override
    public OnlineShoppingOrder queryOrderByOrderNum(String orderNum) {
         return orderMapper.selectByOrderNum(orderNum);
    }
}
