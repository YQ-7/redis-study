package com.study.redis.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.Map;

/**
 * 实现可重入的分布式锁
 * 对客户端的set方法进行包装，使用Threadlocal变量存储当前持有锁的计数
 */
public class RedisWithReentrantLock {

    private ThreadLocal<Map> lockers = new ThreadLocal<>();

    private final static int EXPIRE_TIME = 10;

    private Jedis jedis;

    public RedisWithReentrantLock(Jedis jedis) {
        this.jedis = jedis;
    }

    private boolean _lock(String key) {
        SetParams params = new SetParams();
        // 不存在才新增
        params.nx();
        // 设置超时时间
        params.ex(EXPIRE_TIME);
        return jedis.set(key, "", params) != null;
    }

    private void _unlock(String key) {
        jedis.del(key);
    }

    // 获取当前线程持有的锁
    private Map<String, Integer> currentLockers() {
        Map<String, Integer> refs = lockers.get();
        if (refs != null) {
            return refs;
        }
        lockers.set(new HashMap());
        return lockers.get();
    }

    public boolean lock(String key) {
        Map<String, Integer> refs = currentLockers();
        Integer refCnt = refs.get(key);

        // 若已持有锁，自增重入次数
        if (refCnt != null) {
            refs.put(key, refCnt + 1);
            return true;
        }

        // 尝试获取锁
        boolean ok = this._lock(key);
        if (!ok) {
            return false;
        }
        refs.put(key, 1);
        return true;
    }

    public boolean unlock(String key) {
        Map<String, Integer> refs = currentLockers();
        Integer refCnt = refs.get(key);
        // 未持有锁
        if (refCnt == null) {
            return false;
        }

        // 重入次数减1
        refCnt -= 1;

        if (refCnt > 0) {
            refs.put(key, refCnt);
        } else {
            // 重入次数减至0时，释放锁
            refs.remove(key);
            this._unlock(key);
        }
        return true;
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("server01", 6379);
        RedisWithReentrantLock lock = new RedisWithReentrantLock(jedis);
        System.out.println(lock.lock("rn_lock"));
        System.out.println(lock.lock("rn_lock"));
        System.out.println(lock.unlock("rn_lock"));
        System.out.println(lock.unlock("rn_lock"));
        jedis.close();
    }

}
