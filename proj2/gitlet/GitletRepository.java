package gitlet;

import java.awt.image.CropImageFilter;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

public class GitletRepository {
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    static final File OBJ_DIR = Utils.join(GITLET_DIR, "objects");
    private static Stage stageForAddition;
    private static Stage stageForRemoval;

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
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJ_DIR.mkdir();

        Blob.init();
        Commit.init();
        Stage.init();
        Branch.init();

        stageForAddition = new Stage();
        stageForRemoval = new Stage();
        Utils.writeObject(Stage.STAGE_ADD_FILE, stageForAddition);
        Utils.writeObject(Stage.STAGE_RM_FILE, stageForRemoval);
    }

    private static void popStage() {
        stageForAddition = Utils.readObject(Stage.STAGE_ADD_FILE, Stage.class);
        stageForRemoval = Utils.readObject(Stage.STAGE_RM_FILE, Stage.class);
    }

    private static void pushStage() {
        Utils.writeObject(Stage.STAGE_ADD_FILE, stageForAddition);
        Utils.writeObject(Stage.STAGE_RM_FILE, stageForRemoval);
    }

    public static void entry(String... args) throws GitletException {
        popStage();
        switch (args[0]) {
            case "add":
                validateNumArgs(args, 2);
                add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                commit(args[1]);
                break;
            case "checkout":
                validateNumArgs(args, 2, 3, 4);
                checkout(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "rm":
                validateNumArgs(args, 2);
                rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                global_log();
                break;
            case "find":
                validateNumArgs(args, 2);
                find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                status();
                break;
            case "branch":
                validateNumArgs(args, 2);
                branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                rm_branch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                merge(args[1]);
                break;
            default:
                throw new GitletException("No command with that name exists.");
        }
        pushStage();
    }

    public static void validateNumArgs(String[] args, int... allowedLengths) throws GitletException {
        boolean isValidLength = false;
        for (int length : allowedLengths) {
            if (args.length == length) {
                isValidLength = true;
                break;
            }
        }
        if (!isValidLength) {
            throw new GitletException("Incorrect operands.");
        }
    }


    public static void add(String path) throws GitletException {
        File file = Paths.get(path).isAbsolute() ? new File(path) : Utils.join(CWD, path);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        Blob blob = new Blob(file);
        Commit curCommit = Branch.popHead();

        // 文件未变更，从暂存区中移除
        if (curCommit.contains(blob.getPath())
                && curCommit.get(blob.getPath()).equals(blob.getUID())) {
            stageForAddition.remove(blob);
        } else { // // 否则，覆盖写到暂存区
            blob.save();
            stageForAddition.add(blob);
        }
        // 如果文件被标记为删除，移除该标记
        stageForRemoval.remove(blob);
    }

    public static void commit(String msg, Commit... mergedCommits) throws GitletException {
        if (msg.isBlank()) {
            throw new GitletException("Please enter a commit message.");
        }
        if (stageForAddition.isEmpty() && stageForRemoval.isEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        // 取出当前 Commit
        Commit curCommit = Branch.popHead();
        // 设置新 Commit 的父节点为 当前 Commit
        ArrayList<String> parents = new ArrayList<>();
        parents.add(curCommit.getUID());
        for (Commit commit : mergedCommits) {
            parents.add(commit.getUID());
        }
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
        Branch.updateHead(newCommit.getUID());
    }

    public static void rm(String path) throws GitletException {
        File file = Paths.get(path).isAbsolute() ? new File(path) : Utils.join(CWD, path);
        Commit curCommit = Branch.popHead();

        if (stageForAddition.contains(file.getPath())) {
            stageForAddition.remove(file.getPath());
        } else if (curCommit.contains(file.getPath())) {
            Blob blob = Blob.fromFile(curCommit.get(file.getPath()));
            stageForRemoval.add(blob);
            Utils.restrictedDelete(file);
        } else {
            throw new GitletException("No reason to remove the file.");
        }
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
        Commit curCommit = Branch.popHead();
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
        if (!Branch.exist(branchName)) {
            throw new GitletException("No such branch exists.");
        }
        if (Branch.curBranch().equals(branchName)) {
            throw new GitletException("No need to checkout the current branch.");
        }
        if (!untrackedFiles().isEmpty()) {
            throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        Commit newCommit = Branch.popBranch(branchName);
        for (String uid : newCommit.dCloneBlobMap().values()) {
            Blob blob = Blob.fromFile(uid);
            blob.writeBack();
        }

        for (String name : Utils.plainFilenamesIn(CWD)) {
            File file = Utils.join(CWD, name);
            if (!newCommit.contains(file.getPath())) {
                Utils.restrictedDelete(file);
            }
        }

        stageForAddition.clear();
        stageForRemoval.clear();
        Branch.checkout(branchName);
    }

    public static void log() {
        Commit curCommit = Branch.popHead();
        do {
            System.out.print(curCommit);
            curCommit = curCommit.getFirstParent();
        } while (curCommit != null);
    }

    public static void global_log() {
        for (Commit commit : Commit.fromFileAll()) {
            System.out.println(commit);
        }
    }

    public static void find(String commitMessage) throws GitletException {
        boolean found = false;
        for (Commit commit : Commit.fromFileAll()) {
            if (commit.getMessage().equals(commitMessage)) {
                System.out.println(commit.getUID());
                found = true;
            }
        }

        if (!found) {
            throw new GitletException("Found no commit with that message.");
        }
    }

    public static void status() {
        ArrayList<String> names = new ArrayList<>();
        Branch.status();

        System.out.println("=== Staged Files ===");
        for (String uid : stageForAddition.getBlobMap().values()) {
            Blob blob = Blob.fromFile(uid);
            names.add((blob.getFile().getName()));
        }
        Collections.sort(names);
        for (String name : names) {
            System.out.println(name);
        }
        System.out.println("");

        System.out.println("=== Removed Files ===");
        names.clear();
        for (String uid : stageForRemoval.getBlobMap().values()) {
            Blob blob = Blob.fromFile(uid);
            names.add(blob.getFile().getName());
        }
        Collections.sort(names);
        for (String name : names) {
            System.out.println(name);
        }
        System.out.println("");

        System.out.println("=== Modifications Not Staged For Commit ===");
        names = modifiedFiles();
        Collections.sort(names);
        for (String name : names) {
            System.out.print(name);
            File file = Utils.join(CWD, name);
            if (file.exists()) {
                System.out.println(" (modified)");
            } else {
                System.out.println(" (deleted)");
            }
        }
        System.out.println("");

        System.out.println("=== Untracked Files ===");
        names = untrackedFiles();
        Collections.sort(names);
        for (String name : names) {
            System.out.println(name);
        }
        System.out.println("");
    }

    public static void branch(String branchName) throws GitletException {
        if (Branch.exist(branchName)) {
            throw new GitletException("A branch with that name already exists.");
        }
        Branch.create(branchName);
    }

    public static void rm_branch(String branchName) throws GitletException {
        if (!Branch.exist(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        if (Branch.curBranch().equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        Branch.delete(branchName);
    }

    public static void reset(String commitId) throws GitletException {
        Commit commit = Commit.fromFileByPrefixUID(commitId);
        if (commit == null) {
            throw new GitletException("No commit with that id exists.");
        }
        if (!untrackedFiles().isEmpty()) {
            throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        for (String uid : commit.dCloneBlobMap().values()) {
            Blob blob = Blob.fromFile(uid);
            blob.writeBack();
        }

        for (String name : Utils.plainFilenamesIn(CWD)) {
            File file = Utils.join(CWD, name);
            if (!commit.contains(file.getPath())) {
                Utils.restrictedDelete(file);
            }
        }

        stageForAddition.clear();
        stageForRemoval.clear();
        Branch.updateHead(commit.getUID());
    }

    public static void merge(String givenBranchName) throws GitletException {
        if (!stageForAddition.isEmpty() || !stageForRemoval.isEmpty()) {
            throw new GitletException("You have uncommitted changes.");
        }
        if (!Branch.exist(givenBranchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        if (Branch.curBranch().equals(givenBranchName)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }

        Commit curCommit = Branch.popHead();
        Commit givenCommit = Branch.popBranch(givenBranchName);
        Commit lca = Commit.findLowestCommonAncestor(curCommit, givenCommit);
        if (checkMergeOverwrite(lca, curCommit, givenCommit)) {
            throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        if (lca.equals(givenBranchName)) {
            System.out.println("Given branch is an ancestor of the current branch.");
        } else if (lca.equals(curCommit)) {
            checkoutBranch(givenBranchName);
            System.out.println("Current branch fast-forwarded.");
        }

        // case 1:
        for (String path : curCommit.dCloneBlobMap().keySet()) {
            String uid = curCommit.get(path);
            if (lca.contains(path) && givenCommit.contains(path) &&
                lca.get(path).equals(uid) && !givenCommit.get(uid).equals(uid)) {
                checkoutCommitFile(givenCommit.getUID(), path);
                add(path);
            }
        }
        // case 2: do nothing
        // case 3: do nothing
        // case 4: do nothing
        // case 5:
        for (String path : givenCommit.dCloneBlobMap().keySet()) {
            if (!lca.contains(path) && !curCommit.contains(path)) {
                checkoutCommitFile(givenCommit.getUID(), path);
                add(path);
            }
        }
        // case 6:
        for (String path : curCommit.dCloneBlobMap().keySet()) {
            String uid = curCommit.get(path);
            if (lca.contains(path) && !givenCommit.contains(path)
                && lca.get(path).equals(uid)) {
                rm(path);
            }
        }
        // case 7: do nothing
        // case 8:
        boolean mergeConflict = false;
        for (String path : curCommit.dCloneBlobMap().keySet()) {
            String uid = curCommit.get(path);
            if (!lca.contains(path) && givenCommit.contains(path)
                && !givenCommit.get(path).equals(uid)) {
                mergeConflict = true;
                handleMergeConflict(path, curCommit, givenCommit);
            }
            if (lca.contains(path) && !givenCommit.contains(path)
                && !lca.get(path).equals(uid)) {
                mergeConflict = true;
                handleMergeConflict(path, curCommit, givenCommit);
            }
            if (lca.contains(path) && givenCommit.contains(path)
                && !lca.get(path).equals(uid) && !givenCommit.get(path).equals(uid)
                && !lca.get(path).equals(givenCommit.get(path))) {
                mergeConflict = true;
                handleMergeConflict(path, curCommit, givenCommit);
            }
        }
        for (String path : givenCommit.dCloneBlobMap().keySet()) {
            String uid = givenCommit.get(path);
            if (lca.contains(path) && !curCommit.contains(path)
                && !lca.get(path).equals(uid)) {
                mergeConflict = true;
                handleMergeConflict(path, curCommit, givenCommit);
            }
        }

        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        String msg = "Merged " + givenBranchName + " into " + Branch.curBranch() + ".";
        commit(msg, givenCommit);
    }

    private static boolean checkMergeOverwrite(Commit lca, Commit curCommit, Commit givenCommit) {
        for (String name : Utils.plainFilenamesIn(CWD)) {
            File file = Utils.join(CWD, name);
            String path = file.getPath();
            if (!curCommit.contains(path)) { // 这个 CWD 下的 file 在 curCommit 下是 untracked 的
                if (lca.contains(path) && givenCommit.contains(path)
                    && !lca.get(path).equals(givenCommit.get(path))) {   // 1: 可能被 case 8 的冲突 overwrite
                    return true;
                }
                if (!lca.contains(path) && givenCommit.contains(path)) { // 2: 可能被 case 5 的情况 overwrite
                    return true;
                }
            }
        }

        return false;
    }

    private static void handleMergeConflict(String path, Commit curCommit, Commit givenCommit) {
        StringBuilder sb = new StringBuilder();
        sb.append("<<<<<<< HEAD\n");
        if (curCommit.contains(path)) {
            String uid = curCommit.get(path);
            Blob blob = Blob.fromFile(uid);
            sb.append(blob.getContentAsString());
        }
        sb.append("=======\n");
        if (givenCommit.contains(path)) {
            String uid = givenCommit.get(path);
            Blob blob = Blob.fromFile(uid);
            sb.append(blob.getContentAsString());
        }
        sb.append(">>>>>>>");

        Utils.writeContents(new File(path), sb.toString());
        add(path);
    }

    /**        CWD Monitor Methods     **/
    private static ArrayList<String> untrackedFiles() {
        Commit curCommit = Branch.popHead();

        ArrayList<String> res = new ArrayList<>();

        for (String name : Utils.plainFilenamesIn(CWD)) {
            File file = Utils.join(CWD, name);
            // The final category (“Untracked Files”) is for files present in
            // the working directory but neither staged for addition nor tracked.
            if (!curCommit.contains(file.getPath()) && !stageForAddition.contains(file.getPath())) {
                res.add(name);
            }
        }

        return res;
    }

    private static ArrayList<String> modifiedFiles() {
        Commit curCommit = Branch.popHead();

        ArrayList<String> res = new ArrayList<>();

        for (String name : Utils.plainFilenamesIn(CWD)) {
            File file = Utils.join(CWD, name);
            Blob blob = new Blob(file);
            // 1: Tracked in the current commit, changed in the working directory, but not staged;
            if (curCommit.contains(file.getPath()) && !stageForAddition.contains(file.getPath())
                && !curCommit.get(file.getPath()).equals(blob.getUID())) {
                res.add(name);
            }
            // 2: Staged for addition, but with different contents than in the working directory; or
            if (stageForAddition.contains(file.getPath())
                    && !stageForAddition.get(file.getPath()).equals(blob.getUID())) {
                res.add(name);
            }
        }

        // 3: Staged for addition, but deleted in the working directory;
        for (String path : stageForAddition.getBlobMap().keySet()) {
            File file = new File(path);
            if (!file.exists()) {
                res.add(file.getName());
            }
        }

        // 4: Not staged for removal, but tracked in the current commit and deleted from the working directory.
        for (String path : curCommit.dCloneBlobMap().keySet()) {
            File file = new File(path);
            if (!stageForRemoval.contains(path) && !file.exists()) {
                res.add(file.getName());
            }
        }
        return res;
    }


}
