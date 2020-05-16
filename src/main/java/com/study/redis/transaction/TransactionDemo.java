package com.study.redis.transaction;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

public class TransactionDemo {

    public static String keyFor(String userId) {
        return String.format("account_%s", userId);
    }

    public static int doubleAccount(Jedis jedis, String userId) {
        String key = keyFor(userId);
        while (true) {
            // watch实现乐观锁，若key在执行事务期间，已发送改变，则事务执行失败
            jedis.watch(key);
            int value = Integer.parseInt(jedis.get(key));
            value *= 2;
            Transaction tx = jedis.multi();
            tx.set(key, String.valueOf(value));
            List<Object> res = tx.exec();
            if (res != null) {
                //执行成功
                break;
            }
        }
        return Integer.parseInt(jedis.get(key));
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("server01", 6379);
        String userId = "abc";
        String key = keyFor(userId);
        jedis.del(key);
        jedis.setnx(key, String.valueOf(5));
        System.out.println(doubleAccount(jedis, userId));
        jedis.close();
    }
}
