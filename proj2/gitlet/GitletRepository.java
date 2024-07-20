package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeMap;

public class GitletRepository {
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    // TODO Hint: look at the `join`
    //      function in Utils
    static final File STORY_FILE = Utils.join(GITLET_DIR, "story");
    static final File OBJ_DIR = Utils.join(GITLET_DIR, "objects");
    static final File BLOB_DIR = Utils.join(OBJ_DIR, "blobs");
    static final File COMMIT_DIR = Utils.join(OBJ_DIR, "commits");
    static final File HEAD_FILE = Utils.join(GITLET_DIR, "HEAD");
    static final File INDEX_DIR = Utils.join(GITLET_DIR, "index");
    static final File STAGE_ADD_FILE = Utils.join(INDEX_DIR, "stage_for_add");
    static final File STAGE_RM_FILE = Utils.join(INDEX_DIR, "stage_for_rm");
    static final File REFS_DIR = Utils.join(GITLET_DIR, "refs");
    static final File BRANCHES_DIR = Utils.join(REFS_DIR, "branches");
    private static Stage stageForAddition;
    private static Stage stageForRemoval;
    private static final String DEFAULT_BRANCH = "master";  // modify your preferred default branch here!!

    /*
     *   .gitlet
     *      |--objects
     *      |     |--commit and blob
     *      |--refs
     *      |    |--branches
     *      |         |--master
     *      |--HEAD
     *      |--stage
     */
    public static void init() throws GitletException {
        // TODO
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJ_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();
        INDEX_DIR.mkdir();
        REFS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        try {
            HEAD_FILE.createNewFile();
            STAGE_ADD_FILE.createNewFile();
            STAGE_RM_FILE.createNewFile();
        } catch (Exception e) {
            System.err.println("fail to create " + String.valueOf(HEAD_FILE));
        }

        Stage stageForAddition = new Stage();
        Stage stageForRemoval = new Stage();
        Utils.writeObject(STAGE_ADD_FILE, stageForAddition);
        Utils.writeObject(STAGE_RM_FILE, stageForRemoval);

        Utils.writeContents(HEAD_FILE, DEFAULT_BRANCH);
        Commit initCommit = new Commit();
        initCommit.save();

        File BRANCH_FILE = Utils.join(BRANCHES_DIR, DEFAULT_BRANCH);
        Utils.writeContents(BRANCH_FILE, initCommit.getUID());
    }

    private static void popStage() {
        stageForAddition = Utils.readObject(STAGE_ADD_FILE, Stage.class);
        stageForRemoval = Utils.readObject(STAGE_RM_FILE, Stage.class);
    }

    private static void pushStage() {
        Utils.writeObject(STAGE_ADD_FILE, stageForAddition);
        Utils.writeObject(STAGE_RM_FILE, stageForRemoval);
    }

    private static Commit popHead() {
        String head = Utils.readContentsAsString(HEAD_FILE);
        File HEAD_BRANCH = Utils.join(BRANCHES_DIR, head);
        String uid = Utils.readContentsAsString(HEAD_BRANCH);
        return Commit.fromFileByUID(uid);
    }

    private static void updateHead(String uid) {
        String head = Utils.readContentsAsString(HEAD_FILE);
        File HEAD_BRANCH = Utils.join(BRANCHES_DIR, head);
        Utils.writeContents(HEAD_BRANCH, uid);
    }

    public static void add(String path) throws GitletException {
        popStage();

        File file = Paths.get(path).isAbsolute() ? new File(path) : Utils.join(CWD, path);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        Blob blob = new Blob(file);
        Commit curCommit = popHead();

        // 文件未变更，从暂存区中移除
        if (curCommit.contains(blob.getPath()) && curCommit.get(blob.getPath()).equals(blob.getUID())) {
            stageForAddition.remove(blob);
        } else { // // 否则，覆盖写到暂存区
            blob.save();
            stageForAddition.add(blob);
        }
        // 如果文件被标记为删除，移除该标记
        stageForRemoval.remove(blob);


        pushStage();
    }

