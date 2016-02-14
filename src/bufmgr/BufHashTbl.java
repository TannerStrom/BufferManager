package bufmgr;

import global.PageId;
import sun.jvm.hotspot.tools.SysPropsDumper;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jianruan on 2/11/16.
 */
public class BufHashTbl {
    //total number of buckets, each is a list of <pageID, frameNo> pairs
    private static final int HTSIZE = 41;
    private Bucket[] directory;

    public BufHashTbl () {
        directory =  new Bucket[HTSIZE];
    }

    private int HashFunc(int pageNo) {
        return pageNo % HTSIZE;
    }

    public void insert (PageId pageNo, int fr) {
        int index = HashFunc(pageNo.pid);
        int[] pair = {pageNo.pid, fr};
        if (directory[index] == null){
            directory[index] = new Bucket();
        }
        directory[index].pairs.add(pair);
    }

    public void remove (PageId pageNo){
        int index = HashFunc(pageNo.pid);
        if (directory[index] == null){
            directory[index] = new Bucket();
        }
        else{
            Iterator iter = directory[index].pairs.iterator();
            while(iter.hasNext()) {
                int[] pair = (int[]) iter.next();
                if(pair[0] == pageNo.pid)
                    directory[index].pairs.remove(pair);
            }
        }
    }



    public int isExist(PageId pageNo) {
        int index = HashFunc(pageNo.pid);
        Bucket bucket = directory[index];

        if (bucket == null){
            bucket = new Bucket();
            return -1;
        }

        Iterator iter = bucket.pairs.iterator();
        while(iter.hasNext()) {
            int[] pair = (int[]) iter.next();
            if(pair[0] == pageNo.pid)
                return pair[1];
        }
        return -1;
    }
}
