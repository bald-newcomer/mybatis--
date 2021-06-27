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
package org.apache.ibatis.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultReflectorFactory implements ReflectorFactory {
    private boolean classCacheEnabled = true;
    private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<Class<?>, Reflector>();

    public DefaultReflectorFactory() {
    }

    @Override
    public boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    @Override
    public void setClassCacheEnabled(boolean classCacheEnabled) {
        this.classCacheEnabled = classCacheEnabled;
    }

    /**
     * 反射获取对象
     * <p>
     * 首先在这个工厂中存储着一个cache和一个classCacheEnabled，表明类缓存是否可以获取，表明是否可以使用缓存
     * <p>
     * 如果可以进行缓存，去缓存中获取类对象对应的Reflector
     * 如果不可以进行缓存，则直接new对象就是
     */
    @Override
    public Reflector findForClass(Class<?> type) {
        if (classCacheEnabled) {
            // synchronized (type) removed see issue #461
            Reflector cached = reflectorMap.get(type);
            if (cached == null) {
                cached = new Reflector(type);
                reflectorMap.put(type, cached);
            }
            return cached;
        } else {
            return new Reflector(type);
        }
    }

}
