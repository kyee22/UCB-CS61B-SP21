package bstmap;

import java.security.Key;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private int size;
    private BSTNode root;
    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left;
        private BSTNode right;

        public BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public BSTNode getLeft() {
            return left;
        }

        public BSTNode getRight() {
            return right;
        }

        public void setLeft(BSTNode left) {
            this.left = left;
        }

        public void setRight(BSTNode right) {
            this.right = right;
        }
    }

    public BSTMap() {
        size = 0;
        root = null;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        //return get(key) != null; // if value is exactly null, this is wrong!!
        return root != null && find(root, key) != null;
    }

    @Override
    public V get(K key) {
        if (root == null) {
            //throw new NullPointerException("Not found that key!");
            return null;
        }

        BSTNode node = find(root, key);
        if (node == null) {
            //throw new NullPointerException("Not found that key!");
            return null;
        }

        return node.getValue();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new BSTNode(key, value);
            size += 1;
            return;
        }

        insert(root, key, value);
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Unimplemented!!");
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("Unimplemented!!");
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("Unimplemented!!");
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("Unimplemented!!");
    }

    private BSTNode find(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        if (node.getKey().equals(key)) {
            return node;
        } else if (node.getKey().compareTo(key) < 0) {
            return find(node.getLeft(), key);
        } else {
            return find(node.getRight(), key);
        }
    }

    private BSTNode insert(BSTNode node, K key, V value) {
        if (node == null) {
            size += 1;
            return new BSTNode(key, value);
        }

        if (node.getKey().compareTo(key) < 0) {
            node.setLeft(insert(node.getLeft(), key, value));
        } else if (node.getKey().compareTo(key) > 0) {
            node.setRight(insert(node.getRight(), key, value));
        }

        return node;
    }

}
