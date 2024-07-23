package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Branch {

    static final File HEAD_FILE = Utils.join(GitletRepository.GITLET_DIR, "HEAD");

    static final File BRANCHES_DIR = Utils.join(GitletRepository.REFS_DIR, "branches");

    // modify your preferred default branch here!!
    private static final String DEFAULT_BRANCH = "master";

    public static void init() {
        BRANCHES_DIR.mkdir();
        try {
            HEAD_FILE.createNewFile();
        } catch (IOException e) {
            System.err.println("fail to create " + HEAD_FILE);
        }

        Utils.writeContents(HEAD_FILE, DEFAULT_BRANCH);
        Commit initCommit = new Commit();
        initCommit.save();

        File branchFile = Utils.join(BRANCHES_DIR, DEFAULT_BRANCH);
        Utils.writeContents(branchFile, initCommit.getUID());

    }

    public static Commit popHead() {
        String head = Utils.readContentsAsString(HEAD_FILE);
        File headBranch = Utils.join(BRANCHES_DIR, head);
        String uid = Utils.readContentsAsString(headBranch);
        return Commit.fromFileByUID(uid);
    }

    public static void updateHead(String uid) {
        String head = Utils.readContentsAsString(HEAD_FILE);
        File HEAD_BRANCH = Utils.join(BRANCHES_DIR, head);
        Utils.writeContents(HEAD_BRANCH, uid);
    }

    public static void status() {
        System.out.println("=== Branches ===");
        String headBranchName = Utils.readContentsAsString(HEAD_FILE);
        List<String> names = Utils.plainFilenamesIn(BRANCHES_DIR);
        Collections.sort(names);
        for (String branchName : names) {
            if (branchName.equals(headBranchName)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println("");
    }

    public static boolean exist(String branchName) {
        for (String name : Utils.plainFilenamesIn(BRANCHES_DIR)) {
            if (name.equals(branchName)) {
                return true;
            }
        }

        return false;
    }

    public static void create(String branchName) {
        File file = Utils.join(BRANCHES_DIR, branchName);
        String uid = popHead().getUID();
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println(e + ": can not create " + file);
        }
        Utils.writeContents(file, uid);
    }

    public static void delete(String branchName) {
        File file = Utils.join(BRANCHES_DIR, branchName);
        file.delete();
    }

    public static void checkout(String branchName) {
        Utils.writeContents(HEAD_FILE, branchName);
    }

    public static String curBranch() {
        return Utils.readContentsAsString(HEAD_FILE);
    }

    public static Commit popBranch(String branchName) {
        File headBranch = Utils.join(BRANCHES_DIR, branchName);
        String uid = Utils.readContentsAsString(headBranch);
        return Commit.fromFileByUID(uid);
    }
}
