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
        hashTable = new BufHashTbl();
        numBuffers = numbufs;
        bufPool = new Page[numbufs];
        bufDescriptor = new Descriptor[numbufs];
//        for(int i = 0; i< numbufs; i++){
//            bufPool[i] = new Page();
//        }

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
    public Page pinPage(PageId pageno, Page page, boolean emptyPage) throws ChainException {
//        removeDup();

        PageId pageNo = new PageId(pageno.pid);

        int fr = hashTable.isExist(pageNo);

        if (fr >= 0){
            //in the pool
            //candidate handle
            bufDescriptor[fr].increaseCount();
            page.setpage(bufPool[fr].getpage());
            bufDescriptor[fr].increaseLru();
            return bufPool[fr];
        }
        else{
            //not in the pool: free frame, exceeded or a candidate

            //free frame
            for(int i = 0; i < numBuffers; i++){
                if (bufDescriptor[i] == null){

                    Page temp = new Page();

                    try {
                        Minibase.DiskManager.read_page(pageNo, temp);
                    } catch (Exception e) {}

                    page.setpage(temp.getpage());

                    bufPool[i] = temp;

                    hashTable.insert(pageNo, i);

                    bufDescriptor[i] = new Descriptor(pageNo);

                    bufDescriptor[i].increaseCount();

                    bufDescriptor[i].increaseLru();
                    return bufPool[i];
                }
            }

            //cand
            int index = -1;
            for (int i = 0; i < numBuffers; i++){
                if (bufDescriptor[i].getPin_count()==0){
                    index = i;
                    break;
                }
            }

            if (index > -1) {

                for (int i = index+1; i < numBuffers; i++){
                    if (bufDescriptor[i].getPin_count()==0){
                        if (bufDescriptor[index].getLru() > bufDescriptor[i].getLru()){
                            index = i;
                        }
                    }
                }

//                long timeNow = System.currentTimeMillis();
//
//                int freq = bufDescriptor[index].getLru();
//                for (int i = 0; i< numBuffers; i++){
//                    if (bufDescriptor[i].getLru() == freq && bufDescriptor[i].getTimeNow()<timeNow){
//                        index = i;
//                    }
//                }
//                for (int i = index+1; i < numBuffers; i++){
//                    if (bufDescriptor[i].getPin_count()==0){
//                        if (bufDescriptor[i].getLru() == freq && bufDescriptor[index].getPageNo().pid > bufDescriptor[i].getPageNo().pid){
//                            index = i;
//                        }
//                    }
//                }


                fr = index;

                if (bufDescriptor[fr].getDirtyBit()) {
                    flushPage(bufDescriptor[fr].getPageNo());
                }

                hashTable.remove(bufDescriptor[fr].getPageNo(), bufDescriptor);

                Page temp = new Page();

                try {
                    Minibase.DiskManager.read_page(pageNo, temp);
                } catch (Exception e) {}

                page.setpage(temp.getpage());

                bufPool[fr] = temp;

                hashTable.insert(pageNo, fr);

                bufDescriptor[fr] = new Descriptor(pageNo);
                bufDescriptor[fr].increaseCount();
                bufDescriptor[fr].increaseLru();
                return bufPool[fr];
            }

            //exceed
            throw new BufferPoolExceededException(null, "bufmgr.BufferPoolExceededException");



        }

    }

    private void removeDup() {
        for(int i = 0; i< numBuffers; i++){
            for(int j = i; j < numBuffers; j++){
                if (bufDescriptor[i] != null && bufDescriptor[j] != null) {
                    if (bufDescriptor[i].getPageNo().pid == bufDescriptor[i].getPageNo().pid) {
                        int temp = hashTable.isExist(bufDescriptor[i].getPageNo());
                        if (i != temp)
                            bufDescriptor[i] = null;
                        if (j != temp)
                            bufDescriptor[j] = null;
                    }
                }
            }
        }
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
    public void unpinPage(PageId pageno, boolean dirty) throws ChainException {
        PageId pageNo = new PageId(pageno.pid);

        int fr = hashTable.isExist(pageNo);
        if (fr < 0){
            //Page not found in buffer pool
//            System.out.println("fr < 0 HashEntryNotFoundException");
            throw new HashEntryNotFoundException(null, "bufmgr.HashEntryNotFoundException");
        }
        if (bufDescriptor[fr].getPin_count() == 0){
//            System.out.println("bufDescriptor[fr].getPin_count() = "+bufDescriptor[fr].getPin_count());
            throw new PageUnpinnedException(null, "bufmgr.PageUnpinnedException");
        }
        if (bufDescriptor[fr].getPin_count() > 0) {
            bufDescriptor[fr].decreaseCount();
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

        if (getNumUnpinned() == 0){
            return null;
        }

        PageId pid = new PageId();
        try {
            Minibase.DiskManager.allocate_page(pid, howmany);
            pinPage(pid, firstpage, false);
            // } catch (Exception e) {e.printStackTrace();}
        } catch (Exception e) {}

        return pid;

    }

    /**
     * This method should be called to delete a page that is on disk.
     * This routine must call the method in diskmgr package to
     * deallocate the page. *
     * @param globalPageId the page number in the data base. */

    public void freePage(PageId globalPageId) throws ChainException{

        PageId pid = new PageId(globalPageId.pid);

        int fr = hashTable.isExist(pid);
        if (fr < 0){
//            Page not found in buffer pool, return
            try {
                Minibase.DiskManager.deallocate_page(pid, 1);
            } catch (Exception e) {}
            return;
        }
        if (bufDescriptor[fr].getPin_count() != 0){
//            System.out.println("PagePinnedException");

            throw new PagePinnedException(null, "bufmgr.PagePinnedException");
        }
        else {
            hashTable.remove(pid, bufDescriptor);
            bufDescriptor[fr] = null;
            bufPool[fr] = null;
        }

        try {
            Minibase.DiskManager.deallocate_page(pid, 1);
        } catch (Exception e) {}
    }

    /**
     * Used to flush a particular page of the buffer pool to disk.
     * This method calls the write_page method of the diskmgr package. *
     * @param pageid the page number in the database. */
    public void flushPage(PageId pageid) throws ChainException{

        PageId pageId = new PageId(pageid.pid);
        int fr = hashTable.isExist(pageId);
        if (fr < 0){
            //Page not found in buffer pool
//            System.out.println("HashEntryNotFoundException");
            throw new HashEntryNotFoundException(null, "bufmgr.HashEntryNotFoundException");
        }
        Page p = new Page();
        p.setpage(bufPool[fr].getpage());
        try {
            Minibase.DiskManager.write_page(pageId, p);
        } catch (Exception e) {}

        bufDescriptor[fr].setClean();
    }

    /**
     * Used to flush all dirty pages in the buffer pool to disk *
     */
    public void flushAllPages() {
        for (int i = 0; i < numBuffers; i++){
            if (bufDescriptor[i] != null)
                try {
                    flushPage(bufDescriptor[i].getPageNo());
                } catch (ChainException e) {}
        }

    }

    /**
     * Returns the total number of buffer frames. */
    public int getNumBuffers() {
        return numBuffers;
    }

    /**
     * Returns the total number of unpinned buffer frames. */
    public int getNumUnpinned() {
        int ret = 0;
        for(int i = 0; i< numBuffers; i++){
            if(bufDescriptor[i] == null){
                ret++;
            }
            else if (bufDescriptor[i].getPin_count()==0){
                ret++;
            }
        }
        return ret;



    }

}