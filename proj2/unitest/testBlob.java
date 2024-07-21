package unitest;

import gitlet.Blob;
import gitlet.Commit;
import gitlet.Stage;
import gitlet.Utils;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class testBlob {

    static final File CWD = new File(System.getProperty("user.dir"));
    static final File TEST = Utils.join(CWD, "unitest");


    @Test
    public void testBlobEquals() {
        File file1 = Utils.join(TEST, "a.txt");
        File file2 = Utils.join(TEST, "a.txt");
        File file3 = Utils.join(TEST, "b.txt");
        Utils.writeContents(file2, "old info!\n");
        Utils.writeContents(file3, "old info\n");

        Blob blob1 = new Blob(file1);
        Blob blob2 = new Blob(file2);
        Blob blob3 = new Blob(file3);

        Utils.writeContents(file1, "new info\n");
        Blob blob4 = new Blob(file2);

        assertEquals(blob1, blob2);
        assertNotEquals(blob1, blob3);
        assertNotEquals(blob1, blob4);
        assertNotEquals(blob3, blob4);
        assertNotEquals(blob1, null);
        assertNotEquals(blob1, "a string");
    }

    @Test
    public void testCommitBlobIdentical() {
        File file1 = Utils.join(TEST, "1.txt");
        File file2 = Utils.join(TEST, "1.txt");
        File file3 = Utils.join(TEST, "2.txt");
        Utils.writeContents(file2, "old info!\n");
        Utils.writeContents(file3, "old info\n");

        Commit initCommit = new Commit();
        ArrayList<String> p = new ArrayList<>();
        p.add(initCommit.getUID());

        Blob blob1 = new Blob(file1);
        Blob blob2 = new Blob(file2);
        Blob blob3 = new Blob(file3);

        TreeMap<String, String> mp = new TreeMap<>();
        mp.put(blob1.getPath(), blob1.getUID());
        mp.put(blob3.getPath(), blob3.getUID());

        Commit commit = new Commit("commit message", mp, p);

        Utils.writeContents(file1, "new info\n");
        Blob blob4 = new Blob(file2);
        Utils.writeContents(file2, "old info!\n");
        Blob blob5 = new Blob(file1);

        assertTrue(commit.contains(blob3.getPath()));
        assertTrue(commit.contains(blob2.getPath()));
        assertTrue(commit.get(blob2.getPath()).equals(blob2.getUID()));
        assertTrue(commit.contains(blob4.getPath()));
        assertFalse(commit.get(blob4.getPath()).equals(blob4.getUID()));
        assertTrue(commit.contains(blob5.getPath()));
        assertTrue(commit.get(blob5.getPath()).equals(blob5.getUID()));
    }

    @Test
    public void testStageAddAndRemove() {
        File file1 = Utils.join(TEST, "1.txt");
        File file2 = Utils.join(TEST, "1.txt");
        File file3 = Utils.join(TEST, "2.txt");
        Utils.writeContents(file2, "old info!\n");
        Utils.writeContents(file3, "old info\n");
        Blob blob1 = new Blob(file1);
        Blob blob2 = new Blob(file2);
        Blob blob3 = new Blob(file3);

        Stage stage = new Stage();
        assertTrue(stage.isEmpty());

        stage.remove(blob1);
        stage.remove(blob2);
        stage.remove(blob3);
        assertTrue(stage.isEmpty());

        stage.add(blob1);
        stage.add(blob1);
        assertFalse(stage.isEmpty());
        stage.add(blob2);
        stage.add(blob3);
        stage.remove(blob2);
        stage.remove(blob2);
        stage.remove(blob3);
        stage.remove(blob1);
        assertTrue(stage.isEmpty());
    }

    @Test
    public void testMergeCommitLog() {
        Commit initCommit = new Commit();
        ArrayList<String> p = new ArrayList<>();
        p.add(initCommit.getUID());

        Commit commit1 = new Commit("commit 1", new TreeMap<>(), p);

        ArrayList<String> pp = new ArrayList<>();
        pp.add(initCommit.getUID());
        pp.add(commit1.getUID());
        Commit commit2 = new Commit("merge", new TreeMap<>(), pp);

        System.out.println(commit1);
        System.out.println(commit2);
    }
}
