package com.study.redis.queue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * 实现延时队列
 * 将消息序列化成一个字符串作为zset的value，到期时间作为score
 * 多个线程轮询zset获取到期的任务进行处理
 *
 * 进一步优化：使用lua脚本将zrangebyscore和zrem挪到服务器端进行原子化操作
 */
public class RedisDelayQueue<T> {

    static class TaskItem<T> {
        public String id;
        public T msg;
    }

    private Type taskType = new TypeReference<TaskItem<T>>(){}.getType();
    private Jedis jedis;
    private String queueKey;

    public RedisDelayQueue(Jedis jedis, String queueKey) {
        this.jedis = jedis;
        this.queueKey = queueKey;
    }

    // 向延时队列添加元素
    public void delay(T msg, long delayTime) {
        TaskItem task = new TaskItem();
        task.id = UUID.randomUUID().toString();
        task.msg = msg;
        String s = JSON.toJSONString(task);
        // 已过期时间作为zset的score
        jedis.zadd(queueKey, System.currentTimeMillis() + delayTime, s);
    }

    public void loop() {
        while (!Thread.interrupted()) {
            // 取出已到期的元素中的第一个，score∈[0,currentTime]
            Set<String> values = jedis.zrangeByScore(
                    queueKey,
                    0,
                    System.currentTimeMillis(),
                    0,
                    1);

            // 队列为空，则sleep，避免消耗CPU
            if (values.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }

            String s = values.iterator().next();
            // 从队列中移除元素成功后进行处理
            if (jedis.zrem(queueKey, s) > 0) {
                TaskItem<T> task = JSON.parseObject(s, taskType);
                handleMsg(task.msg);
            }
        }
    }

    public void handleMsg(T msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) throws InterruptedException {
        Jedis jedis = new Jedis("server01", 6379);
        RedisDelayQueue<String> queue = new RedisDelayQueue<>(jedis, "queue-demo");

        Thread producer = new Thread(){
            public void run() {
                for (int i = 0; i < 10; i++) {
                    long delayTIme = new Random().nextInt(10);
                    queue.delay("delay_test_" + i + "_" + delayTIme, delayTIme * 1000);
                }
            }
        };

        Thread consumer = new Thread(){
            public void run() {
                queue.loop();
            }
        };

        producer.start();
        consumer.start();
        producer.join();
        Thread.sleep(15000);
        consumer.interrupt();
        consumer.join();
        jedis.close();
    }
}
