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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;

/**
 * Lru (least recently used) cache decorator
 * <p>
 * 最少最近使用原则
 * 当一组数据在最近一段时间内没有被访问到，其在将来被访问的可能也很低。也就是说，在空间满的时候，将最久未被使用的元素淘汰
 *
 * @author Clinton Begin
 */
public class LruCache implements Cache {

    private final Cache delegate;
    private Map<Object, Object> keyMap;
    private Object eldestKey;

    public LruCache(Cache delegate) {
        this.delegate = delegate;
        setSize(1024);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void setSize(final int size) {
        /**
         * 初始化keyMap，默认长度1024，且使用LinkedHashMap todo linkedHashMap的结构与好处
         * 这里要注意accessOrde属性：即按照访问顺序进行排序，即无论是对LinkedHashMap的访问还是插入，都会将被指向的节点放到尾部
         * 这是实现lru算法的一个很重要的关键
         * 重写removeEldestEntry，只进行存储和反馈是否超长，在尾部的元素，一定是最不经常被访问的元素
         *
         * size() > size ，表明由于.75F，该map早就扩容了，只是认为固定大小到这个点的时候，就需要记录最少被使用的元素，在下次put的时候进行删除
         *
         * */
        keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
            private static final long serialVersionUID = 4267176411845948333L;

            //重写去除最老元素的方法
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                //当前长度超过最大长度，则将最老的元素存入 eldestKey的key中，返回是否超长的标志
                boolean tooBig = size() > size;
                if (tooBig) {
                    eldestKey = eldest.getKey();
                }
                return tooBig;
            }
        };
    }

    @Override
    public void putObject(Object key, Object value) {
        delegate.putObject(key, value);
        cycleKeyList(key);
    }

    @Override
    public Object getObject(Object key) {
        keyMap.get(key); //touch
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyMap.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /**
     * 向初始化的LinkedList中存值，如果eldestKey不为空，删除
     * <p>
     * 采用LinkedList来实现lru算法，淘汰最近的最少使用的对象
     */
    private void cycleKeyList(Object key) {
        keyMap.put(key, key);
        if (eldestKey != null) {
            delegate.removeObject(eldestKey);
            eldestKey = null;
        }
    }

}
