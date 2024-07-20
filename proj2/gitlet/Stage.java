package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

public class Stage implements Serializable {
    private TreeMap<String, String> blobMap;

    public Stage() {
        this.blobMap = new TreeMap<>();
    }

    public void add(Blob blob) {
        //if (blobMap.containsValue(blob.getUID())) {
        //    return;
        //}

        blobMap.put(blob.getPath(), blob.getUID());
    }

    public void remove(Blob blob) {
        blobMap.remove(blob.getPath());
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
}

