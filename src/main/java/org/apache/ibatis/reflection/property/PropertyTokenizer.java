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
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * 属性分词器，基于.的分词
 * <p>
 * 以表达式 obj1.list[0].obj2为例，此时name为obj1，indexedName为obj1，index为null，children为list[0].obj2
 * 具体可以看桌面的哪个图片
 * <p>
 * order[0].item[0].name
 * <p>
 * first: indexName:order[0] name:order index:0
 * second:......
 *
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterable<PropertyTokenizer>, Iterator<PropertyTokenizer> {
    //
    private String name;
    private String indexedName;
    //
    private String index;
    private String children;

    public PropertyTokenizer(String fullname) {
        int delim = fullname.indexOf('.');
        if (delim > -1) {
            //name =obj1
            name = fullname.substring(0, delim);
            //children = list[0].obj2
            children = fullname.substring(delim + 1);
        } else {
            name = fullname;
            children = null;
        }
        //indexedName = obj1
        indexedName = name;
        delim = name.indexOf('[');

        if (delim > -1) {
            //index = null
            index = name.substring(delim + 1, name.length() - 1);
            //name =obj1
            name = name.substring(0, delim);
        }
    }

    public String getName() {
        return name;
    }

    public String getIndex() {
        return index;
    }

    public String getIndexedName() {
        return indexedName;
    }

    public String getChildren() {
        return children;
    }

    @Override
    public boolean hasNext() {
        return children != null;
    }

    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }

    @Override
    public Iterator<PropertyTokenizer> iterator() {
        return this;
    }
}
