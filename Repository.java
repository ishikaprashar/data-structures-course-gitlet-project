package gitlet;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 * Handles all commands for gitlet version control
 *  @author Ishika Prashar
 */
public class Repository {
    /** The current working directory. */
    private File CWD;
    /** The .gitlet directory. */
    private  File GITLET_DIR;
    /** The staging area file to store stage_add and stage_remove for persistence. */
    private  final File STAGING_AREA;
    /** the commit directory to store all commit objects with name as their sha1 id*/
    private  final File COMMITS;
    /** the branch directory to store all brnaches as their respective names*/
    private final File BRANCHES;
    /** blobs folder to hold all blob files ever */
    private final File BLOB_FOLDER;


    /**
     * error initialized repo check
     */
    public void checkGitletDir() {
        if (!(this.GITLET_DIR.exists())) {
            System.out.println("Not in an initialized Gitlet directory");
            System.exit(0);
        }
    }

    /**
     * error incorrect ops check
     * @param args array of arguments
     */
    public void checkIncorrectOps(String[] args, Integer num) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /**
     * checkout else case
     */
    public void checkoutElse() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }


    /**
     * repo method to create folder and file paths.
     * @param currwd current working directory string of user
     */
    public Repository(String currwd, String[] args) {
        CWD = new File(currwd);
        GITLET_DIR = join(CWD, ".gitlet");
        STAGING_AREA = join(GITLET_DIR, "staging area");
        COMMITS = join(GITLET_DIR, "commits");
        BRANCHES = join(GITLET_DIR, "branches");
        BLOB_FOLDER = join(GITLET_DIR, "blobs");
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
    }

    /**set up persistence and create initial commit. */
    public void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMITS.mkdir();
        BLOB_FOLDER.mkdir();
        BRANCHES.mkdir();
        Commit initialCommit = new Commit("initial commit", null, null);
        File master = join(BRANCHES, "master");
        File head = join(BRANCHES, "HEAD");
        File currentBranch = join(BRANCHES, "current");
        createFileTryCatch(master);
        createFileTryCatch(head);
        createFileTryCatch(currentBranch);

        File commitID = join(COMMITS, getCommitID(initialCommit));
        createFileTryCatch(commitID);
        writeObject(commitID, initialCommit);

        writeContents(master, getCommitID(initialCommit));
        writeContents(head, getCommitID(initialCommit));
        writeContents(currentBranch, "master");
        createFileTryCatch(STAGING_AREA);
        Stage stageObject = new Stage();
        writeObject(STAGING_AREA, stageObject);
    }

    /**
     * get shah1 id of a commit object
     * @param c commit object
     * @return shah1 id
     */
    private String getCommitID(Commit c) {
        byte[] serial = serialize(c);
        return sha1(serial);
    }

    /**
     * handle the try catch exception when using .createNewFile() method.
     * @param name File to be created
     */
    private static void createFileTryCatch(File name) {
        try {
            name.createNewFile();
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /***
     * add file to staging area if it is not already
     * in staging area or current commit
     * @param name file name of file to be added
     */
    public void add(String name) {
        File namedFile = join(this.CWD, name);
        Boolean removed = false;
        if (!(namedFile.exists())) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String idCommit = readContentsAsString(join(BRANCHES, "HEAD"));
        Stage stager = readObject(STAGING_AREA, Stage.class);
        if (stager.getRemoveBlob().containsKey(name)) {
            String rmm = stager.getRemoveBlob().remove(name);
            removed = true;
        }
        Commit c = readObject(join(COMMITS, idCommit), Commit.class);
        if (c.getBlobMap().containsKey(name)) {
            String blobID = c.getBlobMap().get(name);
            File blobFile = join(BLOB_FOLDER, blobID);
            if (Arrays.equals(readContents(blobFile), readContents(namedFile))) {
                if (removed) {
                    writeObject(STAGING_AREA, stager);
                }
                System.exit(0);
            }
        }
        byte[] fileContent = readContents(namedFile);
        File newBlob = join(BLOB_FOLDER, sha1(fileContent));
        createFileTryCatch(newBlob);
        writeContents(newBlob, fileContent);
        stager.getAddBlob().put(name, sha1(fileContent));
        writeObject(STAGING_AREA, stager);
    }

    /**
     * save snapshot of tracked files in current commit and staging area.
     * @param msg string associated with commit call
     * @source https://www.geeksforgeeks.org/iterate-map-java/ to iterate hashmap
     */
    public void commit(String msg, String secondParent) {
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Stage stager = readObject(STAGING_AREA, Stage.class);
        if (stager.getAddBlob().isEmpty() && stager.getRemoveBlob().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        TreeMap<String, String> stagingArea = stager.getAddBlob();
        Commit c = new Commit(msg, readContentsAsString(join(BRANCHES, "HEAD")), secondParent);
        String oldHEAD = readContentsAsString(join(BRANCHES, "HEAD"));
        Commit oldHeadObj = readObject(join(COMMITS, oldHEAD), Commit.class);
        TreeMap<String, String> newMapshallow = oldHeadObj.getBlobMap();
        TreeMap<String, String> newMapdeep = new TreeMap<>();
        newMapdeep.putAll(newMapshallow);
        for (Map.Entry<String, String> entry: stagingArea.entrySet()) {
            if (newMapdeep.containsKey(entry.getKey())) {
                if (!(newMapdeep.get(entry.getKey()).equals(entry.getValue()))) {
                    newMapdeep.put(entry.getKey(), entry.getValue());
                }
            } else {
                newMapdeep.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry: stager.getRemoveBlob().entrySet()) {
            if (newMapdeep.containsKey(entry.getKey())) {
                newMapdeep.remove(entry.getKey());
            }
        }
        clearStagearea();
        c.getBlobMap().putAll(newMapdeep);

        File commitID = join(COMMITS, getCommitID(c));
        createFileTryCatch(commitID);
        writeObject(commitID, c);

        writeContents(join(BRANCHES, "HEAD"), getCommitID(c));
        String currBranch = readContentsAsString(join(BRANCHES, "current"));
        writeContents(join(BRANCHES, currBranch), getCommitID(c));
    }

    /**
     * empty the hashmaps (both add and remove) in the staging area.
     * */
    private void clearStagearea() {
        Stage stager = readObject(STAGING_AREA, Stage.class);
        stager.getAddBlob().clear();
        stager.getRemoveBlob().clear();
        writeObject(STAGING_AREA, stager);
    }

    /**
     * take version of file from head commit and put in CWD
     * @param name string file name
     */
    public void checkout(String name) {
        String headId = readContentsAsString(join(BRANCHES, "HEAD"));
        Commit comm = readObject(join(COMMITS, headId), Commit.class);
        if (!(comm.getBlobMap().containsKey(name))) {
            System.out.println("File does not exist in that commit");
            System.exit(0);
        } else {
            File currVersion = join(CWD, name);
            createFileTryCatch(currVersion);
            String toUpdate = readContentsAsString(join(BLOB_FOLDER, comm.getBlobMap().get(name)));
            writeContents(currVersion, toUpdate);
        }
    }

    /**
     * take version of file from commit given as commitID, and put in CWD
     * @param id string of commit id
     * @param name of file to be checked out
     */
    public void checkout(String id, String name) {
        String idCheck = checkShortId(id);
        if (idCheck == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            Commit commchk = readObject(join(COMMITS, idCheck), Commit.class);
            if (!(commchk.getBlobMap().containsKey(name))) {
                System.out.println("File does not exist in that commit");
                System.exit(0);
            } else {
                File currVersion = join(CWD, name);
                createFileTryCatch(currVersion);
                String toUpdate = readContentsAsString(join(BLOB_FOLDER,
                        commchk.getBlobMap().get(name)));
                writeContents(currVersion, toUpdate);
            }
        }
    }

    /**
     * check if shortened commit id matches one in commits folder
     * @param id string commit id
     */
    private String checkShortId(String id) {
        List<String> lstCommits = plainFilenamesIn(COMMITS);
        for (String s: lstCommits) {
            if (s.startsWith(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * display information about each commit starting at HEAD going backwards
     * @source https://howtodoinjava.com/java/string/get-first-4-characters/
     */
    public void log() {
        String headid = readContentsAsString(join(BRANCHES, "HEAD"));
        Commit head = readObject(join(COMMITS, headid), Commit.class);
        String output = null;
        if (head.getParent().get(1) != null) {
            String first = " " + head.getParent().get(0).substring(0, 7) + " ";
            String second = head.getParent().get(1).substring(0, 7);
            output = "===" + "\n" + "commit " + headid + "\n" + "Merge:" + first + second + "\n"
                    + "Date: " + head.getTimeStamp() + "\n" + head.getMessage() + "\n";
        } else {
            output = "===" + "\n" + "commit " + headid + "\n" + "Date: "
                    + head.getTimeStamp() + "\n" + head.getMessage() + "\n";
        }
        while (head.getParent().get(0) != null) {
            String parentId = head.getParent().get(0);
            Commit parent = readObject((join(COMMITS, parentId)), Commit.class);
            if (parent.getParent().get(1) != null) {
                String firstParent = " " + parent.getParent().get(0).substring(0, 7) + " ";
                String secondParent = parent.getParent().get(1).substring(0, 7);
                output = output + "\n" + "===" + "\n" + "commit " + parentId + "\n"
                        + "Merge:" + firstParent + secondParent + "\n" + "Date: "
                        + parent.getTimeStamp() + "\n" + parent.getMessage() + "\n";
            } else {
                output = output + "\n" + "===" + "\n" + "commit " + parentId + "\n"
                        + "Date: " + parent.getTimeStamp() + "\n" + parent.getMessage() + "\n";
            }
            head = parent;
        }
        System.out.println(output);
    }


    /**
     * untrack file if in head commit and remove from add staging area if present
     * @param name string of file name
     */
    public void rm(String name) {
        File toRm = join(this.CWD, name);
        Stage stager = readObject(STAGING_AREA, Stage.class);
        String headid = readContentsAsString(join(BRANCHES, "HEAD"));
        Commit c = readObject(join(COMMITS, headid), Commit.class);
        TreeMap<String, String> commMap = c.getBlobMap();
        if (stager.getRemoveBlob().containsKey(name)) {
            System.exit(0);
        } else if (stager.getAddBlob().containsKey(name) && commMap.containsKey(name)) {
            stager.getAddBlob().remove(name);
            stager.getRemoveBlob().put(name, commMap.get(name));
            restrictedDelete(toRm);
            writeObject(STAGING_AREA, stager);
        } else if (stager.getAddBlob().containsKey(name)) {
            stager.getAddBlob().remove(name);
            writeObject(STAGING_AREA, stager);
        } else if (commMap.containsKey(name)) {
            stager.getRemoveBlob().put(name, commMap.get(name));
            restrictedDelete(toRm);
            writeObject(STAGING_AREA, stager);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /**
     * like log, except displays information about all
     * commits ever made in random order.
     * @source https://stackoverflow.com/questions/30708036/
     *  delete-the-last-two-characters-of-the-string
     */
    public void globalLog() {
        String output = "";
        for (String cName: plainFilenamesIn(COMMITS)) {
            Commit c = readObject(join(COMMITS, cName), Commit.class);
            if (c.getParent().get(1) != null) {
                String firstParent = " " + c.getParent().get(0).substring(0, 7) + " ";
                String secondParent = c.getParent().get(1).substring(0, 7);
                output = output + "\n" + "===" + "\n" + "commit " + cName + "\n"
                        + "Merge:" + firstParent + secondParent + "\n" + "Date: " + c.getTimeStamp()
                        + "\n" + c.getMessage() + "\n";
            } else {
                output = output + "===" + "\n" + "commit " + cName + "\n"
                        + "Date: " + c.getTimeStamp() + "\n" + c.getMessage() + "\n" + "\n";
            }
        }
        System.out.println(output.substring(0, output.length() - 1));
    }


    /**
     * Prints out the ids of all commits that have the given commit message.
     * @source https://stackoverflow.com/questions/30708036/
     * delete-the-last-two-characters-of-the-string
     */
    public void find(String msg) {
        String output = "";
        for (String cName: plainFilenamesIn(COMMITS)) {
            Commit c = readObject(join(COMMITS, cName), Commit.class);
            if (c.getMessage().equals(msg)) {
                output = output + cName + "\n";
            }
        }
        if (output.equals("")) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        System.out.println(output.substring(0, output.length() - 1));
    }


    /**
     * Display all currently existing branches, with * for current branch.
     * @source 61b ed - Collections.sort method
     */
    public void status() {
        String output = "=== Branches ===" + "\n";
        List<String> branched = plainFilenamesIn(BRANCHES);
        Collections.sort(branched);
        String currBranch = readContentsAsString(join(BRANCHES, "current"));
        for (String branch: branched) {
            if ((!branch.equals("HEAD")) && (!branch.equals("current"))) {
                if (branch.equals(currBranch)) {
                    output = output + "*" + branch + "\n";
                } else {
                    output = output + branch + "\n";
                }
            }
        }
        output = output + "\n" + "=== Staged Files ===" + "\n";
        Stage stager = readObject(STAGING_AREA, Stage.class);
        TreeMap<String, String> addstage = stager.getAddBlob();
        List<String> keyList = new ArrayList<>(addstage.keySet());
        Collections.sort(keyList);
        for (String name: keyList) {
            output = output + name + "\n";
        }
        output = output + "\n" + "=== Removed Files ===" + "\n";
        TreeMap<String, String> removestage = stager.getRemoveBlob();
        List<String> rkeyList = new ArrayList<>(removestage.keySet());
        Collections.sort(rkeyList);
        for (String name: rkeyList) {
            output = output + name + "\n";
        }
        output = output + "\n" + "=== Modifications Not Staged For Commit ===" + "\n";
        List<String> filenames = new ArrayList<>();
        String headid = readContentsAsString(join(BRANCHES, "HEAD"));
        Commit head = readObject(join(COMMITS, headid), Commit.class);
        for (String name: head.getBlobMap().keySet()) {
            File check = join(CWD, name);
            if (check.exists() && (!stager.getAddBlob().containsKey(name))) {
                if (!readContentsAsString(join(BLOB_FOLDER,
                        head.getBlobMap().get(name))).equals(readContentsAsString(check))) {
                    filenames.add(name + " (modified)");
                }
            } else {
                if (!stager.getRemoveBlob().containsKey(name)) {
                    filenames.add(name + " (deleted)");
                }
            }
        }
        for (String name: stager.getAddBlob().keySet()) {
            File check = join(CWD, name);
            if (check.exists()) {
                if (!readContentsAsString(join(BLOB_FOLDER,
                        stager.getAddBlob().get(name))).equals(readContentsAsString(check))) {
                    filenames.add(name + " (modified)");
                }
            } else {
                filenames.add(name + " (deleted)");
            }
        }
        Collections.sort(filenames);
        for (String name: filenames) {
            output = output + name + "\n";
        }
        output = output + "\n" + "=== Untracked Files ===" + "\n";
        List<String> untrackedfiles = new ArrayList<>();
        List<String> allCWD = plainFilenamesIn(CWD);
        for (String name: allCWD) {
            if (!stager.getAddBlob().containsKey(name)) {
                if (!head.getBlobMap().containsKey(name)) {
                    untrackedfiles.add(name);
                }
            }
            if (stager.getRemoveBlob().containsKey(name)) {
                untrackedfiles.add(name);
            }
        }
        Collections.sort(untrackedfiles);
        for (String name: untrackedfiles) {
            output = output + name + "\n";
        }
        System.out.println(output);
    }

    /**
     * @param branch name of branch to check out (make head?)
     *Takes all files in the commit at the head of the given branch,
     *               and puts them in the working directory.
     */
    public void checkoutBranch(String branch) {
        if (!(plainFilenamesIn(BRANCHES).contains(branch))) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (readContentsAsString(join(BRANCHES, "current")).equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String givenid = readContentsAsString(join(BRANCHES, branch));
        Commit given = readObject(join(COMMITS, givenid), Commit.class);

        String currid = readContentsAsString(join(BRANCHES, "HEAD"));
        Commit curr = readObject(join(COMMITS, currid), Commit.class);

        if (untrackedError(curr, given)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }
        for (String file: given.getBlobMap().keySet()) {
            checkout(givenid, file);
        }
        for (String file: curr.getBlobMap().keySet()) {
            if (!given.getBlobMap().containsKey(file)) {
                restrictedDelete(join(CWD, file));
            }
        }
        writeContents(join(BRANCHES, "HEAD"), givenid);
        writeContents(join(BRANCHES, "current"), branch);
        clearStagearea();
    }

    /**
     * returns true if untracked file in the way
     * @param curr current commit head
     * @param given commit head
     * @return
     */
    private boolean untrackedError(Commit curr, Commit given) {
        for (String file: plainFilenamesIn(CWD)) {
            if (!curr.getBlobMap().containsKey(file)) {
                if (given.getBlobMap().containsKey(file)) {
                    String cwdContents = readContentsAsString(join(CWD, file));
                    String givenContents = readContentsAsString(join(BLOB_FOLDER,
                            given.getBlobMap().get(file)));
                    if (!cwdContents.equals(givenContents)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *Creates a new branch with the given name, and points it at the current head commit.
     * @param name branch name
     */
    public void branch(String name) {
        if (plainFilenamesIn(BRANCHES).contains(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File newBranch = join(BRANCHES, name);
        createFileTryCatch(newBranch);
        String id = readContentsAsString(join(BRANCHES, "HEAD"));
        writeContents(newBranch, id);
    }


    /**
     *Deletes the branch with the given name.
     * @param branch name of branch
     */
    public void rmBranch(String branch) {
        if (!plainFilenamesIn(BRANCHES).contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String currBranch = readContentsAsString(join(BRANCHES, "current"));
        if (currBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        join(BRANCHES, branch).delete();
    }

    /**
     * resets file state to that given by commit id.
     * @param id of commit
     */
    public void reset(String id) {
        String idCheck = checkShortId(id);
        if (idCheck == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commchk = readObject(join(COMMITS, idCheck), Commit.class);
        String currId = readContentsAsString(join(BRANCHES, "HEAD"));
        Commit current = readObject(join(COMMITS, currId), Commit.class);

        if (untrackedError(current, commchk)) {
            System.out.println("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
            System.exit(0);
        }

        for (String file: commchk.getBlobMap().keySet()) {
            checkout(idCheck, file);
        }
        for (String file: current.getBlobMap().keySet()) {
            if (!commchk.getBlobMap().containsKey(file)) {
                restrictedDelete(join(CWD, file));
            }
        }
        writeContents(join(BRANCHES, "HEAD"), idCheck);
        String currBranch = readContentsAsString(join(BRANCHES, "current"));
        writeContents(join(BRANCHES, currBranch), idCheck);
        clearStagearea();
    }

    /**
     * helper method for merge errors resulting in exits
     * @param stager stage area
     * @param branch name of branch to merge check error
     */
    private void mergeErrors(Stage stager, String branch) {
        if ((!stager.getRemoveBlob().isEmpty()) || (!stager.getAddBlob().isEmpty())) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!plainFilenamesIn(BRANCHES).contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (readContentsAsString(join(BRANCHES, "current")).equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /**
     * exit if split pt error
     * @param splitIDd split pt sha1
     * @param branch string name
     */
    private void splitpterror(String splitIDd, String branch) {
        String currBranch = readContentsAsString(join(BRANCHES, "current"));
        String currid = readContentsAsString(join(BRANCHES, currBranch));
        String givenid = readContentsAsString(join(BRANCHES, branch));
        if (currid.equals(splitIDd)) {
            checkoutBranch(branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        if (givenid.equals(splitIDd)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
    }

    /**
     * handle untrack exit case
     * @param currentHead commit
     * @param givenHead commit
     */
    private void untrackk(Commit currentHead, Commit givenHead) {
        if (untrackedError(currentHead, givenHead)) {
            System.out.println("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
            System.exit(0);
        }
    }

    /**
     *Merges files from the given branch into the current branch.
     * @param branch name of branch to merge
     */
    public void merge(String branch) {
        Stage stager = readObject(STAGING_AREA, Stage.class);
        boolean mergeConflict = false;
        mergeErrors(stager, branch);
        String currBranch = readContentsAsString(join(BRANCHES, "current"));
        String currid = readContentsAsString(join(BRANCHES, currBranch));
        Commit currentHead = readObject(join(COMMITS, currid), Commit.class);
        String givenid = readContentsAsString(join(BRANCHES, branch));
        Commit givenHead = readObject(join(COMMITS, givenid), Commit.class);
        Commit splitPoint = bfsFindSplitPoint(givenHead, currentHead);
        String splitIDd = getCommitID(splitPoint);
        untrackk(currentHead, givenHead);
        splitpterror(splitIDd, branch);
        if (mergeCaseChecks(splitPoint, currentHead, givenHead, givenid)) {
            mergeConflict = true;
        }
        if (currentnonsplit(currentHead, splitPoint, givenHead)) {
            mergeConflict = true;
        }
        if (nonsplitptFiles(givenHead, splitPoint, currentHead, givenid)) {
            mergeConflict = true;
        }
        commit("Merged " + branch + " into " + currBranch + ".", givenid);
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * main merge work of checking files
     * @param splitPoint commit
     * @param currentHead commit
     * @param givenHead commit
     * @param givenid commit
     * @return boolean merge conflict
     */
    private Boolean mergeCaseChecks(Commit splitPoint, Commit currentHead,
                                    Commit givenHead, String givenid) {
        boolean mergeConflict = false;
        for (String file: splitPoint.getBlobMap().keySet()) {
            File splitBlob = join(BLOB_FOLDER, splitPoint.getBlobMap().get(file));
            byte[] splitContents = readContents(splitBlob);
            if (currentHead.getBlobMap().containsKey(file)
                    && givenHead.getBlobMap().containsKey(file)) {
                File currBlob = join(BLOB_FOLDER, currentHead.getBlobMap().get(file));
                byte[] currContents = readContents(currBlob);
                File givenBlob = join(BLOB_FOLDER, givenHead.getBlobMap().get(file));
                byte[] givenContents = readContents(givenBlob);
                if ((Arrays.equals(currContents, splitContents))
                        && (!Arrays.equals(givenContents, splitContents))) {
                    if (join(CWD, file).exists()) {
                        checkout(givenid, file);
                        add(file);
                    }
                }
                if ((!Arrays.equals(currContents, splitContents))
                        && (Arrays.equals(givenContents, splitContents))) {
                    continue;
                }
                if ((!Arrays.equals(currContents, splitContents))
                        && (!Arrays.equals(givenContents, splitContents))) {
                    if (Arrays.equals(currContents, givenContents)) {
                        continue;
                    }
                    if (!Arrays.equals(currContents, givenContents)) {
                        String currentID = currentHead.getBlobMap().get(file);
                        String currentContent = readContentsAsString(join(BLOB_FOLDER, currentID));
                        String givenID = givenHead.getBlobMap().get(file);
                        String givenContent = readContentsAsString(join(BLOB_FOLDER, givenID));
                        mergeConflict = true;
                        String toReplace = "<<<<<<< HEAD\n" + currentContent + "=======\n"
                                + givenContent + ">>>>>>>\n";
                        File toUpdate = join(CWD, file);
                        writeContents(toUpdate, toReplace);
                        add(file);
                    }
                }
            } else if (currentHead.getBlobMap().containsKey(file)) {
                File currBlob = join(BLOB_FOLDER, currentHead.getBlobMap().get(file));
                byte[] currContents = readContents(currBlob);
                if ((Arrays.equals(currContents, splitContents))
                        && (!givenHead.getBlobMap().containsKey(file))) {
                    rm(file);
                }
                if (!Arrays.equals(currContents, splitContents)) {
                    String currentID2 = currentHead.getBlobMap().get(file);
                    String currentContent2 = readContentsAsString(join(BLOB_FOLDER, currentID2));
                    mergeConflict = true;
                    String toReplace2 = "<<<<<<< HEAD\n" + currentContent2
                            + "=======\n" + "" + ">>>>>>>\n";
                    File toUpdate2 = join(CWD, file);
                    writeContents(toUpdate2, toReplace2);
                    add(file);
                }
            } else if (givenHead.getBlobMap().containsKey(file)) {
                File givenBlob = join(BLOB_FOLDER, givenHead.getBlobMap().get(file));
                byte[] givenContents = readContents(givenBlob);
                if (Arrays.equals(givenContents, splitContents)
                        && (!currentHead.getBlobMap().containsKey(file))) {
                    continue;
                }
                if (!Arrays.equals(givenContents, splitContents)) {
                    String givenID2 = givenHead.getBlobMap().get(file);
                    String givenContent2 = readContentsAsString(join(BLOB_FOLDER, givenID2));
                    mergeConflict = true;
                    String toReplace2 = "<<<<<<< HEAD\n" + "" + "=======\n"
                            + givenContent2 + ">>>>>>>\n";
                    File toUpdate2 = join(CWD, file);
                    writeContents(toUpdate2, toReplace2);
                    add(file);
                }
            }
        }
        return mergeConflict;
    }


    /**
     * helps with non split file checks
     * @param givenHead commit
     * @param splitPoint commit
     * @param currentHead commit
     * @param givenid shah1
     * @return boolean merge conflict result
     */
    private Boolean nonsplitptFiles(Commit givenHead, Commit splitPoint, Commit currentHead,
                                 String givenid) {
        boolean mergeConflict = false;
        for (String file: givenHead.getBlobMap().keySet()) {
            if ((!splitPoint.getBlobMap().containsKey(file))
                    && (!currentHead.getBlobMap().containsKey(file))) {
                checkout(givenid, file);
                add(file);
            }
            if (!splitPoint.getBlobMap().containsKey(file)) {
                if (currentHead.getBlobMap().containsKey(file)) {
                    String currCont = readContentsAsString(join(BLOB_FOLDER,
                            currentHead.getBlobMap().get(file)));
                    String givenCont = readContentsAsString(join(BLOB_FOLDER,
                            givenHead.getBlobMap().get(file)));
                    if (!givenCont.equals(currCont)) {
                        String toReplace = "<<<<<<< HEAD\n" + currCont + "=======\n"
                                + givenCont + ">>>>>>>\n";
                        writeContents(join(CWD, file), toReplace);
                        add(file);
                        mergeConflict = true;
                    }
                }
            }
        }
        return mergeConflict;
    }


    /**
     * for non split pt current branch checks
     * @param currentHead
     * @param splitPoint
     * @param givenHead
     * @return
     */
    private Boolean currentnonsplit(Commit currentHead, Commit splitPoint, Commit givenHead) {
        boolean mergeConflict = false;
        for (String file: currentHead.getBlobMap().keySet()) {
            if ((!splitPoint.getBlobMap().containsKey(file))
                    && (!givenHead.getBlobMap().containsKey(file))) {
                continue;
            }
            if (!splitPoint.getBlobMap().containsKey(file)) {
                if (givenHead.getBlobMap().containsKey(file)) {
                    String currCont = readContentsAsString(join(BLOB_FOLDER,
                            currentHead.getBlobMap().get(file)));
                    String givenCont = readContentsAsString(join(BLOB_FOLDER,
                            givenHead.getBlobMap().get(file)));
                    if (!currCont.equals(givenCont)) {
                        String toReplace = "<<<<<<< HEAD\n" + currCont + "=======\n"
                                + givenCont + ">>>>>>>\n";
                        writeContents(join(CWD, file), toReplace);
                        add(file);
                        mergeConflict = true;
                    }
                }
            }
        }
        return mergeConflict;
    }

    /**
     * private helper for merge to traverse given and curr branch and return the split point
     * @param givenBranch commit
     * @param currentBranch commit
     * @return commit split point
     */
    private Commit bfsFindSplitPoint(Commit givenBranch, Commit currentBranch) {
        Collection<String> markedGiven = new HashSet<>();
        Collection<String> markedCurrent = new HashSet<>();
        Queue<String> fringe = new PriorityQueue<>();
        String givenIDd = getCommitID(givenBranch);
        fringe.add(givenIDd);
        markedGiven.add(givenIDd);
        while (!fringe.isEmpty()) {
            String v = fringe.remove();
            for (String w: readObject(join(COMMITS, v), Commit.class).getParent()) {
                if ((!markedGiven.contains(w)) && (w != null)) {
                    fringe.add(w);
                    markedGiven.add(w);
                }
            }
        }
        fringe.clear();
        String currIDd = getCommitID(currentBranch);
        fringe.add(currIDd);
        markedCurrent.add(currIDd);
        if (markedGiven.contains(currIDd)) {
            return currentBranch;
        }
        while (!fringe.isEmpty()) {
            String v = fringe.remove();
            for (String w: readObject(join(COMMITS, v), Commit.class).getParent()) {
                if ((!markedCurrent.contains(w)) && (w != null)) {
                    fringe.add(w);
                    markedCurrent.add(w);
                    if (markedGiven.contains(w)) {
                        return readObject(join(COMMITS, w), Commit.class);
                    }
                }
            }
        }
        return null;
    }

    //Is a remote just another .gitlet directory on the local computer that we would attempt
    // to interact with? Instead of changing just what's in the current .gitlet folder,
    // the remote commands would also change what's in the other .gitlet folder?
    public void addRemote(String remoteName, String remoteDir) {
    }

    public void rmRemote(String remoteName) {
    }

    public void push(String remoteName, String remoteBranch) {
    }

    public void fetch(String remoteName, String remoteBranch) {
    }

    public void pull(String remoteName, String remoteBranch) {
    }

}
