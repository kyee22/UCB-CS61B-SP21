package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    static final File COMMIT_DIR = Utils.join(GitletRepository.OBJ_DIR, "commits");
    private static DateFormat dateFormat =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);

    /** The message of this Commit. */
    private String message;
    private String timeStamp;
    private TreeMap<String, String> blobMap;
    private ArrayList<String> parents;
    private String UID;

    /* TODO: fill in the rest of this class. */
    public Commit(String msg, TreeMap<String, String> blobMap, ArrayList<String> parents) {
        this.message = msg;
        this.timeStamp = dateFormat.format(new Date());
        this.blobMap = blobMap;
        this.parents = parents;
        this.UID = Utils.sha1(message, timeStamp, blobMap.toString(), parents.toString());
    }

    public Commit() {
        this("initial commit", new TreeMap<>(), new ArrayList<>());
        this.timeStamp = dateFormat.format(new Date(0));
    }

    public static void init() {
        COMMIT_DIR.mkdir();
    }

    public boolean contains(String path) {
        return blobMap.containsKey(path);
    }

    public String get(String path) {
        if (!contains(path)) {
            return null;
        }

        return blobMap.get(path);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("===\n");
        sb.append("commit " + UID + "\n");
        if (parents.size() > 1) {
            sb.append("Merge: " + parents.get(0).substring(0,7) + " " + parents.get(1).substring(0,7) + "\n");
        }
        sb.append("Date: " + timeStamp + "\n");
        sb.append(message + "\n");
        sb.append("\n");

        return sb.toString();
    }

    public void save() {
        Utils.writeObject(Utils.join(COMMIT_DIR, UID), this);
    }

    public String getUID() {
        return UID;
    }

    public String getMessage() {
        return message;
    }

    public static Commit fromFileByUID(String UID) {
        return Utils.readObject(Utils.join(COMMIT_DIR, UID), Commit.class);
    }

    public static Commit fromFileByPrefixUID(String prefix) {
        for (String uid : Utils.plainFilenamesIn(COMMIT_DIR)) {
            if (uid.startsWith(prefix)) {
                return Utils.readObject(Utils.join(COMMIT_DIR, uid), Commit.class);
            }
        }

        return null;
    }

    public static ArrayList<Commit> fromFileAll() {
        ArrayList<Commit> res = new ArrayList<>();
        for (String uid : Utils.plainFilenamesIn(COMMIT_DIR)) {
            res.add(Utils.readObject(Utils.join(COMMIT_DIR, uid), Commit.class));
        }

        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Commit)) {
            return false;
        }

        return UID.equals(((Commit)o).getUID());
    }

    @Override
    public int hashCode() {
        return UID != null ? UID.hashCode() : 0;
    }


    public TreeMap<String, String> dCloneBlobMap() {
        TreeMap<String, String> res = new TreeMap<>();
        for (String k : blobMap.keySet()) {
            res.put(k, blobMap.get(k));
        }

        return res;
    }

    public Commit getFirstParent() {
        if (parents.isEmpty()) {
            return null;
        }

        return Commit.fromFileByUID(parents.get(0));
    }

    public Commit getSecondParent() {
        if (parents.isEmpty() || parents.size() < 2) {
            return null;
        }

        return Commit.fromFileByUID(parents.get(1));
    }

    public static Commit findLowestCommonAncestor(Commit commit1, Commit commit2) {
        HashMap<Commit, Integer> distances = new HashMap<>();
        dfs(commit1, 0, distances);

        LinkedList<Commit> queue = new LinkedList<>();
        queue.add(commit2);
        int minDistance = Integer.MAX_VALUE;
        Commit lca = null;

        while (!queue.isEmpty()) {
            Commit curCommit = queue.poll();
            if (distances.containsKey(curCommit) && distances.get(curCommit) < minDistance) {
                lca = curCommit;
                minDistance = distances.get(curCommit);
            }
            if (curCommit.getFirstParent() != null) {
                queue.add(curCommit.getFirstParent());

            }
            if (curCommit.getSecondParent() != null) {
                queue.add(curCommit.getSecondParent());
            }
        }

        return lca;
    }

    private static void dfs(Commit commit, int distance, HashMap<Commit, Integer> distances) {
        if (commit == null) {
            return;
        }

        int _distance = distance;
        if (distances.containsKey(commit) && distances.get(commit) < distance) {
            _distance = distances.get(commit);
        }
        distances.put(commit, _distance);
        dfs(commit.getFirstParent(), distance + 1, distances);
        dfs(commit.getSecondParent(), distance + 1, distances);
    }
}
