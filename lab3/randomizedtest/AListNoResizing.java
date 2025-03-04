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

public class AListNoResizing<Item> {
    private Item[] items;
    private int size;

    /** Creates an empty list. */
    public AListNoResizing() {
        items = (Item[]) new Object[1000];
        size = 0;
    }

    /** Inserts X into the back of the list. */
    public void addLast(Item x) {
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
        Item x = getLast();
        items[size - 1] = null;
        size = size - 1;
        return x;
    }

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> sample = new AListNoResizing<>();

        sample.addLast(4);
        sample.addLast(5);
        sample.addLast(6);

        assertEquals((Integer) 6, sample.removeLast());
        assertEquals((Integer) 5, sample.removeLast());
        assertEquals((Integer) 4, sample.removeLast());

    }
}
