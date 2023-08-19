package gitlet;
import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;


/** Represents a gitlet commit object.
 *  to create commit objects that store metadata about given commit
 *  @author Ishika Prashar
 */
public class Commit implements Serializable {

    /** The message of this Commit. */
    private final String message;
    /**time stamp of this commit */
    private final String time;
    /** parent commit pointer */
    private final String parent;
    /** map of blob name to its shah1 id*/
    private final TreeMap<String, String> blobNameID;
    /** second parent pointer for merge commits */
    private final String secondParent;

    /**
     * commit constructor to initialize object and store metadata
     * @param msg associated with commit
     * @param parentID shah1 of parent of new commit (oldhead id)
     * @param secondParentId shah1 of second parent when merging else null
     */
    public Commit(String msg, String parentID, String secondParentId) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        if (parentID == null) {
            message = msg;
            parent = null;
            secondParent = null;
            blobNameID = new TreeMap<>();
            time = formatter.format(new Date(0));

        } else {
            message = msg;
            parent = parentID;
            secondParent = secondParentId;
            blobNameID = new TreeMap<>();
            time = formatter.format(new Date());
        }
    }


    /**
     * @return blobNameId hashmap
     */
    public TreeMap<String, String> getBlobMap() {
        return this.blobNameID;
    }

    /**
     * @return message string
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @return time string
     */
    public String getTimeStamp() {
        return this.time;
    }

    /**
     * @return parent string ID
     */
    public List<String> getParent() {
        List<String> parents = new ArrayList<>();
        parents.add(this.parent);
        parents.add(this.secondParent);
        return parents;
    }
}
