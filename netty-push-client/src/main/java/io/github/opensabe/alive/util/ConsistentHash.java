/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.alive.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * 一致性hash算法
 *
 * @author bjzhangyu
 */
public class ConsistentHash<T> {

    /**
     * 圈大小
     */
    private static final int circleSize = 188833;
    /**
     * 虚节点数量
     */
    private final int numberOfReplicas;
    /**
     * 虚节点和节点映射
     */
    private volatile TreeMap<Integer, List<T>> circle = new TreeMap<>();

    //	private static final int circleSize = 18;

    /**
     * @param numberOfReplicas 虚节点数
     * @param nodes            节点数
     */
    public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            addNode(circle, node);
        }

    }

    private static int hashCode(byte[] bytes) {
        int hash = 0;
        for (byte b : bytes) {
            hash = hash * 31 + ((int) b & 0xFF);
            if (hash > 0x4000000) {
                hash = hash % 0x4000000;
            }
        }
        return hash;
    }

    /**
     * 获取0 到 (size-1)的一个随机数
     *
     * @param size 范围
     * @return 返回 0到(size-1)的一个随机数
     */
    public static int getIndex(int size) {
        return ((int) (Math.random() * 100)) % size;
    }

    public synchronized void add(T node) {
        TreeMap<Integer, List<T>> newCircle = copyCircle();
        addNode(newCircle, node);
        this.circle = newCircle;
    }

    /**
     * 删除节点
     *
     * @param node 要删除的节点
     */
    public synchronized void remove(T node) {
        TreeMap<Integer, List<T>> newCircle = copyCircle();
        remove(newCircle, node);
        this.circle = newCircle;
    }

    private TreeMap<Integer, List<T>> copyCircle() {
        TreeMap<Integer, List<T>> newTree = new TreeMap<>();

        for (Map.Entry<Integer, List<T>> entry : circle.entrySet()) {
            List<T> list = new ArrayList<T>();
            list.addAll(entry.getValue());
            newTree.put(entry.getKey(), list);
        }
        return newTree;
    }

    /**
     * 添加节点
     *
     * @param node 添加的节点
     */
    private void addNode(TreeMap<Integer, List<T>> circle, T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            // 节点与虚拟节点映射关系
            int key = hashMd5(node.toString() + i);
            List<T> list = circle.get(key);
            if (list == null) {
                list = new ArrayList<T>();
                circle.put(key, list);
            }
            if (!containsNode(list, node)) {
                list.add(node);
            }
        }
    }

    private void removeNodeToList(List<T> list, T node) {
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (node.equals(it.next())) {
                it.remove();
            }
        }
    }

    private boolean containsNode(List<T> list, T node) {
        for (T t : list) {
            if (t.equals(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除节点
     *
     * @param node 要删除的节点
     */
    private void remove(TreeMap<Integer, List<T>> circle, T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            // 节点与虚拟节点映射关系
            int key = hashMd5(node.toString() + i);
            List<T> list = circle.get(key);
            if (list != null) {
                if (list.contains(node)) {
                    removeNodeToList(list, node);
                }
                if (list.isEmpty()) {
                    circle.remove(key);
                }
            }
        }
    }

    /**
     * 得到key对应节点
     *
     * @param key 节点key
     * @return 返回该节点
     */
    public T get(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        // 对key的特征进行映射，hash值为虚节点
        int hash = hashMd5(key);
        // 找到对应节点
        Map.Entry<Integer, List<T>> entry = circle.ceilingEntry(hash);
        List<T> node = null;
        if (entry == null) {//空就代表需要回环了

            node = circle.firstEntry().getValue();
        } else {
            node = entry.getValue();
        }

        if (node != null && !node.isEmpty()) {
            return node.get(0);
        }
        return null;
    }

    /**
     * md5映射计算
     *
     * @param o 计算md5的对象
     * @return md5值
     */
    private int hashMd5(Object o) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(o.toString().getBytes());
            int hashCode = hashCode(bytes);
            return hashCode % circleSize;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }

}