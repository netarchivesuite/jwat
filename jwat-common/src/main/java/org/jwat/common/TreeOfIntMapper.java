/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwat.common;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TreeOfIntMapper<T> {

    protected static class IntItem<T> {
        protected int key;
        protected List<IntItem<T>> itemList;
        protected T value;
    }

    protected IntItem<T> rootItem = new IntItem<>();

    protected int size = 0;

    public int size() {
        return size;
    }

    public void add(T value, int[] arr) {
        int arrIdx = 0;
        int arrLen = arr.length;
        int arrKey;
        IntItem<T> item = rootItem;
        List<IntItem<T>> itemList;
        int listIdx;
        int listLen;
        while (arrIdx < arrLen) {
            itemList = item.itemList;
            if (itemList == null) {
                itemList = new LinkedList<>();
                item.itemList = itemList;
            }
            arrKey = arr[arrIdx++];
            listIdx = 0;
            listLen = itemList.size();
            item = null;
            while (listIdx < listLen && (item = itemList.get(listIdx)).key < arrKey) {
                ++listIdx;
            }
            if (item == null || item.key != arrKey) {
                item = new IntItem<>();
                item.key = arrKey;
                itemList.add(listIdx, item);
            }
        }
        item.value = value;
        ++size;
    }

    public T lookup(int[] arr) {
        int arrIdx = 0;
        int arrLen = arr.length;
        int arrKey;
        IntItem<T> item = rootItem;
        List<IntItem<T>> itemList;
        int listIdx;
        int listLen;
        T value = null;
        while (item != null && arrIdx < arrLen) {
            itemList = item.itemList;
            item = null;
            if (itemList != null) {
                arrKey = arr[arrIdx++];
                listIdx = 0;
                listLen = itemList.size();
                while (listIdx < listLen && (item = itemList.get(listIdx)).key < arrKey) {
                    ++listIdx;
                }
                if (item != null && item.key != arrKey) {
                    item = null;
                }
            }
        }
        if (item != null) {
            value = item.value;
        }
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<IntItem<T>> iter = rootItem.itemList.iterator();
        IntItem<T> tmpListItem;
        List<IntItem<T>> itemList;
        Deque<Iterator<IntItem<T>>> queue = new ArrayDeque<>();
        queue.push(iter);
        String indent = "  ";
        while (queue.size() > 0) {
            iter = queue.pop();
            indent = indent.substring(2);
            while (iter.hasNext()) {
                tmpListItem = iter.next();
                if (tmpListItem.value == null) {
                    sb.append(indent + tmpListItem.key);
                }
                else {
                    sb.append(indent + tmpListItem.key + " " + tmpListItem.value);
                }
                sb.append("\n");
                itemList = tmpListItem.itemList;
                if (itemList != null) {
                    queue.push(iter);
                    iter = itemList.iterator();
                    indent = indent + "  ";
                }
            }
        }
        return sb.toString();
    }

}
