package com.study.redis.pool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisPool {
    private JedisPool pool;

    public RedisPool() {
        this.pool = new JedisPool("server01", 6379);
    }

    public void execute(CallWithJedis caller) {
        Jedis jedis = pool.getResource();
        try {
            caller.call(jedis);
        } catch (JedisConnectionException e) {
            // 重试一次
            caller.call(jedis);
        } finally {
            jedis.close();
        }
    }
}
