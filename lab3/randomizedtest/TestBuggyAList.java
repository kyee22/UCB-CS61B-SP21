package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> expect = new AListNoResizing<>();
        BuggyAList<Integer> actual = new BuggyAList<>();

        actual.addLast(4);
        actual.addLast(5);
        actual.addLast(6);

        expect.addLast(4);
        expect.addLast(5);
        expect.addLast(6);


        assertEquals((Integer) 6, actual.removeLast());
        assertEquals((Integer) 5, actual.removeLast());
        assertEquals((Integer) 4, actual.removeLast());

        assertEquals((Integer) 6, expect.removeLast());
        assertEquals((Integer) 5, expect.removeLast());
        assertEquals((Integer) 4, expect.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> sample = new BuggyAList<>();

        int N = 50000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, L.size() == 0 ? 2 : 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
                sample.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                assertEquals(L.size(), sample.size());
                System.out.println("size: " + size);
            } else if (operationNumber == 2) {
                assertEquals(L.getLast(), sample.getLast());
                System.out.println("getLast(" + L.getLast() + ")");
            } else if (operationNumber == 3) {
                int tmp = L.removeLast();
                assertEquals((Integer) tmp, sample.removeLast());
                System.out.println("removeLast(" + tmp + ")");
            }
        }
    }
}
