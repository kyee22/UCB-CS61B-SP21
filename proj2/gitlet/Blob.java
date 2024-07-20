package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private File file;
    private byte[] content;
    private String UID;

    public Blob(File file) {
        this.file = file;
        this.content = Utils.readContents(file);
        this.UID = Utils.sha1(file.getPath(), content);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Blob)) {
            return false;
        }

        return UID.equals(((Blob)o).getUID());
    }

    public String getUID() {
        return UID;
    }

    public String getPath() {
        return file.getPath();
    }

    public void save() {
        Utils.writeObject(Utils.join(GitletRepository.BLOB_DIR, UID), this);
    }

    public static Blob fromFile(String UID) {
        return (Blob) Utils.readObject(Utils.join(GitletRepository.BLOB_DIR, UID), Blob.class);
    }

    public void writeBack() {
        Utils.writeContents(this.file, this.content);
    }

    public File getFile() {
        return file;
    }
}
