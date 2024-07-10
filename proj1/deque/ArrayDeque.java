package deque;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayDeque<T> {

    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        this.size = 0;
        this.items = (T[]) new Object[8];
        this.nextFirst = 4;
        this.nextLast = 5;
    }

    public void addFirst(T item) {
        if (nextFirst == nextLast) {
            resize(size * 2);
        }

        items[nextFirst] = item;
        nextFirst = nextFirst(nextFirst);

        // do not forget to maintain invariant
        size += 1;
    }

    public void addLast(T item) {
        if (nextFirst == nextLast) {
            resize(size * 2);
        }

        items[nextLast] = item;
        nextLast = nextLast(nextLast);

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
        for (int i = nextLast(nextFirst); i != nextLast; i = nextLast(i)) {
            System.out.print(items[i]);
            System.out.print(nextLast(i) != nextLast ? " " : "\n");
        }

    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        if ((size < items.length / 4) && (size > 8)) {
            resize(items.length / 4);
        }


        nextFirst = nextLast(nextFirst);
        // do not forget to maintain invariant
        size -= 1;

        return items[nextFirst];
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        if ((size < items.length / 4) && (size > 8)) {
            resize(items.length / 4);
        }

        nextLast = nextFirst(nextLast);

        // do not forget to maintain invariant
        size -= 1;

        return items[nextLast];
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        int front = nextLast(nextFirst);
        int target = (front + index) % items.length;

        return items[target];
    }

    public Iterator<T> iterator() {
        // TODO
        return null;
    }

    public boolean equals(Object o) {
        //if (o == null || !(o instanceof )) {
        //    return false;
        //}
        // TODO after implementation of iterator
        return true;
    }


    private int nextFirst(int first) {
        if (first == 0) {
            return items.length - 1;
        } else {
            return  first - 1;
        }
    }

    private int nextLast(int last) {
        if (last == items.length - 1) {
            return 0;
        } else {
            return last + 1;
        }
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];

        for (int i = nextLast(nextFirst), j = 0; i != nextLast; i = nextLast(i), ++j) {
            a[j] = items[i];
        }

        items = a;
        nextFirst = nextFirst(0);
        nextLast = size;
    }

}
