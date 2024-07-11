package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {

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

    @Override
    public void addLast(T item) {
        if (nextFirst == nextLast) {
            resize(size * 2);
        }

        items[nextLast] = item;
        nextLast = nextLast(nextLast);

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
        while(iter.hasNext()) {
            System.out.print(iter.next());
            System.out.print(" ");
        }
        System.out.println("");
    }

    @Override
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

    @Override
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

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        int front = nextLast(nextFirst);
        int target = (front + index) % items.length;

        return items[target];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int pos;

        public ArrayDequeIterator() {
            this.pos = nextLast(nextFirst);
        }

        @Override
        public boolean hasNext() {
            return this.pos != nextLast;
        }

        @Override
        public T next() {
            T r = items[pos];
            pos = nextLast(pos);
            return r;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Deque)) {
            return false;
        }

        if (this.size() != ((Deque<T>) o).size()) {
            return false;
        }

        Iterator<T> me = this.iterator();
        Iterator<T> other = ((Deque<T>) o).iterator();

        while (me.hasNext() && other.hasNext()) {
            if (me.next().equals(other.next())) {
                return false;
            }
        }
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
