package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }

        private K getKey() {
            return key;
        }

        private V getValue() {
            return value;
        }

        private void setValue(V value) {
            this.value = value;
        }
    }

    private static final int DEFAULT_SIZE = 16;
    private static final double MAX_LF = 0.75;

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private double maxLoad;
    private int size;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        this(DEFAULT_SIZE);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, MAX_LF);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.buckets = createTable(initialSize);
        this.maxLoad = maxLoad;
        this.size = 0;
    }

    private int loadFactor() {
        return size / buckets.length;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // DONE: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        size = 0;
        for (Collection<Node> bucket : buckets) {
            if (bucket != null) {
                bucket.clear();
            }
        }
    }

    @Override
    public boolean containsKey(K key) {
        int hashCode = (key.hashCode() & 0x7fffffff) % buckets.length;
        if (buckets[hashCode] == null) {
            return false;
        }

        Iterator<Node> iterator = buckets[hashCode].iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public V get(K key) {
        int hashCode = (key.hashCode() & 0x7fffffff) % buckets.length;
        if (buckets[hashCode] == null) {
            return null;
        }

        Iterator<Node> iterator = buckets[hashCode].iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.getKey().equals(key)) {
                return node.getValue();
            }
        }

        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int hashCode = (key.hashCode() & 0x7fffffff) % buckets.length;

        if (buckets[hashCode] == null) {
            buckets[hashCode] = createBucket();
        }

        Iterator<Node> iterator = buckets[hashCode].iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.getKey().equals(key)) {
                node.setValue(value);
                return;
            }
        }

        buckets[hashCode].add(new Node(key, value));
        size += 1;
    }

    @Override
    public Set<K> keySet() {
        Set<K> result = new HashSet<>();

        for (Collection<Node> bucket : buckets) {
            if (bucket != null) {
                Iterator<Node> iterator = bucket.iterator();
                while (iterator.hasNext()) {
                    Node node = iterator.next();
                    result.add(node.getKey());
                }
            }
        }

        return result;
    }

    @Override
    public V remove(K key) {
        //throw new UnsupportedOperationException("Unimplemented!!");
        int hashCode = (key.hashCode() & 0x7fffffff) % buckets.length;
        if (buckets[hashCode] == null) {
            return null;
        }

        Iterator<Node> iterator = buckets[hashCode].iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.getKey().equals(key)) {
                buckets[hashCode].remove(node);
                return node.getValue();
            }
        }

        return null;
    }

    @Override
    public V remove(K key, V value) {
        //throw new UnsupportedOperationException("Unimplemented!!");
        int hashCode = (key.hashCode() & 0x7fffffff) % buckets.length;
        if (buckets[hashCode] == null) {
            return null;
        }

        Iterator<Node> iterator = buckets[hashCode].iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.getKey().equals(key)) {
                if (!node.getValue().equals(value)) {
                    return null;
                }
                buckets[hashCode].remove(node);
                return node.getValue();
            }
        }

        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    private class MyHashMapIterator implements Iterator<K> {
        private Iterator<K> iter;

        public MyHashMapIterator() {
            iter = keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public K next() {
            return iter.next();
        }
    }
}
