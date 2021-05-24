/*
 * @(#)BTreeIndexImpl.java   1.0   Nov 19, 2013
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2016 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.btree;

import minibase.RecordID;
import minibase.storage.buffer.BufferManager;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;

public final class BTreeIndexImpl extends BTreeIndex {

    BTreeIndexImpl(final BufferManager bufferManager, final String name, final Page<BTreeHeader> header) {
        super(bufferManager, name, header);
    }

    @Override
    protected int findKey(final Page<? extends BTreePage> page, final int key, final boolean leaf) {
        // TODO implement this
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected Page<BTreeLeaf> search(final PageID rootID, final int key) {
        // TODO implement this
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected Entry insert(final PageID rootID, final int key, final RecordID value) {
        // TODO implement this
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected RemovalResult remove(final int key, final PageID pageID) {
        // TODO implement this
        throw new UnsupportedOperationException("not yet implemented");
    }
}
