package gitlet;
import java.io.Serializable;
import java.util.TreeMap;

/** Represents a gitlet stage object.
 *  made to persist hashmaps associated with staging area for add and remove
 *  @author Ishika Prashar
 */
public class Stage implements Serializable {
    /**map file name to its blob shah1 for add*/
    private TreeMap<String, String> addBlob;
    /**map file name to its blob shah1 for remove*/
    private TreeMap<String, String> removeBlob;


    /**
     * stage constructor to initialize maps
     */
    public Stage() {
        addBlob = new TreeMap<>();
        removeBlob = new TreeMap<>();
    }

    /**
     * acess add stage
     * @return map of staging area
     */
    public TreeMap<String, String> getAddBlob() {
        return addBlob;
    }

    /**
     * access remove stage
     * @return map of staging area for removal
     */
    public TreeMap<String, String> getRemoveBlob() {
        return removeBlob;
    }

}
