package bufmgr;

import global.PageId;

import java.util.ArrayList;
import java.util.Iterator;

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
        else  if (!replaceCand.isEmpty()){
            //replacement candidates
            //no change in indicator and numAvailableFr
            int min = Integer.MAX_VALUE;
            fr = -1;
            int i = 0, deli = 0;
            int size = replaceCand.size();
            System.out.println("replaceCand.size = "+size);
            while(i < size) {
                System.out.println("i = "+i);
                System.out.println("get{i} = "+replaceCand.get(i).pid);
                int frameNo = hashTbl.isExist(replaceCand.get(i));
                System.out.println("frno = "+frameNo);

                if (lfu[frameNo]<min){
                    fr = frameNo;
                    min = lfu[frameNo];
                    deli = i;
                }
                i++;
            }
            replaceCand.remove(deli);
            System.out.println("replaceCand.size = "+replaceCand.size());

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
