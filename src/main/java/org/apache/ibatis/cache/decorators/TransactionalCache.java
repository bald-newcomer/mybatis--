/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * The 2nd level cache transactional buffer.
 * <p>
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session.
 * Entries are sent to the cache when commit is called or discarded if the Session is rolled back.
 * Blocking cache support has been added. Therefore any get() that returns a cache miss
 * will be followed by a put() so any lock associated with the key can be released.
 * <p>
 * <p>
 * 2级事务缓存
 * <p>
 * 一级缓存是entriesToAddOnCommit，二级缓存是delegate中的缓存
 * 在commit后，才将一级缓存中的提交到二级缓存中
 * entriesMissedInCache：看下面的解析，为了避免缓存穿透
 * *
 * <p>
 * entriesMissedInCache主要是用来保存在查询过程中在缓存中没有命中的key，
 * 由于没有命中，说明需要到数据库中查询，那么查询过后会保存到entriesToAddCommit中，
 * 那么假设在事务提交过程中失败了，而此时entriesToAddCommit的数据又都刷新到缓存中了，
 * 那么此时调用rollback就会通过entriesMissedInCache中保存的key，来清理真实缓存，这样就可以保证在事务中缓存数据与数据库的数据保持一致。
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class TransactionalCache implements Cache {

  private static final Log log = LogFactory.getLog(TransactionalCache.class);

  private final Cache delegate;
  private boolean clearOnCommit;
  private final Map<Object, Object> entriesToAddOnCommit;
  private final Set<Object> entriesMissedInCache;

  public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
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
  public Object getObject(Object key) {
    // issue #116
    Object object = delegate.getObject(key);
    //存了下空对象的key，存储的是未命中的key
    if (object == null) {
      entriesMissedInCache.add(key);
    }
    // issue #146
    //这个应该是防止读不一致的问题，在清除功能提交后，所有的get都只会返回空
    if (clearOnCommit) {
      return null;
    } else {
      return object;
    }
  }

  @Override
  public void putObject(Object key, Object object) {
    entriesToAddOnCommit.put(key, object);
  }

  @Override
  public Object removeObject(Object key) {
    return null;
  }

  @Override
  public void clear() {
    clearOnCommit = true;
    entriesToAddOnCommit.clear();
  }

  /**
   * 缓存会在提交后进行清除
   * <p>
   * 调用put方法时，只会将对应的内容存储到缓存中
   * 调用get方法时，获取到的空的key值，会放到另一个缓存中
   */
  public void commit() {
    if (clearOnCommit) {
      delegate.clear();
    }
    flushPendingEntries();
    reset();
  }

  public void rollback() {
    unlockMissedEntries();
    reset();
  }

  private void reset() {
    clearOnCommit = false;
    entriesToAddOnCommit.clear();
    entriesMissedInCache.clear();
  }

  private void flushPendingEntries() {
    //将之前put到commitCache中的元素存入delegate中缓存中
    for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
      delegate.putObject(entry.getKey(), entry.getValue());
    }
    //走一下过程
    //先从缓存中取值
    //没有获取到，数据库查询，并且将未命中的key存储到miss中
    //数据库查询到内容，调用put存入二级缓存中
    //调用提交，将二级缓存中的东西刷到一级缓存中
    //此时，未命中的miss中有这个key，但是二级缓存中没有，这就导致了其实数据库没查到
    //然后如果有大量的查询查询这个key，会突破缓存查询数据库，即为缓存穿透
    //但是如果我们把这些为空的缓存存到一级缓存中，用户可以从一级缓存中获取到这个key，虽然key的内容是空的，但是应该避免了缓存穿透
    for (Object entry : entriesMissedInCache) {
      if (!entriesToAddOnCommit.containsKey(entry)) {
        delegate.putObject(entry, null);
      }
    }
  }

  private void unlockMissedEntries() {
    for (Object entry : entriesMissedInCache) {
      try {
        delegate.removeObject(entry);
      } catch (Exception e) {
        log.warn("Unexpected exception while notifying a rollback to the cache adapter. "
          + "Consider upgrading your cache adapter to the latest version. Cause: " + e);
      }
    }
  }

}
