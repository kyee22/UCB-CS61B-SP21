package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    private static class Node<T> {
        private T item;
        private Node<T> next;
        private Node<T> prev;

        public Node(T i) {
            this.item = i;
            this.next = this;
            this.prev = this;
        }

        public void setNext(Node<T> elem) {
            this.next = elem;
        }

        public void setPrev(Node<T> elem) {
            this.prev = elem;
        }

        public Node<T> getNext() {
            return next;
        }

        public Node<T> getPrev() {
            return prev;
        }

        @Override
        public String toString() {
            return this.item.toString();
        }

        public T getItem() {
            return item;
        }
    }

    private int size;
    private Node sentinel;

    public LinkedListDeque() {
        this.size = 0;
        this.sentinel = new Node<>(28332); // sentinel with a magic number
    }

    @Override
    public void addFirst(T item) {
        Node<T> elem = new Node<>(item);

        elem.setNext(sentinel.getNext());

        if (elem.getNext() != sentinel) {
            elem.getNext().setPrev(elem);
        } else {
            sentinel.setPrev(elem);
        }

        elem.setPrev(sentinel);
        sentinel.setNext(elem);

        // do not forget to maintain invariant
        size += 1;
    }

    @Override
    public void addLast(T item) {
        Node<T> elem = new Node<>(item);

        elem.setPrev(sentinel.getPrev());

        if (elem.getPrev() != sentinel) {
            elem.getPrev().setNext(elem);
        } else {
            sentinel.setNext(elem);
        }

        elem.setNext(sentinel);
        sentinel.setPrev(elem);

        // do not forget to maintain invariant
        size += 1;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void printDeque() {
        Iterator<T> iter = this.iterator();
        while (iter.hasNext()) {
            System.out.print(iter.next());
            System.out.print(" ");
        }
        System.out.println("");
    }

    @Override
    public T removeFirst() {
        if (sentinel.getNext() == sentinel) {
            return null;
        }

        Node<T> elem = sentinel.getNext();

        sentinel.setNext(elem.getNext());
        elem.setPrev(null);
        elem.getNext().setPrev(sentinel);
        elem.setNext(null);

        // do not forget to maintain invariant
        size -= 1;


        return elem.getItem();
    }

    @Override
    public T removeLast() {
        if (sentinel.getPrev() == sentinel) {
            return null;
        }

        Node<T> elem = sentinel.getPrev();

        sentinel.setPrev(elem.getPrev());
        elem.setNext(null);
        elem.getPrev().setNext(sentinel);
        elem.setPrev(null);

        // do not forget to maintain invariant
        size -= 1;

        return elem.getItem();
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        Node<T> p = sentinel.getNext();

        for (int i = 0; i < index; ++i) {
            p = p.getNext();
        }

        return p.getItem();
    }

    private T helpRecursiveGet(Node<T> elem, int index) {
        if (index == 0) {
            return elem.getItem();
        }

        return helpRecursiveGet(elem.getNext(), index - 1);
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        return (T) helpRecursiveGet(sentinel.getNext(), index);
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node<T> p;

        public LinkedListDequeIterator() {
            this.p = sentinel.getNext();
        }

        @Override
        public boolean hasNext() {
            return this.p != sentinel;
        }

        @Override
        public T next() {
            T r = p.getItem();
            p = p.getNext();
            return r;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Deque)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        Deque<T> other = (Deque<T>) o;

        if (this.size() != other.size()) {
            return false;
        }


        for (int i = 0; i < this.size(); ++i) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }
}
