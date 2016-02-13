package bufmgr;

import global.PageId;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jianruan on 2/11/16.
 */
public class BufHashTbl {
    //total number of buckets, each is a list of <pageID, frameNo> pairs
    private static final int HTSIZE = 41;
    private ArrayList<Bucket> directory;

    public BufHashTbl () {
        directory =  new ArrayList<>();
    }

    private int HashFunc(int pageNo) {
        return pageNo % HTSIZE;
    }

    public void insert (PageId pageNo, int fr) {
        int index = HashFunc(pageNo.pid);
        int[] pair = {pageNo.pid, fr};
        directory.get(index).pairs.add(pair);
    }



    public int isExist(PageId pageNo) {
        int index = HashFunc(pageNo.pid);
        Bucket bucket = directory.get(index);
        Iterator iter = bucket.pairs.iterator();
        while(iter.hasNext()) {
            int[] pair = (int[]) iter.next();
            if(pair[0] == pageNo.pid)
                return pair[1];
        }
        return -1;
    }
}
