package com.study.redis.other;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class BatchAdd {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("server01", 6379);
        Pipeline pipe = jedis.pipelined();
        for (int i = 0; i < 10000; i++) {
            pipe.set("key" + i, "" +i);
        }
        pipe.close();
        jedis.close();
    }
}
