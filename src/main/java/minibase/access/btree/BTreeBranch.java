/*
 * @(#)BTreeBranch.java   1.0   Oct 29, 2013
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2016 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.btree;

import minibase.storage.buffer.BufferManager;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;
import minibase.storage.file.DiskManager;

/**
 * Utility class for accessing fields in a {@link BTreeIndex} branch page.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
final class BTreeBranch extends BTreePage {

    /**
     * Minimum number of keys in an inner node.
     */
    public static final int MIN_KEYS = (DiskManager.PAGE_SIZE - Integer.BYTES - PageID.BYTES)
            / (2 * (Integer.BYTES + PageID.BYTES));

    /**
     * Maximum number of keys in an inner node.
     */
    public static final int MAX_KEYS = 2 * MIN_KEYS;

    /**
     * Hidden default constructor.
     */
    private BTreeBranch() {
        throw new AssertionError();
    }

    /**
     * Initializes a newly allocated page as a B+-Tree branch page.
     *
     * @param bufferManager the buffer manager for getting the new page
     * @return the initialized page
     */
    static Page<BTreeBranch> newPage(final BufferManager bufferManager) {
        @SuppressWarnings("unchecked") final Page<BTreeBranch> page = (Page<BTreeBranch>) bufferManager.newPage();
        BTreePage.setMeta(page, false, 0);
        return page;
    }

    /**
     * Checks if the given page is a branch page.
     *
     * @param page page to check
     * @return the page if it is a branch page, {@code null} otherwise
     */
    static Page<BTreeBranch> check(final Page<BTreePage> page) {
        return cast(BTreePage.isLeafPage(page) ? null : page);
    }

    /**
     * Casts the given page to a branch page.
     *
     * @param page the page to cast
     * @return the page
     */
    @SuppressWarnings("unchecked")
    static Page<BTreeBranch> cast(final Page<? extends BTreePage> page) {
        return (Page<BTreeBranch>) page;
    }

    /**
     * Gets the key stored at the given position.
     *
     * @param page the page
     * @param pos  position of the key
     * @return the key's value
     */
    static int getKey(final Page<BTreeBranch> page, final int pos) {
        return page.readInt(pos * Integer.BYTES);
    }

    /**
     * Sets the key at the given position.
     *
     * @param page the page
     * @param pos  position of the key
     * @param key  the key to set
     */
    static void setKey(final Page<BTreeBranch> page, final int pos, final int key) {
        page.writeInt(pos * Integer.BYTES, key);
    }

    /**
     * Gets the child page ID at the given position.
     *
     * @param page the page
     * @param pos  position of the child page ID
     * @return the child page ID
     */
    static PageID getChildID(final Page<BTreeBranch> page, final int pos) {
        return page.readPageID(childIDOffset(pos));
    }

    /**
     * Sets the child page ID at the given position.
     *
     * @param page    the page
     * @param pos     position of the child page ID
     * @param childID the new ID
     */
    static void setChildID(final Page<BTreeBranch> page, final int pos, final PageID childID) {
        page.writePageID(childIDOffset(pos), childID);
    }

    /**
     * Inserts an entry (i.e. a pair of a child page ID and the search key coming before it)
     * into this branch page.
     *
     * @param page    the page
     * @param pos     position to insert at, {@code 0} inserts after the first child page ID
     * @param key     key to insert
     * @param childID child page ID to insert
     */
    static void insertEntry(final Page<BTreeBranch> page, final int pos, final int key, final PageID childID) {
        final int pageIDOffset = childIDOffset(pos);
        final int numKeys = BTreePage.getNumKeys(page);
        final byte[] data = page.getData();
        System.arraycopy(data, pos * Integer.BYTES, data, (pos + 1) * Integer.BYTES,
                (numKeys - pos) * Integer.BYTES);
        final int length = (numKeys + 1 - pos) * PageID.BYTES;
        System.arraycopy(data, pageIDOffset - length + PageID.BYTES, data, pageIDOffset - length, length);
        setKey(page, pos, key);
        setChildID(page, pos + 1, childID);
        BTreePage.setNumKeys(page, numKeys + 1);
    }

    /**
     * Deletes an entry (i.e. a pair of a child page ID and the search key coming before it)
     * from this branch page.
     *
     * @param page the page
     * @param pos  position of the entry to delete, {@code 0} deletes the key and ID after the
     *             first child page ID
     */
    static void deleteEntry(final Page<BTreeBranch> page, final int pos) {
        final int keyOffset = pos * Integer.BYTES;
        // We want the childID right of the deleted key
        final int pageIDOffset = childIDOffset(pos + 1);
        final int numKeys = BTreePage.getNumKeys(page);
        final byte[] data = page.getData();
        System.arraycopy(data, keyOffset + Integer.BYTES, data, keyOffset, (numKeys - pos - 1) * Integer.BYTES);
        final int length = (numKeys - pos) * PageID.BYTES;
        System.arraycopy(data, pageIDOffset - length, data, pageIDOffset - length + PageID.BYTES, length);
        BTreePage.setNumKeys(page, numKeys - 1);
    }

    /**
     * Shifts {@code n} entries from position {@code from} to position {@code to} in the given page.
     *
     * @param page the page
     * @param from original start of the entries
     * @param to   new start of the entries
     * @param n    number of entries to shift
     */
    static void shiftEntries(final Page<BTreeBranch> page, final int from, final int to, final int n) {
        copyEntries(page, from, page, to, n);
    }

    /**
     * Copies the entries {@code [srcPos .. srcPos+n-1]}  of page {@code src} to positions
     * {@code [destPos .. destPos+n-1]} of page {@code dest}.
     *
     * @param src     source page
     * @param srcPos  position in the source page
     * @param dest    destination page
     * @param destPos position in the destination page
     * @param n       number of entries to copy
     */
    static void copyEntries(final Page<BTreeBranch> src, final int srcPos,
                            final Page<BTreeBranch> dest, final int destPos, final int n) {
        System.arraycopy(src.getData(), srcPos * Integer.BYTES, dest.getData(), destPos * Integer.BYTES,
                n * Integer.BYTES);
        final int length = (n + 1) * PageID.BYTES;
        System.arraycopy(src.getData(), childIDOffset(srcPos) - length + PageID.BYTES, dest.getData(),
                childIDOffset(destPos) - length + PageID.BYTES, n * PageID.BYTES);
    }

    /**
     * Returns the offset of the child page ID at the given position.
     *
     * @param pos child page ID position
     * @return offset in the page
     */
    private static int childIDOffset(final int pos) {
        return BTreePage.META_POS - (pos + 1) * PageID.BYTES;
    }
}
