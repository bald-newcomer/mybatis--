/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

/**
 * Simple blocking decorator
 * <p>
 * Simple and inefficient version of EhCache's BlockingCache decorator.
 * It sets a lock over a cache key when the element is not found in cache.
 * This way, other threads will wait until this element is filled instead of hitting the database.
 * <p>
 * 阻塞的缓存，在缓存中找不到数据，会一直等待，知道缓存中有数据
 *
 * @author Eduardo Macarron
 */
public class BlockingCache implements Cache {

    private long timeout;
    /**
     * 这个cache是代理实现，我们使用的真实的Cache是其实现类，因此BlockingCache不能单独的存在
     * delegate中，一般的get、set方法都是使用的被代理的真实缓存实现，比如说PerpetualCache
     * 但是我们会在代理的基础上加上一些功能，比如枷锁等等
     */
    private final Cache delegate;
    /**
     * 锁的缓存
     */
    private final ConcurrentHashMap<Object, ReentrantLock> locks;

    /**
     * 唯一的构造方法，也表示，其实例化需要加一个被代理的Cache
     * */
    public BlockingCache(Cache delegate) {
        this.delegate = delegate;
        this.locks = new ConcurrentHashMap<Object, ReentrantLock>();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public void putObject(Object key, Object value) {
        try {
            delegate.putObject(key, value);
        } finally {
            releaseLock(key);
        }
    }

    /**
     * 获取对象值，获取的时候枷锁，获取到了就释放锁，没获取到就锁住该线程
     */
    @Override
    public Object getObject(Object key) {
        acquireLock(key);
        Object value = delegate.getObject(key);
        if (value != null) {
            releaseLock(key);
        }
        return value;
    }

    @Override
    public Object removeObject(Object key) {
        // despite of its name, this method is called only to release locks
        releaseLock(key);
        return null;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    private ReentrantLock getLockForKey(Object key) {
        ReentrantLock lock = new ReentrantLock();
        //如果key或者value有为空的，会抛空指针
        ReentrantLock previous = locks.putIfAbsent(key, lock);
        return previous == null ? lock : previous;
    }

    /**
     * 获取锁，将锁放入缓存
     */
    private void acquireLock(Object key) {
        //创建锁，再和key，一起存入locks中
        Lock lock = getLockForKey(key);
        if (timeout > 0) {
            try {
                //试着枷锁，超时时间为timeout
                boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    throw new CacheException("Couldn't get a lock in " + timeout + " for the key " + key + " at the cache " + delegate.getId());
                }
            } catch (InterruptedException e) {
                throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
            }
        } else {
            //直接枷锁
            lock.lock();
        }
    }

    /**
     * 获取锁，判断该锁是否被当前线程所拥有，若有则直接释放
     */
    private void releaseLock(Object key) {

        ReentrantLock lock = locks.get(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}