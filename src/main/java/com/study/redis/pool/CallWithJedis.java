package com.study.redis.pool;

import redis.clients.jedis.Jedis;

public interface CallWithJedis {
    void call(Jedis jedis);
}

