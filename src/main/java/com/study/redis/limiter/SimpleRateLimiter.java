package com.study.redis.limiter;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * 滑动窗口实现限流
 */
public class SimpleRateLimiter {

    private Jedis jedis;

    public SimpleRateLimiter(Jedis jedis) {
        this.jedis = jedis;
    }

    public boolean isActionAllowed(String userId, String actionKey, int period, int maxCount) {
        String key = String.format("hist:%s:%s", userId, actionKey);
        long nowTs = System.currentTimeMillis();
        Pipeline pipe = jedis.pipelined();
        pipe.multi();
        // 记录用户本次行为,value无实际作用，确保惟一即可
        pipe.zadd(key, nowTs, "" + nowTs);
        // 删除滑动窗口外的记录
        pipe.zremrangeByScore(key, 0, nowTs - period * 1000);
        // 获取滑动窗口内的记录数
        Response<Long> count = pipe.zcard(key);
        // 设置过期时间，期间没有添加元素，则过期自动删除
        pipe.expire(key, period + 1);
        pipe.exec();
        pipe.clear();
        return count.get() <= maxCount;
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("server01", 6379);
        SimpleRateLimiter limiter = new SimpleRateLimiter(jedis);
        for (int i = 0; i < 20; i++) {
            System.out.println(limiter.isActionAllowed("user", "replay", 60, 5));
        }
    }
}
