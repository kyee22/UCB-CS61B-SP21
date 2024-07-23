package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RemoteRepository {
    static final File REMOTE_DIR = Utils.join(GitletRepository.REFS_DIR, "remote");

    public static void init() {
        REMOTE_DIR.mkdir();
    }

    public static boolean exist(String remoteName) {
        for (String name : Utils.plainFilenamesIn(REMOTE_DIR)) {
            if (name.equals(remoteName)) {
                return true;
            }
        }

        return false;
    }

    public static void create(String remoteName, String remotePath) {
        File file = Utils.join(REMOTE_DIR, remoteName);
        String fixedRemotePath = remotePath.replace("/", File.separator);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println(e + ": can not create " + file);
        }
        Utils.writeContents(file, fixedRemotePath);
    }

    public static void delete(String remoteName) {
        File file = Utils.join(REMOTE_DIR, remoteName);
        file.delete();
    }


    public static void entry(String... args) throws GitletException {
        switch (args[0]) {
            case "add-remote":
                validateNumArgs(args, 3);
                addRemote(args[1], args[2]);
                break;
            case "rm-remote":
                validateNumArgs(args, 2);
                rmRemote(args[1]);
                break;
            case "push":
                validateNumArgs(args, 3);
                push(args[1], args[2]);
                break;
            case "fetch":
                validateNumArgs(args, 3);
                fetch(args[1], args[2]);
                break;
            case "pull":
                validateNumArgs(args, 3);
                pull(args[1], args[2]);
                break;
            default:
                throw new GitletException("No command with that name exists.");
        }
    }

    private static void addRemote(String remoteName, String remotePath) throws GitletException {
        if (exist(remoteName)) {
            throw new GitletException("A remote with that name already exists.");
        }

        create(remoteName, remotePath);
    }

    private static void rmRemote(String remoteName) throws GitletException {
        if (!exist(remoteName)) {
            throw new GitletException("A remote with that name does not exist.");
        }

        delete(remoteName);
    }

    private static void push(String remoteName, String branchName) throws GitletException {
        if (!exist(remoteName)) {
            throw new GitletException("A remote with that name does not exist.");
        }
        String remotePath = readRemotePath(remoteName);
        File remoteGielet = new File(remotePath);
        if (!remoteGielet.exists() || !remoteGielet.getName().equals(".gitlet")) {
            throw new GitletException("Remote directory not found.");
        }

        Commit curCommit = Branch.popHead();
        Commit remoteCommit;

        if (!remoteBranchExist(remotePath, branchName)) {
            remoteCreateBranch(remotePath, branchName);
        } else {
            remoteCommit = headFromRemoteBranch(remotePath, branchName);
            if (!checkInHistory(curCommit, remoteCommit)) {
                throw new GitletException("Please pull down remote changes before pushing.");
            }
        }

        pushObjects(remotePath);
        pushBranch(remotePath, branchName, curCommit);
        File remoteCwd = remoteGielet.getParentFile();

        remoteCommit = headFromRemoteBranch(remotePath, branchName);
        for (String path : remoteCommit.dCloneBlobMap().keySet()) {
            File file = new File(path);
            String uid = remoteCommit.dCloneBlobMap().get(path);
            String name = file.getName();
            file = Utils.join(remotePath, "objects", "blobs", uid);
            Blob blob = Utils.readObject(file, Blob.class);
            File target = Utils.join(remoteCwd, name);
            Utils.writeContents(target, blob.getContentAsString());
        }

        //for (String name : Utils.plainFilenamesIn(remoteCwd)) {
        //    File file = Utils.join(GitletRepository.CWD, name);
        //    String path = file.getPath();
        //    if (!remoteCommit.contains(path)) {
        //        file = Utils.join(remoteCwd, name);
        //        file.delete();
        //    }
        //}

        Stage stage = new Stage();
        File file = Utils.join(remotePath, "index", "stage_for_add");
        Utils.writeObject(file, stage);
        stage = new Stage();
        file = Utils.join(remotePath, "index", "stage_for_rm");
        Utils.writeObject(file, stage);
    }

    private static void fetch(String remoteName, String branchName) throws GitletException {
        if (!exist(remoteName)) {
            throw new GitletException("A remote with that name does not exist.");
        }
        String remotePath = readRemotePath(remoteName);
        File remoteGielet = new File(remotePath);
        if (!remoteGielet.exists() || !remoteGielet.getName().equals(".gitlet")) {
            throw new GitletException("Remote directory not found.");
        }
        if (!remoteBranchExist(remotePath, branchName)) {
            throw new GitletException("That remote does not have that branch.");
        }
        Commit remoteCommit = headFromRemoteBranch(remotePath, branchName);
        pullObjects(remotePath);

        File branchFile = Utils.join(Branch.BRANCHES_DIR, remoteName + "_" + branchName);
        if (!branchFile.exists()) {
            try {
                branchFile.createNewFile();
            } catch (IOException e) {
                System.out.println("shit!" + e);
            }
        }
        Utils.writeContents(branchFile, remoteCommit.getUID());
    }

    private static void pull(String remoteName, String branchName) throws GitletException {
        fetch(remoteName, branchName);
        String fixedBranchName = remoteName + "_" + branchName;
        //GitletRepository.merge(_branchName);    // wrong!! 直接从这里进去的话 stage 还没有 pop
        String[] args = {"merge", fixedBranchName};
        GitletRepository.entry(args);             // correct!!
    }

    private static String readRemotePath(String remoteName) {
        File file = Utils.join(REMOTE_DIR, remoteName);
        return Utils.readContentsAsString(file);
    }


    private static void validateNumArgs(String[] args, int... allowedLengths)
            throws GitletException {
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

    private static boolean remoteBranchExist(String path, String branchName) {
        File file = Utils.join(path, "refs", "branches", branchName);
        return file.exists();
    }

    private static void remoteCreateBranch(String path, String branchName) {
        File file = Utils.join(path, "refs", "branches", branchName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("can not create " + file);
        }
    }

    private static Commit headFromRemoteBranch(String path, String branchName) {
        File headBranchFile = Utils.join(path, "refs", "branches", branchName);
        String commitId = Utils.readContentsAsString(headBranchFile);
        File commitFile = Utils.join(path, "objects", "commits", commitId);
        return Utils.readObject(commitFile, Commit.class);
    }


    private static boolean checkInHistory(Commit commit, Commit target) {
        if (commit == null) {
            return false;
        }
        if (commit.equals(target)) {
            return true;
        }
        return checkInHistory(commit.getFirstParent(), target)
                || checkInHistory(commit.getSecondParent(), target);
    }

    private static void pushObjects(String path) {
        try {
            for (String name : Utils.plainFilenamesIn(Commit.COMMIT_DIR)) {
                File src = Utils.join(Commit.COMMIT_DIR, name);
                Path srcPath = src.toPath();
                File dst = Utils.join(path, "objects", "commits", name);
                Path dstPath = dst.toPath();
                Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            }
            for (String name : Utils.plainFilenamesIn(Blob.BLOB_DIR)) {
                File src = Utils.join(Blob.BLOB_DIR, name);
                Path srcPath = src.toPath();
                File dst = Utils.join(path, "objects", "blobs", name);
                Path dstPath = dst.toPath();
                Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.out.println("Something went wrong when copy files: " + e);
        }
    }

    private static void pullObjects(String path) {
        try {
            File dir = Utils.join(path, "objects", "commits");
            for (String name : Utils.plainFilenamesIn(dir)) {
                File dst = Utils.join(Commit.COMMIT_DIR, name);
                if (dst.exists()) {
                    continue;
                }
                Path dstPath = dst.toPath();
                File src = Utils.join(path, "objects", "commits", name);
                Path srcPath = src.toPath();
                Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            }
            dir = Utils.join(path, "objects", "blobs");
            for (String name : Utils.plainFilenamesIn(dir)) {
                File dst = Utils.join(Blob.BLOB_DIR, name);
                if (dst.exists()) {
                    continue;
                }
                Path dstPath = dst.toPath();
                File src = Utils.join(path, "objects", "blobs", name);
                Path srcPath = src.toPath();
                Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.out.println("Something went wrong when copy files: " + e);
        }
    }

    private static void pushBranch(String path, String branchName, Commit commit) {
        File remoteHeadFile = Utils.join(path, "HEAD");
        Utils.writeContents(remoteHeadFile, branchName);
        File remoteBranchFile = Utils.join(path, "refs", "branches", branchName);
        Utils.writeContents(remoteBranchFile, commit.getUID());
    }
}
