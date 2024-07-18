package flik;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestFlik {

    @Test
    public void testIsSameNumber() {
        int MAX_TEST = 1000;
        for (int i = 0; i < MAX_TEST; ++i) {
            assertTrue("expeceted True, bug got False " + i + "==" + i + "?", Flik.isSameNumber(i, i));
        }
    }
}
