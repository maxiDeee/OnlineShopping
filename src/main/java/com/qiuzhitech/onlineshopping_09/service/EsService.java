package com.qiuzhitech.onlineshopping_09.service;

import com.alibaba.fastjson.JSON;
import com.qiuzhitech.onlineshopping_09.db.po.OnlineShoppingCommodity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service
@Slf4j
public class EsService {
    public static final String COMMODITY_INDEX = "commodity";

    @Resource
    RestHighLevelClient esClient;
    // insert
    public int insertCommodity(OnlineShoppingCommodity commodity) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest(COMMODITY_INDEX);

        boolean exists = esClient.indices().exists(
                getIndexRequest, RequestOptions.DEFAULT);

        if (!exists) {
            // create Index first
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject()
                    .startObject("dynamic_templates")
                    .startObject("strings")
                    .field("match_mapping_type", "string")
                    .startObject("mapping")
                    .field("type", "text")
                    .field("analyzer", "ik_smart")
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject();
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(COMMODITY_INDEX).source(builder);
            CreateIndexResponse createIndexResponse = esClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (!createIndexResponse.isAcknowledged()) {
                log.error("Create index failed");
                return RestStatus.INTERNAL_SERVER_ERROR.getStatus();
            }

        }
        String jsonString = JSON.toJSONString(commodity);
        IndexRequest indexRequest = new IndexRequest(COMMODITY_INDEX)
                .source(jsonString, XContentType.JSON)
                .id(commodity.getCommodityId().toString());
        IndexResponse response = esClient.index(indexRequest, RequestOptions.DEFAULT);
        log.info("AddCommodity to Es, commodityId={}", commodity.getCommodityId());
        return response.status().getStatus();
    }

    // select/query
    public List<OnlineShoppingCommodity> searchCommodity(String  keyword, int from, int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(COMMODITY_INDEX);
        List<OnlineShoppingCommodity> res = new ArrayList<>();
        // search filter
        MultiMatchQueryBuilder multiMatchQueryBuilder = multiMatchQuery(keyword,"commodityName", "commodityDesc");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder
                .from(from)
                .size(size)
                .query(multiMatchQueryBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits()) {
            String sourceJSON = hit.getSourceAsString();
            OnlineShoppingCommodity onlineShoppingCommodity = JSON.parseObject(sourceJSON, OnlineShoppingCommodity.class);
            res.add(onlineShoppingCommodity);
        }
        return res;
    }
}