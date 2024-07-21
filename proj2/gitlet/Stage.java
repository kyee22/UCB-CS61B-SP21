package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

public class Stage implements Serializable {
    static final File INDEX_DIR = Utils.join(GitletRepository.GITLET_DIR, "index");
    static final File STAGE_ADD_FILE = Utils.join(INDEX_DIR, "stage_for_add");
    static final File STAGE_RM_FILE = Utils.join(INDEX_DIR, "stage_for_rm");
    private TreeMap<String, String> blobMap;

    public Stage() {
        this.blobMap = new TreeMap<>();
    }

    public static void init() {
        INDEX_DIR.mkdir();
        try {
            STAGE_ADD_FILE.createNewFile();
            STAGE_RM_FILE.createNewFile();
        } catch (Exception e) {
            System.err.println("fail to create " + STAGE_ADD_FILE + " " + STAGE_RM_FILE);
        }
    }

    public void add(Blob blob) {
        blobMap.put(blob.getPath(), blob.getUID());
    }

    public void remove(Blob blob) {
        blobMap.remove(blob.getPath());
    }

    public void remove(String path) {
        blobMap.remove(path);
    }

    public boolean isEmpty() {
        return blobMap.isEmpty();
    }

    public TreeMap<String, String> getAndClearBlobMap() {
        TreeMap<String, String> r = blobMap;
        blobMap = new TreeMap<>();
        return r;
    }

    public TreeMap<String, String> getBlobMap() {
        return blobMap;
    }

    public boolean contains(String path) {
        return blobMap.containsKey(path);
    }

    public String get(String path) {
        return blobMap.get(path);
    }

    public void clear() {
        blobMap.clear();
    }
}

