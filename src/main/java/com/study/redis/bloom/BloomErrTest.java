package com.study.redis.bloom;

import io.rebloom.client.Client;

/**
 * 测试布隆过滤器，多少次判定会出错
 */
public class BloomErrTest {
    public static void main(String[] args) {
        Client client = new Client("server01", 6379);
        String key = "bloom_test";
        client.delete(key);
        for (int i = 0; i < 100000; i++) {
            client.add(key, "user" + i);
            boolean ret = client.exists(key, "user" + (i + 1));
            if (ret) {
                System.out.println(i);
                break;
            }
        }
        client.close();
    }
}
