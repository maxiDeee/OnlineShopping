package com.qiuzhitech.onlineshopping_09.service;

import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
public class SearchService {
    @Resource
    private OnlineShoppingCommodityDao commodityDao;

    @Resource
    private EsService esService;

    public List<OnlineShoppingCommodity> searchCommodityDB(String keyword) {
        return commodityDao.searchCommodityByKeyword(keyword);
    }

    public List<OnlineShoppingCommodity> searchCommodityES(String keyword) throws IOException {
        return esService.searchCommodity(keyword, 0, 100);
    }
}
