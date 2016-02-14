package bufmgr;

import global.PageId;

/**
 * Created by jianruan on 2/11/16.
 */
public class Descriptor {
    private PageId pageNo;
    private int pin_count = 0;
    private boolean dirtybit = false;

    public Descriptor(PageId pageNo) {this.pageNo = pageNo;}

    public int getPin_count(){return pin_count;}
    public void increaseCount () {pin_count++;}
    public void decreaseCount() {pin_count--;}


    public PageId getPageNo(){return pageNo;}

    public boolean getDirtyBit(){
        return dirtybit;
    }

    public void setDirtybit(){
        dirtybit = true;
    }

    public void setClean(){
        dirtybit = false;
    }
}
