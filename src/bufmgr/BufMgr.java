package bufmgr;

import chainexception.ChainException;
import diskmgr.DiskMgr;
import diskmgr.FileIOException;
import diskmgr.InvalidPageNumberException;
import global.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by jianruan on 2/4/16.
 */
public class BufMgr implements GlobalConst {

    private BufHashTbl hashTable;
    /** Total number of buffer frames in the buffer pool. */
    private int  numBuffers;
    /** physical buffer pool. */
    private Page[] bufPool;

    private Descriptor[] bufDescriptor;
    int bufAt = 0;

    private FrameMgr fm;


    /**
     * Create the BufMgr object.
     * Allocate pages (frames) for the buffer pool in main memory and
     * make the buffer manage aware that the replacement policy is
     * specified by replacerArg (e.g., LH, Clock, LRU, MRU, LFU, etc.). *
     * @param numbufs number of buffers in the buffer pool
     * @param lookAheadSize: Please ignore this parameter
     * @param replacementPolicy Name of the replacement policy, that parameter will be set to "LFU" (you can safely ignore this parameter as you will implement only one policy)
     */
    public BufMgr(int numbufs, int lookAheadSize, String replacementPolicy) {
        numBuffers = numbufs;
        bufPool = new Page[numbufs];
        hashTable = new BufHashTbl();
        bufDescriptor = new Descriptor[numbufs];
        fm = new FrameMgr(numbufs);
        bufAt = 0;
    }
    /**
     * Pin a page.
     * First check if this page is already in the buffer pool.
     * If it is, increment the pin_count and return a pointer to this
     * page.
     * If the pin_count was 0 before the call, the page was a
     * replacement candidate, but is no longer a candidate.
     * If the page is not in the pool, choose a frame (from the
     * set of replacement candidates) to hold this page, read the
     * page (using the appropriate method from {\em diskmgr} package) and pin it.
     * Also, must write out the old page in chosen frame if it is dirty
     * before reading new page.__ (You can assume that emptyPage==false for
     * this assignment.) *
     * @param pageno page number in the Minibase.
     * @param page the pointer point to the page.
     * @param emptyPage true (empty page); false (non-empty page)
     */
    public Page pinPage(PageId pageno, Page page, boolean emptyPage) throws ChainException, HashEntryNotFoundException, BufferPoolExceededException {
//        System.out.println("\n===========PinPage===========");
//        try {
//            System.out.println(pageno.pid);
//            System.out.println(Convert.getIntValue (0, page.getpage()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        int fr = hashTable.isExist(pageno);
        //System.out.println("fr = "+fr);

        if(fr >= 0) {
//            System.out.println("-------in pool----------");

            bufDescriptor[fr].increaseCount();

            if (bufDescriptor[fr].getPin_count() == 1){
                fm.removeCand(pageno);
            }
            page.setpage( bufPool[fr].getpage());
        }
        else {
            fr = fm.generateFrame(hashTable);
//            System.out.println("generateFrame # fr = "+fr);

            if (fr < 0){
                throw new BufferPoolExceededException();
            }

            if(bufDescriptor[fr] != null && bufDescriptor[fr].getDirtyBit()) {
                try {
                    flushPage(bufDescriptor[fr].getPageNo());
                } catch (HashEntryNotFoundException e){System.out.println("HashEntryNotFoundException");}
                hashTable.remove(bufDescriptor[fr].getPageNo());
            }

            Descriptor d= new Descriptor(pageno);
            bufDescriptor[fr] = d;

            Page temp = new Page();

            try {
                Minibase.DiskManager.read_page(pageno, temp);
//            } catch (Exception e) {e.printStackTrace();}
            } catch (Exception e) {System.out.println("ChainException");}

            page.setpage(temp.getpage());

            bufPool[fr] = temp;

            hashTable.insert(pageno, fr);

            bufDescriptor[fr].increaseCount();
//            System.out.println("Count = "+bufDescriptor[fr].getPin_count());

        }

        fm.lfu[fr]++;
        return bufPool[fr];

    }


