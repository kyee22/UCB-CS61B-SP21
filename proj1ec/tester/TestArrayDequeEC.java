package tester;

import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;


public class TestArrayDequeEC {

    @Test
    public void strongRandomizedTest() {
        StudentArrayDeque<Integer> stu = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> std = new ArrayDequeSolution<>();
        int N = 5000;
        String debugInfo = "";

        for (int i = 0; i < N; i += 1) {

            // ALWAYS check the size, that is at any time!!
            debugInfo += "size()\n";
            assertEquals(debugInfo, std.size(), stu.size());

            int operationNumber = StdRandom.uniform(0, stu.size() == 0 ? 2 : 4);

            if (operationNumber == 0) {
                // addLast
                Integer randVal = StdRandom.uniform(0, 1000);
                stu.addLast(randVal);
                std.addLast(randVal);

                debugInfo += "addLast(" + randVal + ")\n";
            } else if (operationNumber == 1) {
                // addFirst
                Integer randVal = StdRandom.uniform(0, 1000);
                stu.addFirst(randVal);
                std.addFirst(randVal);

                debugInfo += "addFirst(" + randVal + ")\n";
            } else if (operationNumber == 2) {
                // removeLast
                Integer stur = stu.removeLast();
                Integer stdr = std.removeLast();

                debugInfo += "removeLast()\n";

                assertEquals(debugInfo, stdr, stur);
            } else if (operationNumber == 3) {
                // removeFirst
                Integer stur = stu.removeFirst();
                Integer stdr = std.removeFirst();

                debugInfo += "removeFirst()\n";

                assertEquals(debugInfo, stdr, stur);
            }
        }

    }
}
