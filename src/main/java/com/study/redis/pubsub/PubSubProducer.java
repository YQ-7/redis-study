package com.study.redis.pubsub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class PubSubProducer {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("server01", 6379);
        Pipeline pipe = jedis.pipelined();
        String channel = "codehole";
        pipe.publish(channel, "python comes");
        pipe.publish(channel, "java comes");
        pipe.publish(channel, "golang comes");
        pipe.publish(channel, "quit");
        pipe.close();
        jedis.close();
    }
}
