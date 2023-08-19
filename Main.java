package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        Repository repo = new Repository(System.getProperty("user.dir"), args);
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                repo.checkIncorrectOps(args, 1);
                repo.init();
                break;
            case "add":
                repo.checkIncorrectOps(args, 2);
                repo.checkGitletDir();
                repo.add(args[1]);
                break;
            case "commit":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 2);
                repo.commit(args[1], null);
                break;
            case "checkout":
                repo.checkGitletDir();
                if ((args.length == 3) && (args[1].equals("--"))) {
                    repo.checkout(args[2]);
                } else if ((args.length == 4) && (args[2].equals("--"))) {
                    repo.checkout(args[1], args[3]);
                } else if (args.length == 2) {
                    repo.checkoutBranch(args[1]);
                } else {
                    repo.checkoutElse();
                }
                break;
            case "rm":
                repo.checkIncorrectOps(args, 2);
                repo.checkGitletDir();
                repo.rm(args[1]);
                break;
            case "log":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 1);
                repo.log();
                break;
            case "global-log":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 1);
                repo.globalLog();
                break;
            case "find":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 2);
                repo.find(args[1]);
                break;
            case "status":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 1);
                repo.status();
                break;
            case "branch":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 2);
                repo.branch(args[1]);
                break;
            case "rm-branch":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 2);
                repo.rmBranch(args[1]);
                break;
            case "reset":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 2);
                repo.reset(args[1]);
                break;
            case "merge":
                repo.checkGitletDir();
                repo.checkIncorrectOps(args, 2);
                repo.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }


}
