package com.qiuzhitech.onlineshopping_09.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESConfig {

    @Bean
    public RestHighLevelClient RestHighLevelClientProvider() {
        HttpHost httpHost = new HttpHost("localhost", 9200, "http");
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }

}
