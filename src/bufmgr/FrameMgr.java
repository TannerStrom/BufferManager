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

    public int generateFrame(BufHashTbl hashTbl){
        int fr;
        PageId no;
//        System.out.println("numAvailableFr = "+numAvailableFr);
        if (numAvailableFr > 0) {
            //unused frame in buffer pool
            fr = traverseIndicator();
            numAvailableFr--;
            return fr;
        }
        else  if (! replaceCand.isEmpty()){
            //replacement candidates
            //no change in indicator and numAvailableFr
            int min = Integer.MAX_VALUE;
            fr = -1;
            for(int i = 0; i< numbufs; i++){
                if (indicator[i] == true){
                    if (lfu[i] < min) {
                        fr = i;
                    }
                }
            }
            return fr;
        }
        else{
            return -1;
        }
    }

    public void removeFrame(PageId pageNo, BufHashTbl hashTbl){
        removeCand(pageNo);
        int fr = hashTbl.isExist(pageNo);
        indicator[fr] = false;
        numAvailableFr++;
        lfu[fr] = 0;

    }

    public int getNumAvailableFr(){
        return numAvailableFr;
    }

    public void removeCand(PageId pageNo){
        replaceCand.remove(pageNo);
    }

    public  void addCand(PageId pageNo){
        replaceCand.add(pageNo);
    }

    public int traverseIndicator(){
        for(int i = 0; i< numbufs; i++){
            if (indicator[i] == false){
                indicator[i] = true;
                return i;
            }
        }
        return -1;
    }

}
