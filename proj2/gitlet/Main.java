package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author     Arren @ Beihang University
 *  @Time       Jul, 2024
 *
 *  Pre-requisite: lab06
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        panic(args.length <= 0, "Please enter a command.");
        String firstArg = args[0];

        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
                try {
                    GitletRepository.init();
                } catch (GitletException e) {
                    panic(true, e.getMessage());
                }
                break;

            case "add":
            case "commit":
            case "checkout":
            case "rm":
            case "log":
            case "global-log":
            case "find":
            case "status":
            case "branch":
            case "rm-branch":
            case "reset":
            case "merge":
                validateGitletExist();
                try {
                    GitletRepository.entry(args);
                } catch (GitletException e) {
                    panic(true, e.getMessage());
                }
                break;

            case "add-remote":
            case "rm-remote":
            case "push":
            case "fetch":
            case "pull":
                try {
                    RemoteRepository.entry(args);
                } catch (GitletException e) {
                    panic(true, e.getMessage());
                }
                break;

            default:
                panic(true, "No command with that name exists.");
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param args Argument array from command line
     * @param allowedLengths Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int... allowedLengths) {
        boolean isValidLength = false;
        for (int length : allowedLengths) {
            if (args.length == length) {
                isValidLength = true;
                break;
            }
        }
        panic(!isValidLength, "Incorrect operands.");
    }

    public static void validateGitletExist() {
        panic(!GitletRepository.GITLET_DIR.exists(),"Not in an initialized Gitlet directory.");
    }

    public static void panic(boolean cond, String msg) {
        if (cond) {
            System.out.println(msg);
            System.exit(0);
        }
    }
}