    public static void commit(String msg) throws GitletException {
        popStage();

        if (msg.isBlank()) {
            throw new GitletException("Please enter a commit message.");
        }
        if (stageForAddition.isEmpty() && stageForRemoval.isEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        // 取出当前 Commit
        Commit curCommit = popHead();
        // 设置新 Commit 的父节点为 当前 Commit
        ArrayList<String> parents = new ArrayList<>();
        parents.add(curCommit.getUID());
        // 继承并更新父节点的 blobMap
        TreeMap<String, String> blobMap = curCommit.dCloneBlobMap();
        TreeMap<String, String> blobsToAdd = stageForAddition.getAndClearBlobMap();
        TreeMap<String, String> blobsToRemove = stageForRemoval.getAndClearBlobMap();
        for (String k : blobsToAdd.keySet()) {
            blobMap.put(k, blobsToAdd.get(k));
        }
        for (String k : blobsToRemove.keySet()) {
            blobMap.remove(k);
        }
        // 构建新的 Commit
        Commit newCommit = new Commit(msg, blobMap, parents);
        newCommit.save();
        updateHead(newCommit.getUID());

        pushStage();
    }

    public static void checkout(String... args) throws GitletException {
        if (args.length == 2 && "--".equals(args[0])) {
            // Usage: checkout -- [file name]
            checkoutFile(args[1]);
        } else if (args.length == 3 && "--".equals(args[1])) {
            // Usage: checkout [commit id] -- [file name]
            checkoutCommitFile(args[0], args[2]);
        } else if (args.length == 1) {
            // Usage: checkout [branch name]
            checkoutBranch(args[0]);
        } else {
            throw new GitletException("Incorrect operands.");
        }
    }

    public static void checkoutFile(String path) throws GitletException {
        File file = Paths.get(path).isAbsolute() ? new File(path) : Utils.join(CWD, path);
        Commit curCommit = popHead();
        if (!curCommit.contains(file.getPath())) {
            throw new GitletException("File does not exist in that commit.");
        }
        Blob blob = Blob.fromFile(curCommit.get(file.getPath()));
        blob.writeBack();;
    }

    public static void checkoutCommitFile(String commitID, String path) {
        File file = Paths.get(path).isAbsolute() ? new File(path) : Utils.join(CWD, path);
        Commit commit = Commit.fromFileByPrefixUID(commitID);
        if (commit == null) {
            throw new GitletException("No commit with that id exists.");
        }
        if (!commit.contains(file.getPath())) {
            throw new GitletException("File does not exist in that commit.");
        }
        Blob blob = Blob.fromFile(commit.get(file.getPath()));
        blob.writeBack();
    }

    public static void checkoutBranch(String branchName) {
        //  TODO: Checkpoint 过了之后再来完成
    }

    public static void log() {
        Commit curCommit = popHead();
        do {
            System.out.print(curCommit);
            curCommit = curCommit.getFirstParent();
        } while (curCommit != null);
    }

    public static void status() {
        popStage();

        System.out.println("=== Branches ===");
        String headBranchName = Utils.readContentsAsString(HEAD_FILE);
        for (String branchName : Utils.plainFilenamesIn(BRANCHES_DIR)) {
            if (branchName.equals(headBranchName)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        for (String uid : stageForAddition.getBlobMap().values()) {
            Blob blob = Blob.fromFile(uid);
            System.out.println(blob.getFile().getName());
        }
        System.out.println("");

        System.out.println("=== Removed Files ===");
        for (String uid : stageForRemoval.getBlobMap().values()) {
            Blob blob = Blob.fromFile(uid);
            System.out.println(blob.getFile().getName());
        }
        System.out.println("");

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");

        System.out.println("=== Untracked Files ===");
        System.out.println("");

        pushStage();
    }

}
