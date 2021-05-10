/*
 * @(#)BufferManagerImpl.java   1.0   Nov 06, 2016
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.storage.buffer;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import minibase.storage.buffer.policy.ReplacementPolicy;
import minibase.storage.buffer.policy.ReplacementPolicy.PageState;
import minibase.storage.file.DiskManager;

/**
 * Sample solution for the buffer manager.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni.kn&gt;
 */
public final class BufferManagerImpl implements BufferManager {

    /**
     * Underlying disk manager.
     */
    private final DiskManager diskManager;

    /**
     * Replacement policy for selecting the next page to evict.
     */
    private final ReplacementPolicy replacementPolicy;

    /**
     * Static set of frames the buffer manager maintains.
     */
    private final Page<?>[] pages;

    /**
     * Map from page ID to frame.
     */
    private final Map<PageID, Page<?>> pageMap = new HashMap<>();

    /**
     * List of free pages for fast access.
     */
    private final Deque<Page<?>> freePages = new ArrayDeque<>();

    /**
     * Constructs a buffer manager with the given configuration.
     *
     * @param diskManager         underlying disk manager, used for disk I/O
     * @param bufferPoolSize      number of buffer frames to use
     * @param replacementStrategy strategy for choosing the next page to evict
     */
    public BufferManagerImpl(final DiskManager diskManager, final int bufferPoolSize,
                             final ReplacementStrategy replacementStrategy) {
        this.diskManager = diskManager;
        this.replacementPolicy = replacementStrategy.newInstance(bufferPoolSize);
        this.pages = new Page<?>[bufferPoolSize];
        for (int i = 0; i < bufferPoolSize; i++) {
            final Page<?> page = new Page<>(i);
            this.pages[i] = page;
            this.freePages.add(page);
        }
    }

    @Override
    public DiskManager getDiskManager() {
        return this.diskManager;
    }

    /**
     * Tries to find a buffer page to load a disk block into. If another page must be evicted first,
     * it is written back to disk.
     *
     * @return free buffer page
     * @throws IllegalStateException if all pages in the buffer pool are pinned
     */
    private Page<?> getBufferPage() {
        final Page<?> page;
        if (!this.freePages.isEmpty()) {
            page = this.freePages.poll();
        } else {
            final int evict = this.replacementPolicy.pickVictim();
            if (evict < 0) {
                throw new IllegalStateException("Buffer pool is full.");
            }
            page = this.pages[evict];
            this.pageMap.remove(page.getPageID());
            this.flushPage(page);
        }
        return page;
    }

    @Override
    public Page<?> newPage() {
        final Page<?> page = this.getBufferPage();
        page.reset(this.diskManager.allocatePage());
        Arrays.fill(page.getData(), (byte) 0);
        page.setDirty(true);
        page.incrementPinCount();
        this.pageMap.put(page.getPageID(), page);
        this.replacementPolicy.stateChanged(page.getIndex(), PageState.PINNED);
        return page;
    }

    @Override
    public void freePage(final Page<?> page) {
        final PageID pageID = page.getPageID();
        if (page.getPinCount() != 1) {
            throw new IllegalArgumentException("Wrong pin count for Page#" + pageID + ": " + page.getPinCount());
        }
        this.pageMap.remove(pageID);
        page.reset(PageID.INVALID);
        this.diskManager.deallocatePage(pageID);
        this.replacementPolicy.stateChanged(page.getIndex(), PageState.FREE);
        this.freePages.add(page);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PageType> Page<T> pinPage(final PageID pageID) {
        if (!pageID.isValid()) {
            throw new IllegalArgumentException("Invalid PageID.");
        }

        final Page<?> page;
        if (this.pageMap.containsKey(pageID)) {
            page = this.pageMap.get(pageID);
        } else {
            page = this.getBufferPage();
            page.reset(pageID);
            this.diskManager.readPage(pageID, page.getData());
            this.pageMap.put(pageID, page);
            this.replacementPolicy.stateChanged(page.getIndex(), PageState.PINNED);
        }
        page.incrementPinCount();
        return (Page<T>) page;
    }

    @Override
    public void unpinPage(final Page<?> page, final UnpinMode mode) {
        final int pinCount = page.getPinCount();
        if (pinCount < 1) {
            throw new IllegalArgumentException("Page #" + page.getPageID() + " is not pinned.");
        }

        page.decrementPinCount();
        if (mode == UnpinMode.DIRTY) {
            page.setDirty(true);
        }

        if (pinCount == 1) {
            this.replacementPolicy.stateChanged(page.getIndex(), PageState.UNPINNED);
        }
    }

    @Override
    public void flushPage(final Page<?> page) {
        if (page.isDirty()) {
            // write the page to disk
            this.getDiskManager().writePage(page.getPageID(), page.getData());
            // the buffer page is now clean
            page.setDirty(false);
        }
    }

    @Override
    public void flushAllPages() {
        for (final Page<?> buffered : this.pageMap.values()) {
            this.flushPage(buffered);
        }
    }

    @Override
    public int getNumBuffers() {
        return this.pages.length;
    }

    @Override
    public int getNumPinned() {
        int pinned = 0;
        for (final Page<?> buffered : this.pageMap.values()) {
            if (buffered.getPinCount() > 0) {
                pinned++;
            }
        }
        return pinned;
    }

    @Override
    public int getNumUnpinned() {
        return this.getNumBuffers() - this.getNumPinned();
    }
}
