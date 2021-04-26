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

import minibase.storage.file.DiskManager;

public final class BufferManagerImpl implements BufferManager {

    public BufferManagerImpl(final DiskManager diskManager, final int bufferPoolSize,
                             final ReplacementStrategy replacementStrategy) {
        // TODO implement constructor
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public DiskManager getDiskManager() {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Page<?> newPage() {
        // TODO implement method
        // Tip: delegate page allocation to a DiskManager instance
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void freePage(final Page<?> page) {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public <T extends PageType> Page<T> pinPage(final PageID pageID) {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void unpinPage(final Page<?> page, final UnpinMode mode) {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
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
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int getNumBuffers() {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int getNumPinned() {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int getNumUnpinned() {
        return this.getNumBuffers() - this.getNumPinned();
    }
}
