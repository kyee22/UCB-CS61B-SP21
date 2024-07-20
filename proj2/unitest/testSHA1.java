package unitest;

import gitlet.Blob;
import gitlet.Commit;
import gitlet.Utils;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class testSHA1 {

    static final File CWD = new File(System.getProperty("user.dir"));
    static final File TEST = Utils.join(CWD, "unitest");

    @Test
    public void testBlobShaId() {
        File file1 = Utils.join(TEST, "a.txt");
        File file2 = Utils.join(TEST, "a.txt");
        Utils.writeContents(file2, "old info!\n");
        Blob blob1 = new Blob(file1);
        Blob blob2 = new Blob(file2);
        String uid1 = blob1.getUID();
        String uid2 = blob2.getUID();

        Utils.writeContents(file2, "new info!\n");
        Blob blob3 = new Blob(file2);
        Blob blob4 = new Blob(file1);

        String uid3 = blob3.getUID();
        String uid4 = blob4.getUID();

        Utils.writeContents(file2, "old info!\n");
        Blob blob5 = new Blob(file1);
        String uid5 = blob5.getUID();

        File file3 = Utils.join(TEST, "b.txt");
        Utils.writeContents(file3, "old info!\n");
        Blob blob6 = new Blob(file3);
        String uid6 = blob6.getUID();

        assertEquals(40, uid1.length());
        assertEquals(40, uid2.length());
        assertEquals(40, uid3.length());
        assertEquals(40, uid4.length());
        assertEquals(40, uid5.length());
        assertEquals(uid1, uid2);
        assertEquals(uid3, uid4);
        assertNotEquals(uid1, uid3);
        assertNotEquals(uid2, uid4);
        assertEquals(uid1, uid5);
        assertEquals(uid2, uid5);
        assertNotEquals(uid5, uid6);

        System.out.println(uid1 + "\n" + uid2 + "\n" + uid3 + "\n" + uid4);
    }

    @Test
    public void testCommitShaId() {
        Commit initCommit = new Commit();

        ArrayList<String> parents1 = new ArrayList<>();
        ArrayList<String> parents2 = new ArrayList<>();
        parents1.add(initCommit.getUID());
        parents2.add(initCommit.getUID());

        TreeMap<String, String> mp1 = new TreeMap<>();
        TreeMap<String, String> mp2 = new TreeMap<>();
        mp1.put("/a/b/c", "19e69eb44c8b2a2508f89835f0ded760ee417076");
        mp1.put("/e","c2b61c9b9da59d8cc3bc9f706f5bc6ca244439c3");

        mp2.put("/e","111111111111111111111111111111111111");
        mp2.put("/a/b/c", "19e69eb44c8b2a2508f89835f0ded760ee417076");
        mp2.put("/e","c2b61c9b9da59d8cc3bc9f706f5bc6ca244439c3");
        mp2.put("/a/b/c", "222222222222222222222222222222222");
        mp2.put("/a/b/c", "19e69eb44c8b2a2508f89835f0ded760ee417076");

        //System.out.println(mp1);
        //System.out.println(mp2);

        Commit commit1 = new Commit("commit message!", mp1, parents1);
        Commit commit2 = new Commit("commit message!", mp2, parents2);
        //System.out.println(initCommit.getUID());
        //System.out.println(commit1.getUID());
        //System.out.println(commit2.getUID());

        mp2.put("3","45");
        Commit commit3 = new Commit("commit message!", mp2, parents2);

        assertEquals(40, commit1.getUID().length());
        assertEquals(40, commit2.getUID().length());
        assertEquals(40, commit3.getUID().length());
        assertNotEquals(initCommit, commit1);
        assertNotEquals(initCommit, commit2);
        assertNotEquals(initCommit, commit3);
        assertNotEquals(commit1, commit3);
        assertNotEquals(commit2.getUID(), commit3.getUID());
        assertEquals(commit1, commit2);

        System.out.println(initCommit);
        System.out.println(commit1);
        System.out.println(commit2);
        System.out.println(commit3);
    }

    @Test
    public void testInitCommitEquals() {
        Commit init1 = new Commit();
        Commit init2 = new Commit();
        Commit init3 = new Commit();

        assertEquals(init1, init2);
        assertEquals(init2, init3);

        TreeMap<String, String> mp = new TreeMap<>();
        mp.remove("2");
        System.out.println(Utils.plainFilenamesIn(TEST));
    }
}
