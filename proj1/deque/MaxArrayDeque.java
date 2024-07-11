package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        this.comparator = c;
    }

    public T max() {
        if (super.isEmpty()) {
            return null;
        }

        T result = super.get(0);

        Iterator<T> iter = super.iterator();
        while (iter.hasNext()) {
            T curr = iter.next();
            if (comparator.compare(curr, result) > 0) {
                result = curr;
            }
        }

        return result;
    }

    public T max(Comparator<T> c) {
        if (super.isEmpty()) {
            return null;
        }

        T result = super.get(0);

        Iterator<T> iter = super.iterator();
        while (iter.hasNext()) {
            T curr = iter.next();
            if (c.compare(curr, result) > 0) {
                result = curr;
            }
        }

        return result;
    }
}
