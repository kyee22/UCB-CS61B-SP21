package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.TreeMap;
import java.util.Locale;


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

    private static DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);

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
        sb.append("Date: " + timeStamp + "\n");
        sb.append(message + "\n");
        sb.append("\n");

        return sb.toString();
    }

    public void save() {
        Utils.writeObject(Utils.join(GitletRepository.COMMIT_DIR, UID), this);
    }

    public String getUID() {
        return UID;
    }

    public static Commit fromFileByUID(String UID) {
        return (Commit) Utils.readObject(Utils.join(GitletRepository.COMMIT_DIR, UID), Commit.class);
    }

    public static Commit fromFileByPrefixUID(String prefix) {
        for (String uid : Utils.plainFilenamesIn(GitletRepository.COMMIT_DIR)) {
            if (uid.startsWith(prefix)) {
                return (Commit) Utils.readObject(Utils.join(GitletRepository.COMMIT_DIR, uid), Commit.class);
            }
        }

        return null;
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
}
