/*
 * @(#)BTreeIndexImpl.java   1.0   Nov 19, 2013
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.btree;

import minibase.RecordID;
import minibase.storage.buffer.BufferManager;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;
import minibase.storage.buffer.UnpinMode;

/**
 * Implementations of the core algorithms of a B+ Tree index.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public final class BTreeIndexImpl extends BTreeIndex {

    /**
     * Constructor for opening an existing index.
     *
     * @param bufferManager the buffer manager
     * @param name          name of the index
     * @param header        the index's header page
     */
    BTreeIndexImpl(final BufferManager bufferManager, final String name, final Page<BTreeHeader> header) {
        super(bufferManager, name, header);
    }

    /**
     * Rotates {@code n} entries from page {@code right} over {@code page} to {@code left},
     * where {@code leftPos} is the position of the reference to {@code left} in the parent
     * page {@code page}. After this operation, {@code left} has {@code n} entries more and
     * {@code right} less than before.
     *
     * @param page    parent page
     * @param leftPos position of the child page ID for {@code left} in {@code page}
     * @param left    left child page
     * @param right   right child page
     * @param n       number of entries to shift over
     */
    private static void rotateLeft(final Page<BTreeBranch> page, final int leftPos,
                                   final Page<? extends BTreePage> left, final Page<? extends BTreePage> right,
                                   final int n) {
        if (n > 0) {
            final int leftSize = BTreePage.getNumKeys(left);
            final int rightSize = BTreePage.getNumKeys(right);
            final int newMidKey;
            if (BTreePage.isLeafPage(left)) {
                final Page<BTreeLeaf> lLeaf = BTreeLeaf.cast(left);
                final Page<BTreeLeaf> rLeaf = BTreeLeaf.cast(right);
                newMidKey = BTreeLeaf.getKey(rLeaf, n);
                BTreeLeaf.copyEntries(rLeaf, 0, lLeaf, leftSize, n);
                BTreeLeaf.shiftEntries(rLeaf, n, 0, rightSize - n);
            } else {
                final Page<BTreeBranch> lBranch = BTreeBranch.cast(left);
                final Page<BTreeBranch> rBranch = BTreeBranch.cast(right);
                newMidKey = BTreeBranch.getKey(rBranch, n - 1);
                BTreeBranch.insertEntry(lBranch, leftSize, BTreeBranch.getKey(page, leftPos),
                        BTreeBranch.getChildID(rBranch, 0));
                BTreeBranch.copyEntries(rBranch, 0, lBranch, leftSize + 1, n - 1);
                BTreeBranch.setChildID(rBranch, 0, BTreeBranch.getChildID(rBranch, n));
                BTreeBranch.shiftEntries(rBranch, n, 0, rightSize - n);
            }
            BTreeBranch.setKey(page, leftPos, newMidKey);
            BTreePage.setNumKeys(left, leftSize + n);
            BTreePage.setNumKeys(right, rightSize - n);
        }
    }

    /**
     * Rotates {@code n} entries from page {@code left} over {@code page} to {@code right},
     * where {@code leftPos} is the position of the reference to {@code left} in the parent
     * page {@code page}. After this operation, {@code right} has {@code n} entries more and
     * {@code left} less than before.
     *
     * @param page    parent page
     * @param leftPos position of the child page ID for {@code left} in {@code page}
     * @param left    left child page
     * @param right   right child page
     * @param n       number of entries to shift over
     */
    private static void rotateRight(final Page<BTreeBranch> page, final int leftPos,
                                    final Page<? extends BTreePage> left, final Page<? extends BTreePage> right,
                                    final int n) {
        if (n > 0) {
            final int leftSize = BTreePage.getNumKeys(left);
            final int rightSize = BTreePage.getNumKeys(right);
            final int midKey = BTreeBranch.getKey(page, leftPos);
            final int newMidKey;
            if (BTreePage.isLeafPage(left)) {
                final Page<BTreeLeaf> lLeaf = BTreeLeaf.cast(left);
                final Page<BTreeLeaf> rLeaf = BTreeLeaf.cast(right);
                newMidKey = BTreeLeaf.getKey(lLeaf, leftSize - n);
                BTreeLeaf.shiftEntries(rLeaf, 0, n, rightSize);
                BTreeLeaf.copyEntries(lLeaf, leftSize - n, rLeaf, 0, n);
            } else {
                final Page<BTreeBranch> lBranch = BTreeBranch.cast(left);
                final Page<BTreeBranch> rBranch = BTreeBranch.cast(right);
                newMidKey = BTreeBranch.getKey(lBranch, leftSize - n);
                BTreeBranch.shiftEntries(rBranch, 0, n, rightSize);
                BTreeBranch.setKey(rBranch, n - 1, midKey);
                BTreeBranch.setChildID(rBranch, n, BTreeBranch.getChildID(rBranch, 0));
                BTreeBranch.copyEntries(lBranch, leftSize - n + 1, rBranch, 0, n - 1);
                BTreeBranch.setChildID(rBranch, 0,
                        BTreeBranch.getChildID(lBranch, leftSize - n + 1));
            }
            BTreeBranch.setKey(page, leftPos, newMidKey);
            BTreePage.setNumKeys(left, leftSize - n);
            BTreePage.setNumKeys(right, rightSize + n);
        }
    }

    @Override
    protected int findKey(final Page<? extends BTreePage> page, final int key, final boolean leaf) {
        int leftIncl = 0;
        int rightExcl = BTreePage.getNumKeys(page);
        while (leftIncl < rightExcl) {
            final int mid = leftIncl + (rightExcl - leftIncl) / 2;
            final int currKey = leaf ? BTreeLeaf.getKey(BTreeLeaf.cast(page), mid)
                    : BTreeBranch.getKey(BTreeBranch.cast(page), mid);
            if (currKey < key) {
                leftIncl = mid + 1;
            } else if (currKey == key) {
                return mid;
            } else {
                rightExcl = mid;
            }
        }
        return -(leftIncl + 1);
    }

    @Override
    protected Page<BTreeLeaf> search(final PageID rootID, final int key) {
        final BufferManager bufferManager = this.getBufferManager();
        // get page from disk
        PageID pageID = rootID;
        Page<BTreePage> page = bufferManager.pinPage(pageID);
        while (!BTreePage.isLeafPage(page)) {
            // continue with child page
            final Page<BTreeBranch> branch = BTreeBranch.cast(page);
            pageID = BTreeBranch.getChildID(branch, Math.abs(this.findKey(branch, key, false) + 1));
            bufferManager.unpinPage(page, UnpinMode.CLEAN);
            page = bufferManager.pinPage(pageID);
        }
        return BTreeLeaf.cast(page);
    }

    @Override
    protected Entry insert(final PageID rootID, final int key, final RecordID value) {
        return this.insert(rootID, key, value, null, 0, 0);
    }

    /**
     * Recursive helper method for {@link #insert(PageID, int, RecordID)}.
     *
     * @param pageID     ID of the page to insert into
     * @param key        key to insert
     * @param value      record ID to insert
     * @param parentID   ID of the parent, {@code null} for the root page
     * @param parPos     position of the current page's ID in the parent page
     * @param parNumKeys number of keys in the parent page
     * @return an entry consisting of the middle key and the ID of the child page to insert
     * if the page was split as a result of the insertion, {@code null} otherwise
     */
    private Entry insert(final PageID pageID, final int key, final RecordID value,
                         final PageID parentID, final int parPos, final int parNumKeys) {
        final BufferManager bufferManager = this.getBufferManager();
        // get page from disk
        final Page<BTreePage> page = bufferManager.pinPage(pageID);

        if (BTreePage.isLeafPage(page)) {
            final Page<BTreeLeaf> leaf = BTreeLeaf.check(page);
            return this.insertLeaf(pageID, leaf, key, value, parentID, parPos, parNumKeys);
        }

        // current page is an inner node -- find the next child
        final Page<BTreeBranch> branch = BTreeBranch.check(page);
        final int numKeys = BTreePage.getNumKeys(branch);
        final int pos = Math.abs(this.findKey(branch, key, false) + 1);

        // continue insertion in child page
        final PageID childID = BTreeBranch.getChildID(branch, pos);
        final Entry newChild = this.insert(childID, key, value, pageID, pos, numKeys);

        if (newChild == null) {
            // no structural change
            bufferManager.unpinPage(branch, UnpinMode.CLEAN);
            return null;
        }

        // some space is left - insert the new entry and return
        if (numKeys < BTreeBranch.MAX_KEYS) {
            BTreeBranch.insertEntry(branch, pos, newChild.getMiddleKey(), newChild.getChildID());
            bufferManager.unpinPage(branch, UnpinMode.DIRTY);
            return null;
        }

        // we try to shift one entry into a neighboring page first
        if (parPos > 0) {
            // try to shift one entry into the previous sibling page
            final Page<BTreeBranch> parent = bufferManager.pinPage(parentID);
            final PageID prevID = BTreeBranch.getChildID(parent, parPos - 1);
            final Page<BTreeBranch> prev = bufferManager.pinPage(prevID);
            final int prevNumKeys = BTreePage.getNumKeys(prev);
            final int move = (numKeys - prevNumKeys + 1) / 2;
            if (move > 0) {
                // the previous page gets entries
                if (pos < move - 1) {
                    // the new entry goes into the left node
                    rotateLeft(parent, parPos - 1, prev, branch, move - 1);
                    BTreeBranch.insertEntry(prev, prevNumKeys + pos + 1,
                            newChild.getMiddleKey(), newChild.getChildID());
                } else if (pos == move - 1) {
                    // the new entry goes into the middle
                    rotateLeft(parent, parPos - 1, prev, branch, move);
                    final int midKey = BTreeBranch.getKey(parent, parPos - 1);
                    BTreeBranch.setKey(parent, parPos - 1, newChild.getMiddleKey());
                    BTreeBranch.insertEntry(branch, 0, midKey, BTreeBranch.getChildID(branch, 0));
                    BTreeBranch.setChildID(branch, 0, newChild.getChildID());
                } else {
                    // the new entry goes into the right node
                    rotateLeft(parent, parPos - 1, prev, branch, move);
                    BTreeBranch.insertEntry(branch, pos - move, newChild.getMiddleKey(), newChild.getChildID());
                }
                BTreePage.setNumKeys(prev, prevNumKeys + move);
                BTreePage.setNumKeys(branch, numKeys - move + 1);

                bufferManager.unpinPage(prev, UnpinMode.DIRTY);
                bufferManager.unpinPage(parent, UnpinMode.DIRTY);
                bufferManager.unpinPage(branch, UnpinMode.DIRTY);
                return null;
            }
            bufferManager.unpinPage(prev, UnpinMode.CLEAN);
            bufferManager.unpinPage(parent, UnpinMode.CLEAN);
        }

        if (parPos < parNumKeys) {
            // try to shift entries into the next sibling page
            final Page<BTreeBranch> parent = bufferManager.pinPage(parentID);
            final PageID nextID = BTreeBranch.getChildID(parent, parPos + 1);
            final Page<BTreeBranch> next = bufferManager.pinPage(nextID);
            final int nextNumKeys = BTreePage.getNumKeys(next);
            final int move = (numKeys - nextNumKeys + 1) / 2;
            if (move > 0) {
                // there's space left, rotate values over
                final int newNumKeys = numKeys - move + 1;
                if (pos < newNumKeys) {
                    // the new entry goes into the left node
                    rotateRight(parent, parPos, branch, next, move);
                    BTreeBranch.insertEntry(branch, pos, newChild.getMiddleKey(), newChild.getChildID());
                } else if (pos == newNumKeys) {
                    // the new entry goes into the middle
                    rotateRight(parent, parPos, branch, next, move - 1);
                    final int midKey = BTreeBranch.getKey(parent, parPos);
                    BTreeBranch.setKey(parent, parPos, newChild.getMiddleKey());
                    BTreeBranch.insertEntry(next, 0, midKey,
                            BTreeBranch.getChildID(next, 0));
                    BTreeBranch.setChildID(next, 0, newChild.getChildID());
                } else {
                    // the new entry goes into the right node
                    rotateRight(parent, parPos, branch, next, move - 1);
                    BTreeBranch.insertEntry(next, pos - newNumKeys - 1, newChild.getMiddleKey(), newChild.getChildID());
                }
                BTreePage.setNumKeys(branch, newNumKeys);
                BTreePage.setNumKeys(next, nextNumKeys + move);

                bufferManager.unpinPage(next, UnpinMode.DIRTY);
                bufferManager.unpinPage(parent, UnpinMode.DIRTY);
                bufferManager.unpinPage(branch, UnpinMode.DIRTY);
                return null;
            }
            bufferManager.unpinPage(next, UnpinMode.CLEAN);
            bufferManager.unpinPage(parent, UnpinMode.CLEAN);
        }

        // we have to split the page
        final Page<BTreeBranch> left = branch;
        final Page<BTreeBranch> right = BTreeBranch.newPage(bufferManager);
        final PageID rightID = right.getPageID();

        // choose page in which the new child will be inserted
        final int midKey;
        if (pos < BTreeBranch.MIN_KEYS) {
            // insert into left block
            midKey = BTreeBranch.getKey(left, BTreeBranch.MIN_KEYS - 1);
            BTreeBranch.setChildID(right, 0, BTreeBranch.getChildID(left, BTreeBranch.MIN_KEYS));
            BTreeBranch.copyEntries(left, BTreeBranch.MIN_KEYS, right, 0, BTreeBranch.MIN_KEYS);
            BTreePage.setNumKeys(left, BTreeBranch.MIN_KEYS - 1);
            BTreePage.setNumKeys(right, BTreeBranch.MIN_KEYS);
            BTreeBranch.insertEntry(left, pos, newChild.getMiddleKey(), newChild.getChildID());
        } else if (pos == BTreeBranch.MIN_KEYS) {
            // insert in the middle, the key goes up one level
            midKey = newChild.getMiddleKey();
            BTreeBranch.setChildID(right, 0, newChild.getChildID());
            BTreeBranch.copyEntries(left, BTreeBranch.MIN_KEYS, right, 0, BTreeBranch.MIN_KEYS);
            BTreePage.setNumKeys(left, BTreeBranch.MIN_KEYS);
            BTreePage.setNumKeys(right, BTreeBranch.MIN_KEYS);
        } else {
            // insert into right block
            midKey = BTreeBranch.getKey(left, BTreeBranch.MIN_KEYS);
            BTreeBranch.setChildID(right, 0, BTreeBranch.getChildID(left, BTreeBranch.MIN_KEYS + 1));
            BTreeBranch.copyEntries(
                    left, BTreeBranch.MIN_KEYS + 1, right, 0, BTreeBranch.MIN_KEYS - 1);
            BTreePage.setNumKeys(left, BTreeBranch.MIN_KEYS);
            BTreePage.setNumKeys(right, BTreeBranch.MIN_KEYS - 1);
            BTreeBranch.insertEntry(
                    right, pos - BTreeBranch.MIN_KEYS - 1, newChild.getMiddleKey(), newChild.getChildID());
        }

        // unpin pages and return new pointer
        bufferManager.unpinPage(branch, UnpinMode.DIRTY);
        bufferManager.unpinPage(right, UnpinMode.DIRTY);
        return new Entry(midKey, rightID);
    }

    /**
     * Inserts the given entry into the given leaf page.
     *
     * @param pageID     leaf page's ID
     * @param page       the page
     * @param key        key to insert
     * @param value      record ID to insert
     * @param parentID   page ID of the parent page, {@link PageID#INVALID} for the root page
     * @param parPos     position of this page's ID in the parent page
     * @param parNumKeys number of keys in the parent page
     * @return pair of middle key and new page ID if the page was split, {@code null}
     * otherwise
     */
    private Entry insertLeaf(final PageID pageID, final Page<BTreeLeaf> page, final int key,
                             final RecordID value, final PageID parentID, final int parPos,
                             final int parNumKeys) {
        // check if the leaf value is found
        final BufferManager bufferManager = this.getBufferManager();
        final int numKeys = BTreePage.getNumKeys(page);
        final int found = this.findKey(page, key, true);

        if (found >= 0) {
            // key already exists
            BTreeLeaf.setRecordID(page, found, value);
            bufferManager.unpinPage(page, UnpinMode.DIRTY);
            return null;
        }
        final int pos = -(found + 1);

        // key will be inserted, update index size
        this.incrementSize();

        if (numKeys < BTreeLeaf.MAX_KEYS) {
            // some space is left - insert the value and return
            BTreeLeaf.insertEntry(page, pos, key, value);
            bufferManager.unpinPage(page, UnpinMode.DIRTY);
            return null;
        }

        // try to avoid splitting the page by filling a neighboring one
        final PageID nextID = BTreeLeaf.getNextPage(page);
        if (parPos < parNumKeys) {
            final Page<BTreeLeaf> next = bufferManager.pinPage(nextID);
            final int nextNumKeys = BTreePage.getNumKeys(next);
            final int move = (numKeys - nextNumKeys + 1) / 2;
            if (move > 0) {
                // push values to the right node
                final Page<BTreeBranch> parent = bufferManager.pinPage(parentID);
                final int newNumKeys = numKeys - move + 1;
                if (pos < newNumKeys) {
                    rotateRight(parent, parPos, page, next, move);
                    BTreeLeaf.insertEntry(page, pos, key, value);
                } else if (pos == newNumKeys) {
                    rotateRight(parent, parPos, page, next, move - 1);
                    BTreeLeaf.insertEntry(next, 0, key, value);
                    BTreeBranch.setKey(parent, parPos, key);
                } else {
                    rotateRight(parent, parPos, page, next, move - 1);
                    BTreeLeaf.insertEntry(next, pos - newNumKeys, key, value);
                }
                bufferManager.unpinPage(parent, UnpinMode.DIRTY);
                bufferManager.unpinPage(next, UnpinMode.DIRTY);
                bufferManager.unpinPage(page, UnpinMode.DIRTY);
                return null;
            }
            bufferManager.unpinPage(next, UnpinMode.CLEAN);
        }

        final PageID prevID = BTreeLeaf.getPrevPage(page);
        if (parPos > 0) {
            final Page<BTreeLeaf> prev = bufferManager.pinPage(prevID);
            final int prevNumKeys = BTreePage.getNumKeys(prev);
            final int move = (numKeys - prevNumKeys + 1) / 2;
            if (move > 0) {
                // push values to the left node
                final Page<BTreeBranch> parent = bufferManager.pinPage(parentID);
                if (pos == 0 && move == 1) {
                    // no rotation, entry goes to the left page
                    BTreeLeaf.insertEntry(prev, prevNumKeys, key, value);
                    BTreeBranch.setKey(parent, parPos - 1, BTreeLeaf.getKey(page, 0));
                    bufferManager.unpinPage(page, UnpinMode.CLEAN);
                } else {
                    if (pos < move) {
                        rotateLeft(parent, parPos - 1, prev, page, move - 1);
                        BTreeLeaf.insertEntry(prev, prevNumKeys + pos, key, value);
                    } else if (pos == move) {
                        rotateLeft(parent, parPos - 1, prev, page, move);
                        BTreeLeaf.insertEntry(page, 0, key, value);
                        BTreeBranch.setKey(parent, parPos - 1, key);
                    } else {
                        rotateLeft(parent, parPos - 1, prev, page, move);
                        BTreeLeaf.insertEntry(page, pos - move, key, value);
                    }
                    bufferManager.unpinPage(page, UnpinMode.DIRTY);
                }
                bufferManager.unpinPage(parent, UnpinMode.DIRTY);
                bufferManager.unpinPage(prev, UnpinMode.DIRTY);
                return null;
            }
            bufferManager.unpinPage(prev, UnpinMode.CLEAN);
        }

        // no space is left - allocate new page and distribute entries
        final Page<BTreeLeaf> right = BTreeLeaf.newPage(bufferManager, pageID, nextID);
        final PageID rightID = right.getPageID();
        BTreeLeaf.setNextPage(page, rightID);

        if (nextID.isValid()) {
            // update the next page's previous-page pointer
            final Page<BTreeLeaf> next = bufferManager.pinPage(nextID);
            BTreeLeaf.setPrevPage(next, rightID);
            bufferManager.unpinPage(next, UnpinMode.DIRTY);
        }

        // distribute the entries
        BTreeLeaf.copyEntries(page, BTreeLeaf.MIN_KEYS, right, 0, BTreeLeaf.MIN_KEYS);
        BTreePage.setNumKeys(page, BTreeLeaf.MIN_KEYS);
        BTreePage.setNumKeys(right, BTreeLeaf.MIN_KEYS);

        // choose page in which the new entry will be inserted
        if (pos < BTreeLeaf.MIN_KEYS) {
            BTreeLeaf.insertEntry(page, pos, key, value);
        } else {
            BTreeLeaf.insertEntry(right, pos - BTreeLeaf.MIN_KEYS, key, value);
        }

        final int midKey = BTreeLeaf.getKey(right, 0);
        bufferManager.unpinPage(page, UnpinMode.DIRTY);
        bufferManager.unpinPage(right, UnpinMode.DIRTY);
        return new Entry(midKey, rightID);
    }

    @Override
    protected RemovalResult remove(final int key, final PageID pageID) {
        // get page from disk
        final BufferManager bufferManager = this.getBufferManager();
        final Page<BTreePage> page = bufferManager.pinPage(pageID);
        final int numKeys = BTreePage.getNumKeys(page);

        // check if the current node is a leaf node
        if (BTreePage.isLeafPage(page)) {
            final Page<BTreeLeaf> leaf = BTreeLeaf.cast(page);
            final int pos = this.findKey(leaf, key, true);

            if (pos < 0) {
                // key not found, nothing to do
                bufferManager.unpinPage(leaf, UnpinMode.CLEAN);
                return RemovalResult.NOT_FOUND;
            }

            // key was found
            BTreeLeaf.deleteEntry(leaf, pos);

            // update index size
            this.decrementSize();

            bufferManager.unpinPage(leaf, UnpinMode.DIRTY);
            // check if page is still full enough
            if (numKeys > BTreeLeaf.MIN_KEYS) {
                return RemovalResult.NO_MERGE;
            } else {
                return RemovalResult.MERGE;
            }
        }

        // current page is no leaf - find the next child page
        final Page<BTreeBranch> branch = BTreeBranch.cast(page);
        final int pos = Math.abs(this.findKey(branch, key, false) + 1);
        final PageID childID = BTreeBranch.getChildID(branch, pos);

        // return when value was not found or everything is done
        final RemovalResult response = this.remove(key, childID);
        if (response != RemovalResult.MERGE) {
            bufferManager.unpinPage(branch, UnpinMode.CLEAN);
            return response;
        }

        // child is under-full
        final Page<BTreePage> child = bufferManager.pinPage(childID);
        final int childSize = BTreePage.getNumKeys(child);

        if (pos > 0) {
            // try to steal entries from the left
            final PageID leftID = BTreeBranch.getChildID(branch, pos - 1);
            final Page<BTreePage> left = bufferManager.pinPage(leftID);
            final int leftSize = BTreePage.getNumKeys(left);
            final int move = (leftSize - childSize) / 2;
            if (move > 0) {
                rotateRight(branch, pos - 1, left, child, move);
                bufferManager.unpinPage(left, UnpinMode.DIRTY);
                bufferManager.unpinPage(child, UnpinMode.DIRTY);
                bufferManager.unpinPage(branch, UnpinMode.DIRTY);
                return RemovalResult.NO_MERGE;
            }
            bufferManager.unpinPage(left, UnpinMode.CLEAN);
        }

        if (pos < numKeys) {
            // try to steal entries from the right
            final PageID rightID = BTreeBranch.getChildID(branch, pos + 1);
            final Page<BTreeBranch> right = bufferManager.pinPage(rightID);
            final int rightSize = BTreePage.getNumKeys(right);
            final int move = (rightSize - childSize) / 2;
            if (move > 0) {
                rotateLeft(branch, pos, child, right, move);
                bufferManager.unpinPage(right, UnpinMode.DIRTY);
                bufferManager.unpinPage(child, UnpinMode.DIRTY);
                bufferManager.unpinPage(branch, UnpinMode.DIRTY);
                return RemovalResult.NO_MERGE;
            }
            bufferManager.unpinPage(right, UnpinMode.CLEAN);
        }

        // merge with a neighbor
        final int leftPos = pos > 0 ? pos - 1 : pos;
        bufferManager.unpinPage(child, UnpinMode.CLEAN);
        this.mergeChildren(branch, leftPos);

        if (numKeys == 1) {
            // we're the root node and there's no more sibling,
            // make the only child node the root
            this.setRootID(BTreeBranch.getChildID(branch, leftPos));
            bufferManager.freePage(branch);
            return RemovalResult.NO_MERGE;
        }

        BTreeBranch.deleteEntry(branch, leftPos);
        bufferManager.unpinPage(branch, UnpinMode.DIRTY);
        if (numKeys > BTreeBranch.MIN_KEYS) {
            return RemovalResult.NO_MERGE;
        } else {
            return RemovalResult.MERGE;
        }
    }

    /**
     * Merges two children of the given page, namely those at positions {@code leftPos} and
     * {@code leftPos + 1}. The right one is deleted after that.
     *
     * @param page    parent page
     * @param leftPos position of the left child to be merged
     */
    private void mergeChildren(final Page<BTreeBranch> page, final int leftPos) {
        final BufferManager bufferManager = this.getBufferManager();
        final Page<BTreePage> left = bufferManager.pinPage(BTreeBranch.getChildID(page, leftPos));
        final Page<BTreePage> right = bufferManager.pinPage(BTreeBranch.getChildID(page, leftPos + 1));
        final int leftSize = BTreePage.getNumKeys(left);
        final int rightSize = BTreePage.getNumKeys(right);
        if (BTreePage.isLeafPage(left)) {
            // just concatenate the entries
            final Page<BTreeLeaf> lLeaf = BTreeLeaf.cast(left);
            final Page<BTreeLeaf> rLeaf = BTreeLeaf.cast(right);
            BTreeLeaf.copyEntries(rLeaf, 0, lLeaf, leftSize, rightSize);
            BTreePage.setNumKeys(lLeaf, leftSize + rightSize);

            // fix the previous- and next-pointers
            final PageID nextID = BTreeLeaf.getNextPage(rLeaf);
            BTreeLeaf.setNextPage(lLeaf, nextID);
            if (nextID.isValid()) {
                final Page<BTreeLeaf> next = bufferManager.pinPage(nextID);
                BTreeLeaf.setPrevPage(next, lLeaf.getPageID());
                bufferManager.unpinPage(next, UnpinMode.DIRTY);
            }
        } else {
            // we have to stick the middle value from the parent back in
            final Page<BTreeBranch> lBranch = BTreeBranch.cast(left);
            final Page<BTreeBranch> rBranch = BTreeBranch.cast(right);
            final int midKey = BTreeBranch.getKey(page, leftPos);
            final PageID firstID = BTreeBranch.getChildID(rBranch, 0);
            BTreeBranch.insertEntry(lBranch, leftSize, midKey, firstID);
            BTreeBranch.copyEntries(rBranch, 0, lBranch, leftSize + 1, rightSize);
            BTreePage.setNumKeys(lBranch, leftSize + 1 + rightSize);
        }

        bufferManager.unpinPage(left, UnpinMode.DIRTY);
        bufferManager.freePage(right);
    }
}
