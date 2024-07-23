package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Blob implements Serializable {
    static final File BLOB_DIR = Utils.join(GitletRepository.OBJ_DIR, "blobs");

    private File file;
    private byte[] content;
    private String UID;

    public Blob(File file) {
        this.file = file;
        this.content = Utils.readContents(file);
        this.UID = Utils.sha1(file.getPath(), content);
    }

    public static void init() {
        BLOB_DIR.mkdir();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Blob)) {
            return false;
        }

        return UID.equals(((Blob) o).getUID());
    }

    @Override
    public int hashCode() {
        return this.UID != null ? this.UID.hashCode() : 0;
    }

    public String getUID() {
        return UID;
    }

    public String getPath() {
        return file.getPath();
    }

    public void save() {
        Utils.writeObject(Utils.join(BLOB_DIR, UID), this);
    }

    public static Blob fromFile(String uid) {
        return Utils.readObject(Utils.join(BLOB_DIR, uid), Blob.class);
    }

    public void writeBack() {
        Utils.writeContents(this.file, this.content);
    }

    public File getFile() {
        return file;
    }

    public String getContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }
}
