package com.qiuzhitech.onlineshopping_09.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.qiuzhitech.onlineshopping_09.db.dao.OnlineShoppingCommodityDao;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import com.qiuzhitech.onlineshopping_09.service.EsService;
import com.qiuzhitech.onlineshopping_09.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class CommodityController {
    @Resource
    private OnlineShoppingCommodityDao onlineShoppingCommodityDao;

    @Resource
    private SearchService searchService;

    @Resource
    private EsService esService;

    @RequestMapping("addItem")
    public String AddCommodity() {
        return "add_commodity";
    }

    @PostMapping("/commodities")
    public String HandleAddCommodity(@RequestParam("commodityId") long commodityId,
                               @RequestParam("commodityName") String commodityName,
                               @RequestParam("commodityDesc") String commodityDesc,
                               @RequestParam("price") int price,
                               @RequestParam("creatorUserId") long creatorUserId,
                               @RequestParam("availableStock") int availableStock,
                               Map<String, Object> resultMap) throws IOException {
        OnlineShoppingCommodity commodity = OnlineShoppingCommodity.builder()
                .commodityId(commodityId)
                .commodityName(commodityName)
                .commodityDesc(commodityDesc)
                .price(price)
                .creatorUserId(creatorUserId)
                .availableStock(availableStock)
                .totalStock(availableStock)
                .lockStock(0)
                .build();
        int ret = onlineShoppingCommodityDao.insertCommodity(commodity);
        esService.insertCommodity(commodity);
        resultMap.put("abc", commodity);
        return "add_commodity_success";
    }

    @GetMapping("/")
    public String GetAllCommodities(Map<String, Object> resultMap){
        List<OnlineShoppingCommodity> onlineShoppingCommodities = onlineShoppingCommodityDao.listItems();
        resultMap.put("itemList", onlineShoppingCommodities);
        return "list_items";
    }

    @GetMapping("/commodities/{sellerId}")
    public String GetCommoditiesForSeller(@PathVariable("sellerId") long sellerId,
                                    Map<String, Object> resultMap){
        try (Entry entry = SphU.entry("ListItemRule", EntryType.IN, 1, sellerId)) {
            List<OnlineShoppingCommodity> onlineShoppingCommodities = onlineShoppingCommodityDao.selectByUserId(sellerId);
            resultMap.put("itemList", onlineShoppingCommodities);
            return "list_items";
        } catch (BlockException e) {
            log.error("ListItems got throttled {}", e.toString());
            return "wait";
        }
    }

    @GetMapping("/item/{commodityId}")
    public String GetCommodityDetail(@PathVariable("commodityId") long commodityId,
                                          Map<String, Object> resultMap){
        OnlineShoppingCommodity onlineShoppingCommodity = onlineShoppingCommodityDao.selectByCommodityId(commodityId);
        resultMap.put("commodity", onlineShoppingCommodity);
        return "item_detail";
    }

    @GetMapping("/searchAction")
    public String searchCommodity(@RequestParam("keyWord") String keyWord,
                                Map<String, Object> resultMap) throws IOException {
        //List<OnlineShoppingCommodity> onlineShoppingCommodities = searchService.searchCommodityDB(keyWord);
        List<OnlineShoppingCommodity> onlineShoppingCommodities = searchService.searchCommodityES(keyWord);
        resultMap.put("itemList", onlineShoppingCommodities);
        return "list_items";
    }

    @PostConstruct
    public void CommodityControllerFlow() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();

        // ListItem 限流规则
        rule.setResource("ListItemRule");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        rules.add(rule);

//        // ItemDetail 限流规则
//        rule.setResource("ItemDetailRule");
//        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule.setCount(1);
//        rules.add(rule);

        FlowRuleManager.loadRules(rules);
    }
}
