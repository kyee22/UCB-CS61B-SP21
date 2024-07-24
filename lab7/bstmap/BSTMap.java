package bstmap;

import java.security.Key;
import java.util.*;

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
        //throw new UnsupportedOperationException("Unimplemented!!");
        return getKeysAsSet(root);
    }

    @Override
    public V remove(K key) {
        //throw new UnsupportedOperationException("Unimplemented!!");
        return doRemove(null, root, false, key);
    }

    @Override
    public V remove(K key, V value) {
        //throw new UnsupportedOperationException("Unimplemented!!");
        return doRemove(null, root, false, key, value);
    }



    @Override
    public Iterator<K> iterator() {
        //throw new UnsupportedOperationException("Unimplemented!!");
        return new BSTMapIterator();
    }

    private BSTNode find(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        if (node.getKey().equals(key)) {
            return node;
        } else if (node.getKey().compareTo(key) > 0) {
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

        if (node.getKey().compareTo(key) > 0) {
            node.setLeft(insert(node.getLeft(), key, value));
        } else if (node.getKey().compareTo(key) < 0) {
            node.setRight(insert(node.getRight(), key, value));
        }

        return node;
    }

    public void printInOrder() {
        System.out.println("==============BSTMap================");
        for (K key : getKeysAsList(root)) {
            System.out.println("(" + key + ", " + get(key) + ")");
        }
        System.out.println("====================================");
    }

    private class BSTMapIterator implements Iterator<K> {
        private Iterator<K> keyIter;
        public BSTMapIterator() {
            keyIter = keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return keyIter.hasNext();
        }

        @Override
        public K next() {
            return keyIter.next();
        }
    }

    private Set<K> getKeysAsSet(BSTNode node) {
        if (node == null) {
            return new HashSet<>();
        }

        Set<K> result = new HashSet<>();
        result.add(node.getKey());

        result.addAll(getKeysAsSet(node.getLeft()));
        result.addAll(getKeysAsSet(node.getRight()));

        return result;
    }

    private List<K> getKeysAsList(BSTNode node) {
        if (node == null) {
            return new ArrayList<>();
        }

        List<K> result = new ArrayList<>();
        result.addAll(getKeysAsList(node.getLeft()));
        result.add(node.getKey());
        result.addAll(getKeysAsList(node.getRight()));

        return result;
    }

    private V doRemove(BSTNode parent, BSTNode node, boolean isLeft, K key) {
        if (node == null) {
            return null;
        }

        if (node.getKey().equals(key)) {
            V value = node.getValue();
            doPopNode(parent, node, isLeft);
            return value;
        } else if (node.getKey().compareTo(key) > 0) {
            return doRemove(node, node.getLeft(), true, key);
        } else {
            return doRemove(node, node.getRight(),false, key);
        }
    }

    private V doRemove(BSTNode parent, BSTNode node, boolean isLeft, K key, V givenValue) {
        if (node == null) {
            return null;
        }

        if (node.getKey().equals(key)) {
            V value = node.getValue();
            if (!value.equals(givenValue)) {
                return null;
            }
            doPopNode(parent, node, isLeft);
            return value;
        } else if (node.getKey().compareTo(key) > 0) {
            return doRemove(node, node.getLeft(), true, key);
        } else {
            return doRemove(node, node.getRight(),false, key);
        }
    }


    private void doPopNode(BSTNode parent, BSTNode node, boolean isLeft) {
        BSTNode left = node.getLeft(), right = node.getRight();
        size -= 1;

        if (left == null && right == null) {
            if (parent == null) {
                root = null;
            } else if (isLeft) {
                parent.setLeft(null);
            } else {
                parent.setRight(null);
            }
        } else if (left != null && right == null) {
            if (parent == null) {
                root = node.getLeft();
            } else if (isLeft) {
                parent.setLeft(node.getLeft());
            } else {
                parent.setRight(node.getLeft());
            }
        } else if (left == null && right != null) {
            if (parent == null) {
                root = node.getRight();
            } else if (isLeft) {
                parent.setLeft(node.getRight());
            } else {
                parent.setRight(node.getRight());
            }
        } else {
            BSTNode rightmost = popRightMost(node, node.getLeft(), true);
            rightmost.setLeft(node.getLeft());
            rightmost.setRight(node.getRight());

            if (parent == null) {
                root = rightmost;
            } else if (isLeft) {
                parent.setLeft(rightmost);
            } else {
                parent.setRight(rightmost);
            }
        }
    }

    private BSTNode popRightMost(BSTNode parent, BSTNode node, boolean firstStep) {
        if (node.getRight() == null) {
            if (firstStep) { // 第一步是往左下方找
                parent.setLeft(node.getLeft());
            } else {        // 剩下的都是往右下方找
                parent.setRight(node.getLeft());
            }
            return node;
        }

        return popRightMost(node, node.getRight(), false);
    }

}