    /**
     * Unpin a page specified by a pageId.
     * This method should be called with dirty==true if the client has * modified the page.
     * If so, this call should set the dirty bit
     * for this frame. #
     * Further, if pin_count>0, this method should
     * decrement it. #
     *If pin_count=0 before this call, throw an exception
     * to report error. #
     *(For testing purposes, we ask you to throw
     * an exception named PageUnpinnedException in case of error.) *
     * @param pageno page number in the Minibase.
     * @param dirty the dirty bit of the frame
     */
    public void unpinPage(PageId pageno, boolean dirty) throws PageUnpinnedException, HashEntryNotFoundException, ChainException {
        //System.out.println("\n===========UnPinPage===========");

        int fr = hashTable.isExist(pageno);
        if (fr < 0){
            //Page not found in buffer pool
            System.out.println("fr < 0 HashEntryNotFoundException");
            throw new HashEntryNotFoundException();
        }
        if (bufDescriptor[fr].getPin_count() == 0){
//            System.out.println("bufDescriptor[fr].getPin_count() = "+bufDescriptor[fr].getPin_count());
            System.out.println("PageUnpinnedException");
            throw new PageUnpinnedException();
        }
        if (bufDescriptor[fr].getPin_count() > 0) {
            bufDescriptor[fr].decreaseCount();
            if (bufDescriptor[fr].getPin_count() == 0){
                fm.addCand(pageno);
            }
        }

        if (dirty == true) {
            bufDescriptor[fr].setDirtybit();
        }


    }

    /**
     * Allocate new pages.
     * Call DB object to allocate a run of new pages and
     * find a frame in the buffer pool for the first page
     * and pin it. (This call allows a client of the Buffer Manager
     * to allocate pages on disk.) If buffer is full, i.e., you
     * can't find a frame for the first page, ask DB to deallocate
     * all these pages, and return null. *
     * @param firstpage the address of the first page.
     * @param howmany total number of allocated new pages. *
     * @return the first page id of the new pages.__ null, if error. */
    public PageId newPage(Page firstpage, int howmany) {
//        System.out.println("\n===========NewPage===========");
//
//        System.out.println("getNumUnpinned() = "+getNumUnpinned());
//        System.out.println("howmany = "+howmany);


        if (getNumUnpinned() == 0){
            return null;
        }

        PageId pid = new PageId();
        try {
            Minibase.DiskManager.allocate_page(pid, howmany);
            pinPage(pid, firstpage, false);
       // } catch (Exception e) {e.printStackTrace();}
        } catch (Exception e) {System.out.println("Exception in newPage");}

        return pid;

    }

    /**
     * This method should be called to delete a page that is on disk.
     * This routine must call the method in diskmgr package to
     * deallocate the page. *
     * @param globalPageId the page number in the data base. */

    public void freePage(PageId globalPageId) throws HashEntryNotFoundException, PagePinnedException, ChainException{
        //System.out.println("\n===========FreePage===========");
        int fr = hashTable.isExist(globalPageId);
        if (fr < 0){
            //Page not found in buffer pool
            System.out.println("HashEntryNotFoundException");

            throw new HashEntryNotFoundException();
        }
        else if (bufDescriptor[fr].getPin_count() != 0){
            System.out.println("PagePinnedException");

            throw new PagePinnedException();
        }
        else {
            hashTable.remove(globalPageId);
            bufDescriptor[fr] = null;
            bufPool[fr] = null;
            fm.removeFrame(globalPageId, hashTable);
        }

        try {
            Minibase.DiskManager.deallocate_page(globalPageId, 1);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * Used to flush a particular page of the buffer pool to disk.
     * This method calls the write_page method of the diskmgr package. *
     * @param pageid the page number in the database. */
    public void flushPage(PageId pageid) throws HashEntryNotFoundException{
//        System.out.println("\n===========flushPage===========");

        int fr = hashTable.isExist(pageid);
        if (fr < 0){
            //Page not found in buffer pool
            System.out.println("HashEntryNotFoundException");
            throw new HashEntryNotFoundException();
        }
        try {
            Minibase.DiskManager.write_page(pageid, bufPool[fr]);
        } catch (Exception e) {e.printStackTrace();}

        bufDescriptor[fr].setClean();
    }

    /**
     * Used to flush all dirty pages in the buffer pool to disk *
     */
    public void flushAllPages() {
        System.out.println("\n===========flushAllPage===========");

        for (int i = 0; i < numBuffers; i++){
            if (fm.indicator[i] == true)
                try {
                    flushPage(bufDescriptor[i].getPageNo());
                } catch (HashEntryNotFoundException e) {}
        }

    }

    /**
     * Returns the total number of buffer frames. */
    public int getNumBuffers() {
        return fm.numbufs;
    }

    /**
     * Returns the total number of unpinned buffer frames. */
    public int getNumUnpinned() {
//        System.out.println("numbuf = "+numBuffers);
//        System.out.println(fm.numAvailableFr+ numBuffers);
        return fm.getNumAvailableFr();
    }

}