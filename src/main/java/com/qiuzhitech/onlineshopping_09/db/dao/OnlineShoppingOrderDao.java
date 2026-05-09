package com.qiuzhitech.onlineshopping_09.db.dao;

import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingOrder;

public interface OnlineShoppingOrderDao {
    int insertOrder(OnlineShoppingOrder record);

    int deleteOrderById(Long orderId);

    OnlineShoppingOrder queryOrderById(Long orderId);

    int updateOrder(OnlineShoppingOrder record);
}
