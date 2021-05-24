/*
 * @(#)BTreeIndex.java   1.0   Oct 23, 2013
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2016 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.btree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import minibase.RecordID;
import minibase.access.IndexEntry;
import minibase.access.IndexScan;
import minibase.storage.buffer.BufferManager;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;
import minibase.storage.buffer.UnpinMode;
import minibase.storage.file.DiskManager;


/**
 * A B+-Tree index structure.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public abstract class BTreeIndex implements AutoCloseable {

    /**
     * This index's buffer manager.
     */
    private final BufferManager bufferManager;
    /**
     * Name of the index's logical file.
     */
    private final String name;
    /**
     * The header page, permanently pinned, {@code null} if the index was closed.
     */
    private Page<BTreeHeader> header;

    /**
     * Constructor for opening an existing index.
     *
     * @param bufferManager the buffer manager
     * @param name          name of the index
     * @param header        the index's header page
     */
    BTreeIndex(final BufferManager bufferManager, final String name, final Page<BTreeHeader> header) {
        this.bufferManager = bufferManager;
        this.name = name;
        this.header = header;
    }

    // static factory methods

    /**
     * Creates and opens a new B+-tree index with the given name.
     *
     * @param bufferManager buffer manager
     * @param name          the index's name
     * @return the newly created index
     * @throws IllegalArgumentException if an index with the given name already exists
     */
    public static BTreeIndex createIndex(final BufferManager bufferManager, final String name) {
        if (name != null && bufferManager.getDiskManager().getFileEntry(name) != null) {
            throw new IllegalArgumentException("An index with the name '" + name + "' already exists.");
        }

        // initialize root page
        final Page<BTreeLeaf> root = BTreeLeaf.newPage(bufferManager, PageID.INVALID, PageID.INVALID);
        final PageID rootID = root.getPageID();
        bufferManager.unpinPage(root, UnpinMode.DIRTY);

        // initialize header page
        final Page<BTreeHeader> header = BTreeHeader.newPage(bufferManager, rootID);

        // add the index file
        if (name != null) {
            bufferManager.getDiskManager().addFileEntry(name, header.getPageID());
        }

        // return the index
        return new BTreeIndexImpl(bufferManager, name, header);
    }

    /**
     * Tries to open an existing B+-tree index with the given name.
     *
     * @param bufferManager the buffer manager
     * @param name          the index's name
     * @return the index if it exists, {@code null} otherwise
     * @throws IllegalArgumentException if the index does not exist
     */
    public static BTreeIndex openIndex(final BufferManager bufferManager, final String name) {
        final PageID headerID = bufferManager.getDiskManager().getFileEntry(name);
        if (headerID == null) {
            throw new IllegalArgumentException("There is no index with the name '" + name + "'.");
        }
        final Page<BTreeHeader> header = bufferManager.pinPage(headerID);
        return new BTreeIndexImpl(bufferManager, name, header);
    }

    /**
     * Deletes the B+-tree index with the given name.
     *
     * @param bufferManager the buffer manager
     * @param indexName     the index's name
     * @throws IllegalArgumentException if the index does not exist
     */
    public static void dropIndex(final BufferManager bufferManager, final String indexName) {
        final PageID headerID = bufferManager.getDiskManager().getFileEntry(indexName);
        if (headerID == null) {
            // index does not exist
            throw new IllegalArgumentException("There is no index with the name '" + indexName + "'.");
        }

        // get the root page ID and delete the header page
        final Page<BTreeHeader> header = bufferManager.pinPage(headerID);
        final PageID rootID = BTreeHeader.getRootID(header);
        bufferManager.freePage(header);

        // delete the tree pages
        final Deque<PageID> stack = new ArrayDeque<>();
        stack.add(rootID);
        do {
            final PageID pageID = stack.pop();
            final Page<BTreePage> page = bufferManager.pinPage(pageID);
            if (!BTreePage.isLeafPage(page)) {
                final Page<BTreeBranch> branch = BTreeBranch.cast(page);
                final int numChildren = BTreePage.getNumKeys(branch) + 1;
                for (int childPos = 0; childPos < numChildren; childPos++) {
                    stack.push(BTreeBranch.getChildID(branch, childPos));
                }
            }
            bufferManager.freePage(page);
        } while (!stack.isEmpty());

        // delete the logical file
        bufferManager.getDiskManager().deleteFileEntry(indexName);
    }

    /**
     * Returns the name of this index's logical file.
     *
     * @return the index name
     */
    public final Optional<String> getFileName() {
        return Optional.ofNullable(this.name);
    }

    // public interface

    /**
     * Returns the number of entries stored in this index.
     *
     * @return the number of entries
     */
    public final int size() {
        return BTreeHeader.getSize(this.header);
    }

    /**
     * Retrieves the record ID stored with the given key.
     *
     * @param key key to look up
     * @return the record ID if found, {@code null} otherwise
     */
    public final Optional<IndexEntry> search(final int key) {
        Objects.requireNonNull(key);
        final PageID rootID = BTreeHeader.getRootID(this.header);
        final Page<BTreeLeaf> leaf = this.search(rootID, key);
        final int pos = this.findKey(leaf, key, true);
        final RecordID result = pos >= 0 ? BTreeLeaf.getRecordID(leaf, pos) : null;
        this.bufferManager.unpinPage(leaf, UnpinMode.CLEAN);
        if (result == null) {
            return Optional.empty();
        } else {
            return Optional.of(new IndexEntry(key, result));
        }
    }

    /**
     * Returns an iterator that traverses all entries in ascending order.
     *
     * @return iterator over the index entries
     */
    public final IndexScan openScan() {
        return this.openScan(Integer.MIN_VALUE);
    }

    /**
     * Returns an iterator that traverses all entries with keys greater than or equal to {@code startKey}
     * in ascending order.
     *
     * @param startKey key to start the scan from
     * @return iterator over the index entries
     */
    public final IndexScan openScan(final int startKey) {
        // navigate to the first page that may contain results
        final PageID rootID = BTreeHeader.getRootID(this.header);
        final Page<BTreeLeaf> leaf = this.search(rootID, startKey);
        final int pos = this.findKey(leaf, startKey, true);
        return new BTreeIterator(this.bufferManager, leaf, pos >= 0 ? pos : -pos - 1);
    }

    /**
     * Inserts an entry into this index.
     * If an entry with the given key in already present, the corresponding record ID is replaced.
     *
     * @param key   key to be added
     * @param value value to be added
     */
    public final void insert(final int key, final RecordID value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        final PageID rootID = BTreeHeader.getRootID(this.header);
        final Entry newChild = this.insert(rootID, key, value);
        if (newChild != null) {
            // the root node was split
            final Page<BTreeBranch> newRoot = BTreeBranch.newPage(this.bufferManager);
            final PageID newRootID = newRoot.getPageID();
            BTreeBranch.setChildID(newRoot, 0, BTreeHeader.getRootID(this.header));
            BTreeBranch.insertEntry(newRoot, 0, newChild.getMiddleKey(), newChild.getChildID());
            this.bufferManager.unpinPage(newRoot, UnpinMode.DIRTY);

            // set the new root ID
            BTreeHeader.setRootID(this.header, newRootID);
        }
    }

    /**
     * Removes the entry with the given key from this index if it exists.
     *
     * @param key key of the entry to be removed
     * @return {@code true} if than entry with the given key was found (and deleted), {@code false} otherwise
     */
    public final boolean remove(final int key) {
        Objects.requireNonNull(key);
        final PageID rootID = BTreeHeader.getRootID(this.getHeader());
        return this.remove(key, rootID) != RemovalResult.NOT_FOUND;
    }

    @Override
    public final void close() {
        if (this.header != null) {
            this.bufferManager.unpinPage(this.header, UnpinMode.DIRTY);
            this.header = null;
        }
    }

    /**
     * Deletes this index, freeing all pages.
     */
    public final void delete() {

        if (this.getFileName().isPresent()) {
            final PageID headerID = this.getBufferManager()
                    .getDiskManager().getFileEntry(this.getFileName().get());
            if (headerID == null) {
                // index does not exist
                throw new IllegalArgumentException("There is no index with the name '"
                        + this.getFileName() + "'.");
            }
        }

        // get the root page ID and delete the header page
        final Page<BTreeHeader> header = this.getHeader();
        final PageID rootID = BTreeHeader.getRootID(header);


        // delete the tree pages
        final ArrayDeque<PageID> stack = new ArrayDeque<>();
        stack.add(rootID);
        do {
            final PageID pageID = stack.pop();
            final Page<BTreePage> page = this.getBufferManager().pinPage(pageID);
            if (!BTreePage.isLeafPage(page)) {
                final Page<BTreeBranch> branch = BTreeBranch.cast(page);
                final int numChildren = BTreePage.getNumKeys(branch) + 1;
                for (int childPos = 0; childPos < numChildren; childPos++) {
                    stack.push(BTreeBranch.getChildID(branch, childPos));
                }
            }
            this.getBufferManager().freePage(page);
        } while (!stack.isEmpty());

        // delete the logical file
        if (this.getFileName().isPresent()) {
            this.getBufferManager().getDiskManager().deleteFileEntry(this.getFileName().get());
        }
        this.getBufferManager().freePage(this.getHeader());
        this.removeHeader();
    }

    /**
     * Finds the position of the given key in this page. If the key is not contained in the page, this method returns
     * {@code -(insPos + 1)} where {@code insPos} is the position where {@code key} would have to be inserted.
     *
     * @param page the page
     * @param key  key to find
     * @param leaf if the given page is a leaf page
     * @return position of the key if found, {@code -(insPos + 1)} otherwise
     */
    protected abstract int findKey(Page<? extends BTreePage> page, int key, boolean leaf);

    // abstract core algorithms

    /**
     * Finds the leaf page that {@code key} belongs in and returns it pinned.
     *
     * @param pageID ID of the current page
     * @param key    key to find the leaf page for
     * @return the leaf page
     */
    protected abstract Page<BTreeLeaf> search(PageID pageID, int key);

    /**
     * Inserts the given entry in or below the page with the given ID.
     * If an entry with the given key in already present, the corresponding record ID is replaced.
     *
     * @param pageID current page's ID
     * @param key    search key to insert
     * @param value  record ID to insert
     * @return pair of middle key and ID of new (right) page if the insert lead to a split, {@code null} otherwise
     */
    protected abstract Entry insert(PageID pageID, int key, RecordID value);

    /**
     * Deletes the entry with the given key from or below the page with the given ID.
     * If the key does not exist, the index is left unchanged.
     *
     * @param key    key to delete
     * @param pageID current page ID
     * @return {@code true} if the page is still full enough, {@code false} otherwise
     */
    protected abstract RemovalResult remove(int key, PageID pageID);

    /**
     * Gets this index's buffer manager.
     *
     * @return the buffer manager
     */
    final BufferManager getBufferManager() {
        return this.bufferManager;
    }

    // helper methods

    /**
     * Increments the size of this index on the header page.
     */
    final void incrementSize() {
        BTreeHeader.setSize(this.header, BTreeHeader.getSize(this.header) + 1);
    }

    /**
     * Decrements the size of this index on the header page.
     */
    final void decrementSize() {
        BTreeHeader.setSize(this.header, BTreeHeader.getSize(this.header) - 1);
    }

    /**
     * Reads the current root ID from the header page.
     *
     * @return current root page's ID
     */
    final PageID getRootID() {
        return BTreeHeader.getRootID(this.header);
    }

    /**
     * writes a new root ID to the header page.
     *
     * @param newRootID new root page's ID
     */
    final void setRootID(final PageID newRootID) {
        BTreeHeader.setRootID(this.header, newRootID);
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder("BTreeIndex[\n");
        this.toString(sb, BTreeHeader.getRootID(this.header), 1);
        return sb.append(",\n    size=").append(this.size()).append("\n]").toString();
    }

    // methods for testing

    /**
     * Recursive helper method for {@link #toString()}.
     *
     * @param sb     string builder
     * @param id     current page ID
     * @param indent indentation depth
     */
    private void toString(final StringBuilder sb, final PageID id, final int indent) {
        final Page<BTreePage> page = this.bufferManager.pinPage(id);
        final int numKeys = BTreePage.getNumKeys(page);
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
        if (BTreePage.isLeafPage(page)) {
            final Page<BTreeLeaf> leaf = BTreeLeaf.cast(page);
            sb.append("Leaf#").append(id).append("{").append(numKeys).append("}[");
            for (int i = 0; i < numKeys; i++) {
                sb.append(i == 0 ? "" : ", ");
                sb.append(BTreeLeaf.getKey(leaf, i))
                        .append("=").append(BTreeLeaf.getRecordID(leaf, i));
            }
        } else {
            final Page<BTreeBranch> branch = BTreeBranch.cast(page);
            sb.append("Branch#").append(id).append("{").append(numKeys).append("}[\n");
            this.toString(sb, BTreeBranch.getChildID(branch, 0), indent + 1);
            for (int i = 0; i < numKeys; i++) {
                sb.append(",\n");
                for (int j = 0; j < indent; j++) {
                    sb.append("    ");
                }
                sb.append("  ").append(BTreeBranch.getKey(branch, i)).append(",\n");
                this.toString(sb, BTreeBranch.getChildID(branch, i + 1), indent + 1);
            }
            sb.append("\n");
            for (int i = 0; i < indent; i++) {
                sb.append("    ");
            }
        }
        sb.append(']');
        this.bufferManager.unpinPage(page, UnpinMode.CLEAN);
    }

    /**
     * Checks if all invariants hold for this B+-Tree.
     *
     * @throws AssertionError if invariants are violated
     */
    public final void checkInvariants() {
        final int[] prevNext = {-1, -2};
        final PageID rootID = BTreeHeader.getRootID(this.header);
        final int size = BTreeHeader.getSize(this.header);
        if (this.checkInvariants(rootID, new Integer[2], prevNext) != size) {
            throw new AssertionError("Wrong size in header page: " + size);
        }

        if (prevNext[1] >= 0) {
            throw new AssertionError("Last leaf page should have an invalid next-page ID.");
        }
    }

    /**
     * Recursive helper method for {@link #checkInvariants()}.
     *
     * @param pageID   current page ID
     * @param minMax   minimum allowed key in the current page (inclusive) and
     *                 maximum allowed key in the current page (exclusive)
     * @param prevNext two-element array containing the previous leaf page's ID
     *                 and the expected next leaf page's ID ({@code -2} for the first leaf)
     * @return number of entries in the sub-tree under the current page
     */
    private int checkInvariants(final PageID pageID, final Integer[] minMax, final int[] prevNext) {
        final Page<BTreePage> page = this.bufferManager.pinPage(pageID);
        final int keys = BTreePage.getNumKeys(page);
        if (BTreePage.isLeafPage(page)) {
            // check leaf page
            final Page<BTreeLeaf> leaf = BTreeLeaf.cast(page);
            if (!pageID.equals(BTreeHeader.getRootID(this.header)) && keys < BTreeLeaf.MIN_KEYS) {
                throw new AssertionError("Non-root page " + pageID + " is under-full: " + keys);
            }

            final PageID prv = BTreeLeaf.getPrevPage(leaf);
            if (prevNext[0] != prv.getValue()) {
                throw new AssertionError("ID " + prv + " of previous page is wrong, expected " + prevNext[0]);
            }
            prevNext[0] = pageID.getValue();

            final PageID nxt = BTreeLeaf.getNextPage(leaf);
            if (prevNext[1] != -2 && prevNext[1] != pageID.getValue()) {
                throw new AssertionError("Last leaf page " + prv + " linked to " + prevNext[1]
                        + ", but next leaf page is " + pageID);
            }
            prevNext[1] = nxt.getValue();

            int before = BTreeLeaf.getKey(leaf, 0);
            if (minMax[0] != null && before < minMax[0]) {
                throw new AssertionError("Wrong key found, " + before + " not in allowed range "
                        + "[" + minMax[0] + "; " + (minMax[1] == null ? "+Infinity" : minMax[1].toString()) + ").");
            }
            minMax[0] = before;

            for (int i = 1; i < keys; i++) {
                final int key = BTreeLeaf.getKey(leaf, i);
                if (key <= before) {
                    throw new AssertionError(
                            "Wrong key found, " + key + " must be greater than preceding key " + before);
                }
                before = key;
            }
            if (minMax[1] != null && before >= minMax[1]) {
                throw new AssertionError("Wrong key found, " + before + " not in allowed range "
                        + "[" + minMax[0] + "; " + minMax[1] + ").");
            }
            minMax[1] = before;

            this.bufferManager.unpinPage(page, UnpinMode.CLEAN);
            return keys;
        }

        // check branch page
        final Page<BTreeBranch> branch = BTreeBranch.cast(page);
        if (keys < (pageID.equals(BTreeHeader.getRootID(this.header)) ? 1 : BTreeBranch.MIN_KEYS)) {
            throw new AssertionError("Branch page " + pageID + " is under-full: " + keys);
        }


        final PageID firstChild = BTreeBranch.getChildID(branch, 0);
        if (!firstChild.isValid()) {
            throw new AssertionError("Invalid first child page ID on page " + pageID + ": " + firstChild);
        }

        Integer before = BTreeBranch.getKey(branch, 0);
        if (minMax[0] != null && minMax[0].compareTo(before) >= 0) {
            throw new AssertionError("Empty key range on page " + pageID + ": [" + minMax[0] + "; " + before + ")");
        }

        final Integer maxExcl = minMax[1];
        minMax[1] = before;
        int size = this.checkInvariants(firstChild, minMax, prevNext);
        final int min = minMax[0];

        for (int i = 1; i <= keys; i++) {
            minMax[0] = before;
            minMax[1] = i < keys ? Integer.valueOf(BTreeBranch.getKey(branch, i)) : maxExcl;
            before = minMax[1];

            if (minMax[1] != null && minMax[0].compareTo(minMax[1]) >= 0) {
                throw new AssertionError("Invalid key range on page " + pageID
                        + ": [" + minMax[0] + "; " + minMax[1] + ")");
            }
            final PageID child = BTreeBranch.getChildID(branch, i);
            if (!child.isValid()) {
                throw new AssertionError("Invalid child page ID on page " + pageID + ": " + child);
            }
            // check the descendants recursively
            size += this.checkInvariants(child, minMax, prevNext);
        }
        minMax[0] = min;
        this.bufferManager.unpinPage(page, UnpinMode.CLEAN);
        return size;
    }

    /**
     * Returns a string containing some statistics about this index.
     *
     * @return the description
     */
    final String getStatistics() {
        final List<Double> vals = new ArrayList<>();
        this.getSpaceUsage(BTreeHeader.getRootID(this.header), vals);
        double min = Double.MAX_VALUE;
        double sum = 0;
        double max = -Double.MAX_VALUE;

        final int buckets = 16;
        final int[] histo = new int[buckets];
        int maxH = 0;
        for (final double d : vals) {
            min = Math.min(min, d);
            sum += d;
            max = Math.max(max, d);
            final int h = ++histo[Math.min((int) (buckets * d), buckets - 1)];
            maxH = Math.max(maxH, h);
        }

        final int numPages = vals.size() + 1;
        final StringBuilder sb = new StringBuilder();
        sb.append(" - ").append(numPages).append(" pages\n");
        sb.append(" - ").append(this.size()).append(" entries\n");

        final int bytes = numPages * DiskManager.PAGE_SIZE;
        if (bytes >= 1L << 30) {
            sb.append(String.format(" - %.1f GiB on disk\n", 1.0 * bytes / (1L << 30)));
        } else if (bytes >= 1L << 20) {
            sb.append(String.format(" - %.1f MiB on disk\n", 1.0 * bytes / (1L << 20)));
        } else if (bytes >= 1L << 10) {
            sb.append(String.format(" - %.1f KiB on disk\n", 1.0 * bytes / (1L << 10)));
        } else {
            sb.append(String.format(" - %.1f Bytes on disk\n", 1.0 * bytes));
        }

        sb.append(String.format(" - %.2f%% %s page space used\n", 100 * min, "minimum"));
        sb.append(String.format(" - %.2f%% %s page space used\n", 100 * sum / vals.size(), "average"));
        sb.append(String.format(" - %.2f%% %s page space used\n", 100 * max, "maximum"));

        sb.append(" - Histogram: [");
        for (final int h : histo) {
            final int cp = h == 0 ? ' ' : '\u2581' + Math.min((int) (8.0 * h / maxH), 7);
            sb.append((char) cp);
        }
        return sb.append("]").toString();
    }

    /**
     * Gathers the space usage of all pages recursively.
     *
     * @param pageID the current page's ID
     * @param vals   list to append to
     */
    private void getSpaceUsage(final PageID pageID, final List<Double> vals) {
        final Page<BTreePage> page = this.bufferManager.pinPage(pageID);
        final int numKeys = BTreePage.getNumKeys(page);
        final Page<BTreeBranch> branch = BTreeBranch.check(page);

        if (!pageID.equals(BTreeHeader.getRootID(this.header))) {
            vals.add(1.0 * numKeys / (branch == null ? BTreeLeaf.MAX_KEYS : BTreeBranch.MAX_KEYS));
        }

        if (branch != null) {
            for (int i = 0; i <= numKeys; i++) {
                final PageID childID = BTreeBranch.getChildID(branch, i);
                this.getSpaceUsage(childID, vals);
            }
        }

        this.bufferManager.unpinPage(page, UnpinMode.CLEAN);
    }

    /**
     * Returns the id of the header page of the tree.
     *
     * @return headID
     */
    public Page<BTreeHeader> getHeader() {
        return this.header;
    }

    /**
     * Sets the header to {@code null}.
     */
    protected void removeHeader() {
        this.header = null;
    }

    /**
     * Possible results when trying to delete a key from the index.
     */
    enum RemovalResult {
        /**
         * The key was not found in the index.
         */
        NOT_FOUND,
        /**
         * The removal was successful and the node is still full enough.
         */
        NO_MERGE,
        /**
         * The tree node is too empty. It has to be merged.
         */
        MERGE;
    }

    /**
     * Result of a node split, i.e. a pair of the middle key and the page ID of the new page.
     */
    static class Entry {
        /**
         * The middle key.
         */
        private final int middleKey;
        /**
         * The page ID of the new page.
         */
        private final PageID childID;

        /**
         * Constructs a result of a node split.
         *
         * @param middleKey middle key
         * @param childID   page ID of the new page
         */
        Entry(final int middleKey, final PageID childID) {
            this.middleKey = middleKey;
            this.childID = childID;
        }

        /**
         * Returns the child page ID.
         *
         * @return child page ID
         */
        PageID getChildID() {
            return this.childID;
        }

        /**
         * Returns the middle key.
         *
         * @return middle key
         */
        int getMiddleKey() {
            return this.middleKey;
        }
    }
}
