package com.study.redis.pubsub;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class PubsubConsumer extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if ("quit".equalsIgnoreCase(message)) {
            this.unsubscribe(channel);
        }
        System.out.println(String.format("%s: %s", channel, message));
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        System.out.println("unsubscribe: " + channel);
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("server01", 6379);
        jedis.subscribe(new PubsubConsumer(), "codehole");
        jedis.close();
    }
}
