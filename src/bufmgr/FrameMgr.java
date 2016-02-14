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
        numAvailableFr = num;
    }

    public int generateFrame(PageId pageNo, BufHashTbl hashTbl){
        int fr;
        PageId no;
        System.out.println("numAvailableFr = "+numAvailableFr);
        if (numAvailableFr > 0) {
            //unused frame in buffer pool
            fr = traverseIndicator();
            numAvailableFr--;
            lru.add(pageNo);
            return fr;
        }
        else  if (! replaceCand.isEmpty()){
            //replacement candidates
            //no change in indicator and numAvailableFr
            no = replaceCand.remove(0);
            lru.remove(no);
            lru.add(pageNo);
        }
        else{
            //lru
            //no change in candidates, indicator and numAvailableFr
            System.out.print(numAvailableFr);
            no= lru.remove(0);
            lru.add(pageNo);

        }
        return hashTbl.isExist(no);
    }

    public void removeFrame(PageId pageNo, BufHashTbl hashTbl){
        removeCand(pageNo);
        lru.remove(pageNo);
        int fr = hashTbl.isExist(pageNo);
        indicator[fr] = false;
        numAvailableFr--;

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
