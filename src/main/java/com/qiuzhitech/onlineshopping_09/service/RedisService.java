package com.qiuzhitech.onlineshopping_09.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
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

    public Map<String, String> getMapValue(String key) {
        Jedis jedis = jedisPool.getResource();
        Map<String, String> res = jedis.hgetAll(key);
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
        jedis.close();
        return stock;
    }

    public long revertStock(String redisKey) {
        Jedis jedis = jedisPool.getResource();
        long incr = jedis.incr(redisKey);
        jedis.close();
        return incr;
    }

    public boolean tryToGetDistributeLock(String commodityId, String requestId,  int timeoutInMillSeconds) {
        Jedis jedis = jedisPool.getResource();
        String redisKey = "commodityLock:" + commodityId;
        String ret = jedis.set(redisKey, requestId, "NX", "PX", timeoutInMillSeconds);
        jedis.close();
        return ("OK").equals(ret);              // NPE prevent
    }

    public boolean releaseDistributedLock(String commodityId, String requestId) {
        Jedis jedis = jedisPool.getResource();
        String redisKey = "commodityLock:" + commodityId;
        // Atomically compare requestId and delete — prevents releasing a lock owned by another request
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call('del', KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
        Long result = (Long) jedis.eval(script,
                Collections.singletonList(redisKey),
                Collections.singletonList(requestId));
        jedis.close();
        return result == 1L;        // 1: release succeed   0: release fail (key invalid or value doesn't exist)
    }

    public void addToDenyList(String userId, String commodityId) {
        Jedis jedis = jedisPool.getResource();
        String redisKey = "online_shopping:denyListUserId:" + userId;
        jedis.sadd(redisKey, commodityId);
        jedis.close();
        log.info("Add userId: {} into denyList for commodityId: {}", userId, commodityId);
    }

    public void removeFromDenyList(String userId, String commodityId) {
        Jedis jedis = jedisPool.getResource();
        String redisKey = "online_shopping:denyListUserId:" + userId;
        jedis.srem(redisKey, commodityId);
        jedis.close();
        log.info("Remove userId: {} from denyList for commodityId: {}", userId, commodityId);
    }

    public boolean isInDenyList(String userId, String commodityId) {
        Jedis jedis = jedisPool.getResource();
        String redisKey = "online_shopping:denyListUserId:" + userId;
        Boolean res = jedis.sismember(redisKey, commodityId);
        jedis.close();
        return res;
    }
}
