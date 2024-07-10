package deque;

import java.util.Iterator;

public class LinkedListDeque<T> {

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

    public boolean isEmpty() {
        return this.size == 0;
    }

    public int size() {
        return this.size;
    }

    public void printDeque() {
        Node<T> p = sentinel.getNext();

        for (; p != sentinel; p = p.getNext()) {
            System.out.print(p);
            System.out.print(p.getNext() != sentinel ? " " : "\n");
        }
    }

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

    public Iterator<T> iterator() {
        // TODO
        return null;
    }

    public boolean equals(Object o) {
        // TODO after implementation of iterator

        //if (o == null || !(o instanceof LinkedListDeque)) {
        //    return false;
        //}
        //
        //LinkedListDeque<T> other = (LinkedListDeque<T>) o;
        //
        //if (this.isEmpty() != other.isEmpty()) {
        //    return false;
        //}
        //
        //if (this.isEmpty()) {
        //    return true;
        //}
        //
        //Node<T> p = sentinel.getNext();
        //Node<T> q = other.getSentinel().getNext();
        //for (; p != this.sentinel; p = p.getNext(), q = q.getNext()) {
        //    if (!p.equals(q)) {
        //        return false;
        //    }
        //}

        return true;
    }

    public Node<T> getSentinel() {
        return sentinel;
    }
}
