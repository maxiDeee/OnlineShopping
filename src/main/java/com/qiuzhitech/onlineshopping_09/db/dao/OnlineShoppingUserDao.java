package com.qiuzhitech.onlineshopping_09.db.dao;

import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingUser;

public interface OnlineShoppingUserDao {
    int insertUser(OnlineShoppingUser user);

    int deleteUserById(Long userId);

    OnlineShoppingUser queryUserById(Long userId);

    int updateUser(OnlineShoppingUser user);
}
