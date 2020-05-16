package com.study.redis.hyperloglog;

import redis.clients.jedis.Jedis;

/**
 * 测试HyperLogLog的误差
 */
public class HyperLogLogErrTest {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("server01", 6379);
        int expect_total = 100000;
        for (int i = 0; i < expect_total; i++) {
            jedis.pfadd("UV", "user" + i);
        }
        long total = jedis.pfcount("UV");
        System.out.println(String.format("expect total:%d, actual total:%d", expect_total, total));
        jedis.close();
    }
}
