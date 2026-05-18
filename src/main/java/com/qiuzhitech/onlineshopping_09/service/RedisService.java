package com.qiuzhitech.onlineshopping_09.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class RedisService {
    @Resource
    JedisPool jedisPool;

    public String getValue(String key) {
        Jedis jedis = jedisPool.getResource();
        String res = jedis.get(key);
        jedis.close();
        return res;
    }

    public String setValue(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        String res = jedis.set(key, value);
        jedis.close();
        return res;
    }

    public String setMapValue(String key,
                              Map<String, String> value) {
        Jedis jedis = jedisPool.getResource();
        String res = jedis.hmset(key, value);
        jedis.close();
        return res;
    }

    public List<String> getListValue(String key) {
        Jedis jedis = jedisPool.getResource();
        List<String> res = jedis.hmget(key);
        jedis.close();
        return res;
    }

    public long deductStock(String redisKey) {
        Jedis jedis = jedisPool.getResource();
        String script =
                "if redis.call('exists', KEYS[1]) == 1 then\n" +
                    " local stock = tonumber(redis.call('get', KEYS[1]))\n" +
                    "   if (stock<=0) then\n" +
                    "       return -1\n" +
                    "   end\n" +
                    "\n" +
                    "   redis.call('decr', KEYS[1]);\n" +
                    "   return stock - 1;\n" +
                    "end\n" +
                    "\n" +
                    "return -1;";
        Long stock = -1L;
        stock = (Long)jedis.eval(script, Collections.singletonList(redisKey), Collections.emptyList());
        return stock;
    }
}
