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
    private ArrayList<PageId> lru;

    public FrameMgr(int num){
        numbufs = num;
        indicator = new boolean[num];
        replaceCand = new ArrayList<>();
        lru = new ArrayList<>();
    }

    public int generateFrame(){
        int fr = -1;
        if (numAvailableFr > 0) {

        }
        else  if (! replaceCand.isEmpty()){
            fr = replaceCand.remove(0).pid;
        }
        else{
            //lru
        }
        return -1;
    }

    public void removeFrame(){

    }

    public int getNumAvailableFr(){
        return numAvailableFr;
    }

    public void removeCand(PageId pageNo){
        replaceCand.remove(pageNo);
    }

}
