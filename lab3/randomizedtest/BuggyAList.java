package randomizedtest;

import org.junit.Test;


import static org.junit.Assert.assertEquals;

/** Array based list.
 *  @author Josh Hug
 */

//         0 1  2 3 4 5 6 7
// items: [6 9 -1 2 0 0 0 0 ...]
// size: 5

/* Invariants:
 addLast: The next item we want to add, will go into position size
 getLast: The item we want to return is in position size - 1
 size: The number of items in the list should be size.
*/

public class BuggyAList<Item> {
    private Item[] items;
    private int size;

    /** Creates an empty list. */
    public BuggyAList() {
        items = (Item[]) new Object[1];
        size = 0;
    }

    /** Resizes the underlying array to the target capacity. */
    private void resize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];
        //for (int i = 0; i < size; i += 1) {                           // buggy line
        //for (int i = 0; i < capacity; i += 1) {                       // fixed line - Wrong!! still buggy!!
        //for (int i = 0; i < Integer.min(size, capacity); i += 1) {    // wrong fix!
        for (int i = 0; i < size; i += 1) {
            /**
             *  Bug does not lie in the `size` of `resize`,
             *  Instead, the caller if `resize` should guarantee passed-in `capacity` > `size`
             */
            a[i] = items[i];
        }
        items = a;
    }

    /** Inserts X into the back of the list. */
    public void addLast(Item x) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[size] = x;
        size = size + 1;
    }

    /** Returns the item from the back of the list. */
    public Item getLast() {
        return items[size - 1];
    }
    /** Gets the ith item in the list (0 is the front). */
    public Item get(int i) {
        return items[i];
    }

    /** Returns the number of items in the list. */
    public int size() {
        return size;
    }

    /** Deletes item from back of the list and
      * returns deleted item. */
    public Item removeLast() {
        if ((size < items.length / 4) && (size > 4)) {
            //resize(size / 4);                 // buggy?
            resize(items.length / 4);   // correct fix!
        }
        Item x = getLast();
        items[size - 1] = null;
        size = size - 1;
        return x;
    }

    @Test
    public void testThreeAddThreeRemove() {
        BuggyAList<Integer> sample = new BuggyAList<>();

        sample.addLast(4);
        sample.addLast(5);
        sample.addLast(6);

        assertEquals((Integer) 6, sample.removeLast());
        assertEquals((Integer) 5, sample.removeLast());
        assertEquals((Integer) 4, sample.removeLast());

    }

}
