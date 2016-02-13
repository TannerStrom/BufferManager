package bufmgr;

import java.util.ArrayList;

/**
 * Created by jianruan on 2/11/16.
 */
public class Bucket {
    //int[0] is pid, int[1] is frame
    ArrayList<int[]> pairs;
    public Bucket() {
        pairs = new ArrayList<>();
    }
}
