package com.study.redis.bloom;

import io.rebloom.client.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 试布隆过滤器错误率
 */
public class BloomErrRateTest {

    private String chars;
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            builder.append((char)('a' + i));
        }
        chars = builder.toString();
    }

    private String randomString(int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i < n; i++) {
            int idx = ThreadLocalRandom.current().nextInt(chars.length());
            builder.append(chars.charAt(idx));
        }
        return builder.toString();
    }

    private List<String> randomUsers(int n) {
        List<String> users = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            users.add(randomString(64));
        }
        return users;
    }

    public static void main(String[] args) {
        BloomErrRateTest bloomer = new BloomErrRateTest();
        List<String> users = bloomer.randomUsers(100000);
        List<String> usersTrain = users.subList(0, users.size() / 2);
        List<String> usersTest = users.subList(users.size() / 2, users.size());

        Client client = new Client("server01", 6379);
        String key = "bloom_test";
        client.delete(key);

        client.createFilter(key, 50000, 0.001);
        for (String user: usersTrain) {
            client.add(key, user);
        }

        int err = 0;
        for (String user: usersTest) {
            boolean ret = client.exists(key, user);
            if (ret) {
                err++;
            }
        }
        System.out.println(String.format(
                "total test: %d; err: %d; err_rate: %f",
                usersTest.size(),
                err,
                err/(float)usersTest.size()));
        client.close();
    }
}
