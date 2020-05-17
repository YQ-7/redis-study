package com.study.redis.pool;

public class JedisTest {

    static class Holder<T> {
        private T value;

        public Holder() {
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        Holder<Long> countHolder = new Holder<Long>();
        RedisPool redisPool = new RedisPool();
        redisPool.execute(jedis-> {
            Long count = jedis.zcard("user");
            countHolder.setValue(count);
        });
        System.out.println(countHolder.getValue());
    }
}
