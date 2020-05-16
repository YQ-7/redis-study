package com.study.redis.limiter;

import java.util.HashMap;
import java.util.Map;

/**
 * 单机版漏斗限流算法
 * reids限流模块redis-cell实现了次算法
 */
public class FunnelRateLimiter {

    static class Funnel {
        int capacity;       // 漏斗容量
        float leakingRate;  // 溜嘴流水速率
        int leftQuota;      // 剩余容量
        long leakingTs;     // 上一次漏水时间

        public Funnel(int capacity, float leakingRate) {
            this.capacity = capacity;
            this.leakingRate = leakingRate;
            this.leftQuota = capacity;
            this.leakingTs = System.currentTimeMillis();
        }

        // 释放空间
        void makeSpace() {
            long nowTs = System.currentTimeMillis();
            long releaseTs = nowTs - leakingTs;
            int releaseQuota = (int)(releaseTs * leakingRate);

            // 尽快时间太长，整数过大溢出
            if (releaseQuota < 0) {
                this.leftQuota = capacity;
                this.leakingTs = nowTs;
                return;
            }

            // 腾出空间过小，最小单位是1
            if (releaseQuota < 1) {
                return;
            }

            // 增加剩余空间
            this.leftQuota += releaseQuota;
            this.leakingTs = nowTs;
            // 剩余空间超出最大容量时，重置为最大容量
            if (this.leftQuota > this.capacity) {
                this.leftQuota = this.capacity;
            }
        }

        // 申请容量
        boolean watering(int quota) {
            makeSpace();
            if (this.leftQuota >= quota) {
                this.leftQuota -= quota;
                return true;
            }
            return false;
        }
    }

    private Map<String, Funnel> funnels = new HashMap<>();

    public boolean isActionAllowed(String userId, String actionKey, int capacity, float leadingRate) {
        String key = String.format("hist:%s:%s", userId, actionKey);
        Funnel funnel = funnels.get(key);
        if (funnel == null) {
            funnel = new Funnel(capacity, leadingRate);
            funnels.put(key, funnel);
        }
        return  funnel.watering(1);
    }

    public static void main(String[] args) {
        FunnelRateLimiter limiter = new FunnelRateLimiter();
        for (int i = 0; i < 20; i++) {
            System.out.println(limiter.isActionAllowed("user", "replay", 15, 0.5f));
        }
    }
}
