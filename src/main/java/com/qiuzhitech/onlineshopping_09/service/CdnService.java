package com.qiuzhitech.onlineshopping_09.service;

import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CdnService {
    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private OnlineShoppingCommodityDao onlineShoppingCommodityDao;

    public void createHTML(long commodityId) throws FileNotFoundException {
        PrintWriter writer = null;
        OnlineShoppingCommodity onlineShoppingCommodity = onlineShoppingCommodityDao.selectByCommodityId(commodityId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("commodity", onlineShoppingCommodity);

        Context context = new Context();
        context.setVariables(resultMap);
        File file = new File("src/main/resources/templates",
                "item_detail_" + commodityId + ".html");
        writer = new PrintWriter(file);
        templateEngine.process("item_detail", context, writer);
    }

}
