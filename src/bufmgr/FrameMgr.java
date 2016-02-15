package bufmgr;

import global.PageId;

import java.util.ArrayList;

/**
 * Created by Yao on 2/13/16.
 */
public class FrameMgr {

    boolean[] indicator;
    int numbufs;
    int numAvailableFr;
    private ArrayList<PageId> replaceCand;
    int[] lfu;

    public FrameMgr(int num){
        numbufs = num;
        indicator = new boolean[num];
        replaceCand = new ArrayList<>();
        numAvailableFr = num;
        lfu = new int[num];
    }



}
