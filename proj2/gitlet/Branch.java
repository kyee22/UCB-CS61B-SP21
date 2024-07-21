package gitlet;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Branch {

    static final File HEAD_FILE = Utils.join(GitletRepository.GITLET_DIR, "HEAD");

    static final File REFS_DIR = Utils.join(GitletRepository.GITLET_DIR, "refs");
    static final File BRANCHES_DIR = Utils.join(REFS_DIR, "branches");

    // modify your preferred default branch here!!
    private static final String DEFAULT_BRANCH = "master";

    public static void init() {
        REFS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        try {
            HEAD_FILE.createNewFile();
        } catch (Exception e) {
            System.err.println("fail to create " + HEAD_FILE);
        }

        Utils.writeContents(HEAD_FILE, DEFAULT_BRANCH);
        Commit initCommit = new Commit();
        initCommit.save();

        File BRANCH_FILE = Utils.join(BRANCHES_DIR, DEFAULT_BRANCH);
        Utils.writeContents(BRANCH_FILE, initCommit.getUID());

    }

    public static Commit popHead() {
        String head = Utils.readContentsAsString(HEAD_FILE);
        File HEAD_BRANCH = Utils.join(BRANCHES_DIR, head);
        String uid = Utils.readContentsAsString(HEAD_BRANCH);
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
        } catch (Exception e) {
            System.out.println(e + ": can not create " + file);
        }
        Utils.writeContents(file, uid);
    }

    public static void checkout(String branchName) {
        Utils.writeContents(HEAD_FILE, branchName);
    }

    public static String curBranch() {
        return Utils.readContentsAsString(HEAD_FILE);
    }

    public static Commit popBranch(String branchName) {
        File HEAD_BRANCH = Utils.join(BRANCHES_DIR, branchName);
        String uid = Utils.readContentsAsString(HEAD_BRANCH);
        return Commit.fromFileByUID(uid);
    }
}
