/*
 * @(#)BTreeIndexTest.java   1.0   Oct 23, 2013
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2016 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.btree;

import minibase.BaseTest;
import minibase.RecordID;
import minibase.access.IndexEntry;
import minibase.access.IndexScan;
import minibase.storage.buffer.BufferManager;
import minibase.storage.buffer.BufferManagerImpl;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;
import minibase.storage.file.DiskFile;
import minibase.storage.file.DiskManager;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;

/**
 * Tests for the B+-Tree index.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public class BTreeIndexTest extends BaseTest {

    /**
     * The index's name.
     */
    private static final String INDEX_NAME = "BTreeIndexTest.btree";

    /**
     * Test file containing a B+ tree.
     */
    private static final Path INDEX_FILE = Path.of("src/test/resources", INDEX_NAME + ".gz");

    /**
     * Returns a new record ID where both the page ID and the slot number have the given
     * value.
     *
     * @param val value for page ID and slot number
     * @return the record ID
     */
    private static RecordID newRecordID(final int val) {
        return new RecordID(PageID.getInstance(val), val);
    }

    /**
     * Simple use of temp index.
     *
     * @throws IOException I/O exception while closing
     */
    @Test
    public void simpleTempIndex() throws IOException {
        this.initRandom();

        // creating temp index...
        final BTreeIndex temp = BTreeIndex.createIndex(this.getBufferManager(), null);
        final int num = 1000;
        final int[] inserted = new int[num];
        final Random rng = this.getRandom();
        for (int i = 0; i < num; i++) {
            final int key = rng.nextInt();
            final RecordID rid = newRecordID(i);
            temp.insert(key, rid);
            inserted[i] = key;
            for (int j = 0; j <= i; j++) {
                final int searchedKey = inserted[j];
                final Optional<IndexEntry> entry = temp.search(searchedKey);
                assertTrue("Entry #" + j + " could not be found after inserting entry #" + i + ".", entry.isPresent());
                final RecordID ridFound = entry.get().getRecordID();
                assertEquals(j, ridFound.getPageID().getValue());
            }
        }
        temp.delete();
    }

    /**
     * Tiny fuzzy test.
     *
     * @throws IOException if something with the index file is wrong
     */
    @Test
    public void fuzzyTestTiny() throws IOException {
        this.fuzzyTest(100, 10);
    }

    /**
     * Small fuzzy test.
     *
     * @throws IOException if something with the index file is wrong
     */
    @Test
    public void fuzzyTestSmall() throws IOException {
        this.fuzzyTest(1000, 100);
    }

    /**
     * Normal fuzzy test.
     *
     * @throws IOException if something with the index file is wrong
     */
    @Test
    public void fuzzyTestNormal() throws IOException {
        this.fuzzyTest(100000, 10000);
    }

    /**
     * Big fuzzy test.
     *
     * @throws IOException if something with the index file is wrong
     */
    @Test
    @Ignore
    public void fuzzyTestBig() throws IOException {
        this.fuzzyTest(500000, 10000);
    }

    /**
     * Executes the given number of random operations (insert, delete, lookup) with the given number of
     * distinct keys.
     *
     * @param ops     number of operations
     * @param numKeys number of distinct keys
     * @throws IOException I/O exception
     */
    private void fuzzyTest(final int ops, final int numKeys) throws IOException {
        System.out.format("%s(%d, %d)\n", "fuzzyTest", ops, numKeys);
        final BufferManager bufferManager = this.getBufferManager();
        final int pages = bufferManager.getDiskManager().getAllocCount();
        final String fileName = "fuzzyTest";
        final Random rand = this.getRandom();

        final int[] keys = new int[numKeys];
        final RecordID[] recordIDs = new RecordID[numKeys];
        for (int i = 0; i < numKeys; i++) {
            final int keyInt = rand.nextInt();
            keys[i] = keyInt;
            recordIDs[i] = newRecordID(keyInt);
        }

        BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), fileName);
        final Map<Integer, RecordID> map = new HashMap<>();

        final int oldCount = this.getBufferManager().getNumPinned();
        for (int i = 0; i < ops; i++) {
            if ((i + 1) % 10000 == 0) {
                System.out.println(i + 1 + " operations:");
            }

            final int rk = rand.nextInt(numKeys);
            final int key = keys[rk];
            final RecordID recID = recordIDs[rk];
            final int nextOp = rand.nextInt(3003);
            if (nextOp < 1000) {
                // insert value
                map.put(key, recID);
                index.insert(key, recID);
                final IndexEntry found = index.search(key).get();
                assertEquals(recID, found.getRecordID());
            } else if (nextOp < 2000) {
                // random key lookup
                final Optional<IndexEntry> data = index.search(key);
                final RecordID rid;
                if (data.isPresent()) {
                    rid = data.get().getRecordID();
                    assertEquals(rid, map.get(key));
                }
            } else if (nextOp < 3000) {
                // delete key,rid pair from map
                if (map.remove(key) != null) {
                    // delete also from index
                    assertTrue(index.remove(key));
                } else {
                    assertFalse(index.remove(key));
                }
            } else if (nextOp == 3000) {
                // check iterator, 1/10000 as likely as the other operations
                try (IndexScan scan = index.openScan()) {
                    int size = 0;
                    while (scan.hasNext()) {
                        final IndexEntry entry = scan.next();
                        assertEquals(entry.getRecordID(), map.get(entry.getKey()));
                        size++;
                    }
                    assertEquals(map.size(), size);
                }
            } else if (nextOp == 3001) {
                // check if the index is still consistent
                index.checkInvariants();
            } else {
                // close and re-open the index
                index.close();
                index = BTreeIndex.openIndex(this.getBufferManager(), fileName);
            }

            assertEquals(Integer.toString(i), map.size(), index.size());

            // test pin counts
            assertEquals("Pin count differs in: iteration i" + i + ", op: " + nextOp + ", key: " + key,
                    oldCount, this.getBufferManager().getNumPinned());
        }
        index.delete();

        assertEquals(pages, bufferManager.getDiskManager().getAllocCount());
    }

    /**
     * Tests filling and emptying the index.
     *
     * @throws IOException if the index could not be closed
     */
    @Test
    public void testFillEmpty() throws IOException {

        this.initRandom();
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), "fill_empty")) {

            final int n = 100_000;
            final BitSet success = new BitSet();
            for (int i = 0; i < n; i++) {
                if (i % 10000 == 9999) {
                    index.checkInvariants();
                }
                final int key = this.getRandom().nextInt(n);
                if (index.search(key).isEmpty()) {
                    success.set(i);
                }
                final RecordID rid = newRecordID(i);
                index.insert(key, rid);
                final Optional<IndexEntry> entry = index.search(key);
                assertTrue("Entry #" + i + " was not found.", entry.isPresent());
            }
            assertEquals(success.cardinality(), index.size());
            index.checkInvariants();
            this.initRandom();
            for (int i = 0; i < n; i++) {
                final int key = this.getRandom().nextInt(n);
                if (i % 10000 == 9999) {
                    index.checkInvariants();
                }
                assertEquals("Entry #" + i + " is not in the tree prior to its attempted deletion.",
                        success.get(i), index.search(key).isPresent());
                assertEquals("Entry #" + i + " could not be deleted.", success.get(i), index.remove(key));
            }

            index.delete();
        }
    }

    @Test
    public void lookupTest() throws IOException {
        this.withExistingDBFile("LookupTest", INDEX_FILE, (index, entries) -> {
            final int n = index.size();
            final Random rng = this.getRandom();
            for (int i = 0; i < 5 * n; i++) {
                final int key = rng.nextInt(10 * n);
                assertEquals(entries.get(key), index.search(key).map(IndexEntry::getRecordID).orElse(null));
            }
        });
    }

    @Test
    public void deleteTest() throws IOException {
        this.withExistingDBFile("DeleteTest", INDEX_FILE, (index, entries) -> {
            final int n = index.size();
            final Random rng = this.getRandom();
            for (int i = 0; i < 5 * n; i++) {
                final int key = rng.nextInt(10 * n);
                final int pinned = index.getBufferManager().getNumPinned();
                assertEquals("Error removing key " + key, entries.remove(key) != null, index.remove(key));
                assertEquals("Iteration " + i, pinned, index.getBufferManager().getNumPinned());
                if (i % 10000 == 9999) {
                    try {
                        index.checkInvariants();
                    } catch (final AssertionError e) {
                        throw new AssertionError("Iteration " + i, e);
                    }
                }
            }
            assertEquals(entries.size(), index.size());

            final List<Integer> keys = new ArrayList<>(entries.keySet());
            Collections.shuffle(keys);
            for (final int key : keys) {
                assertTrue(index.remove(key));
                entries.remove(key);
                assertEquals(entries.size(), index.size());
            }
            System.out.println(index.size());
            index.checkInvariants();
        });
    }

    @Test
    public void deleteSearchTest() throws IOException {
        this.withExistingDBFile("DeleteTest", INDEX_FILE, (index, entries) -> {
            final int n = index.size();
            final Random rng = this.getRandom();
            for (int i = 0; i < 5 * n; i++) {
                final int key = rng.nextInt(10 * n);
                final int pinned = index.getBufferManager().getNumPinned();
                assertEquals(entries.get(key), index.search(key).map(IndexEntry::getRecordID).orElse(null));
                assertEquals("Error removing key " + key, entries.remove(key) != null, index.remove(key));
                assertEquals("Iteration " + i, pinned, index.getBufferManager().getNumPinned());
                if (i % 10000 == 9999) {
                    index.checkInvariants();
                }
            }
            assertEquals(entries.size(), index.size());

            final List<Integer> keys = new ArrayList<>(entries.keySet());
            Collections.shuffle(keys);
            for (final int key : keys) {
                assertEquals(entries.get(key), index.search(key).get().getRecordID());
                assertTrue(index.remove(key));
                entries.remove(key);
                assertEquals(entries.size(), index.size());
            }
            System.out.println(index.size());
            index.checkInvariants();
        });
    }

    private void withExistingDBFile(final String name, final Path file,
                                    final BiConsumer<BTreeIndex, Map<Integer, RecordID>> func) throws IOException {
        final Path dbFile = Files.createTempFile(name, ".minibase");
        try {
            try (GZIPInputStream in = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
                Files.copy(in, dbFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (DiskManager diskManager = DiskManager.open(DiskFile.open(dbFile.toFile()))) {
                final BufferManager bufferManager = new BufferManagerImpl(diskManager, BUF_SIZE, BUF_POLICY);
                try (BTreeIndex index = BTreeIndex.openIndex(bufferManager, INDEX_NAME)) {
                    final Map<Integer, RecordID> entries = new HashMap<>();
                    try (IndexScan scan = index.openScan()) {
                        while (scan.hasNext()) {
                            final IndexEntry entry = scan.next();
                            entries.put(entry.getKey(), entry.getRecordID());
                        }
                    }

                    func.accept(index, entries);
                }
                diskManager.destroy();
            }
        } finally {
            Files.deleteIfExists(dbFile);
        }
    }

    /**
     * Index package error cases.
     *
     * @throws IOException if the storage layer has problems
     */
    @Test
    public void errorConditions() throws IOException {
        this.initRandom();

        // creating temporary index...
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {

            // deleting invalid entry...
            final int key = this.getRandom().nextInt(1000);
            assertFalse(index.remove(key));

            // next in completed scan...
            try (IndexScan scan = index.openScan(key)) {
                assertFalse(scan.hasNext());
                try {
                    scan.next();
                    fail();
                } catch (final NoSuchElementException exc) {
                    // expected
                }
            }

            // deleting empty index...
            index.delete();
        }
    }

    private BitSet drawRandom(final int domain, final int numKeys) {
        final Random rng = this.getRandom();
        final BitSet bs = new BitSet(domain);
        for (int i = 0; i < numKeys; i++) {
            int nextKey;
            do {
                nextKey = rng.nextInt(domain);
            } while (bs.get(nextKey));
            bs.set(nextKey);
        }
        return bs;
    }

    @Test
    public void testFindKeyBranch() {
        final int n = 10_000;
        final int numKeys = this.getRandom().nextInt(BTreeBranch.MIN_KEYS + 1) + BTreeBranch.MIN_KEYS;
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
            final BitSet bs = this.drawRandom(n, numKeys);

            final Page<BTreeBranch> branch = BTreeBranch.newPage(this.getBufferManager());
            BTreeBranch.setChildID(branch, 0, PageID.getInstance(n));
            for (int i = 0, k = bs.nextSetBit(0); k >= 0; k = bs.nextSetBit(k + 1), i++) {
                BTreeBranch.setKey(branch, i, k);
                BTreeBranch.setChildID(branch, i, PageID.getInstance(n + i));
            }
            BTreeBranch.setNumKeys(branch, numKeys);
            int nextKey = 0;
            for (int i = -10; i < n; i++) {
                final int pos = index.findKey(branch, i, false);
                if (i >= 0 && bs.get(i)) {
                    assertEquals(nextKey, pos);
                    nextKey++;
                } else {
                    assertEquals(-(nextKey + 1), pos);
                }
            }

            this.getBufferManager().freePage(branch);
            index.delete();
        }
    }

    @Test
    public void testFindKeyLeaf() {
        final int n = 10_000;
        final int numKeys = this.getRandom().nextInt(BTreeLeaf.MIN_KEYS + 1) + BTreeLeaf.MIN_KEYS;
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
            final BitSet bs = this.drawRandom(n, numKeys);

            final Page<BTreeLeaf> leaf = BTreeLeaf.newPage(this.getBufferManager(), PageID.INVALID, PageID.INVALID);
            BTreeLeaf.setRecordID(leaf, 0, newRecordID(n));
            for (int i = 0, k = bs.nextSetBit(0); k >= 0; k = bs.nextSetBit(k + 1), i++) {
                BTreeLeaf.setKey(leaf, i, k);
                BTreeLeaf.setRecordID(leaf, i, newRecordID(n + i));
            }
            BTreeLeaf.setNumKeys(leaf, numKeys);
            int nextKey = 0;
            for (int i = -10; i < n; i++) {
                final int pos = index.findKey(leaf, i, true);
                if (i >= 0 && bs.get(i)) {
                    assertEquals(nextKey, pos);
                    nextKey++;
                } else {
                    assertEquals(-(nextKey + 1), pos);
                }
            }

            this.getBufferManager().freePage(leaf);
            index.delete();
        }
    }

    /**
     * Does random inserts and deletes and checks if the index leaves something pinned at the end.
     *
     * @throws IOException exception of index close
     */
    @Test
    public void testPincountsInsertDelete() throws IOException {
        // Do some random operations on the index
        final String fileName = "testPincountsInsertDelete";
        final BufferManager bufferManager = this.getBufferManager();
        final DiskManager diskManager = bufferManager.getDiskManager();

        final int allocStart = diskManager.getAllocCount();
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), fileName)) {

            final int pinned = bufferManager.getNumPinned();
            final int alloc = diskManager.getAllocCount();

            final Map<Integer, RecordID> map = new HashMap<>();
            final Random rand = new Random(42);
            for (int i = 0; i < 100_000; ++i) {
                final int key = rand.nextInt(10000);
                final RecordID rid = newRecordID(key);
                final int pinnedBeforeInsert = bufferManager.getNumPinned();
                index.insert(key, rid);
                assertEquals("Iteration " + i, pinnedBeforeInsert, bufferManager.getNumPinned());
                map.put(key, rid);
            }
            // PinCount should stay the same after all operations
            assertEquals(pinned, bufferManager.getNumPinned());

            int rem = 0;
            for (final Map.Entry<Integer, RecordID> e : map.entrySet()) {
                final int pinnedBefore = bufferManager.getNumPinned();
                index.remove(e.getKey());
                assertEquals("Remove op " + rem, pinnedBefore, bufferManager.getNumPinned());
                rem++;
            }

            assertEquals("Not all pages are unpinned after all operations.", pinned,
                    bufferManager.getNumPinned());

            assertEquals("Not all pages were removed by delete.", alloc, diskManager.getAllocCount());

            index.delete();
            assertEquals("The index did not delete itself properly.", allocStart, diskManager.getAllocCount());
        }
    }

    /**
     * Tests reopening of the index.
     *
     * @throws Exception exception
     */
    @Test
    public void testReopen() throws Exception {
        // Do some random operations on the index
        final String fileName = "reopenTest";
        final Map<Integer, List<RecordID>> map = new HashMap<>();
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), fileName)) {
            final Random rand = new Random(42);
            for (int i = 0; i < 10000; ++i) {
                final int key = rand.nextInt(10000);
                final RecordID rid = newRecordID(key);
                index.insert(key, rid);
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(rid);
            }
        }
        // Reopen index
        try (BTreeIndex index = BTreeIndex.openIndex(this.getBufferManager(), fileName)) {
            // Simply look for one unspecific entry
            index.search(map.keySet().iterator().next());
            index.delete();
        }
    }

    /**
     * Tests the scan of the whole index.
     *
     * @throws IOException exception
     */
    @Test
    public void scanIndex() throws IOException {
        final int size = 600;
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
            final BitSet bs = new BitSet(size);
            for (int i = 0; i < size; i++) {
                final int key = i;
                final RecordID rid = newRecordID(i);
                index.insert(key, rid);
                bs.set(i);
            }
            try (IndexScan scan = index.openScan()) {
                while (scan.hasNext()) {
                    final IndexEntry e = scan.next();
                    final RecordID rid = e.getRecordID();
                    assertTrue(bs.get(rid.getSlotNo()));
                    bs.clear(rid.getSlotNo());
                }
            }
            assertTrue(bs.isEmpty());
            index.delete();
        }
    }

    /**
     * A special case that does not seem to be covered by the random tests: The newly inserted
     * entry gets shifted to the previous page and the middle key has to be updated.
     */
    @Test
    public void reinsertFirst() {
        final BufferManager bufferManager = this.getBufferManager();
        try (BTreeIndex tree = BTreeIndex.createIndex(bufferManager, INDEX_NAME)) {
            // fills two pages completely
            for (int i = 0; i < 2 * BTreeLeaf.MAX_KEYS; i++) {
                // leave a gap in the middle
                final int key = i < BTreeLeaf.MAX_KEYS ? i : i + 1;
                tree.insert(key, newRecordID(key));
            }
            tree.remove(BTreeLeaf.MAX_KEYS - 1);
            tree.remove(BTreeLeaf.MAX_KEYS + 1);
            tree.insert(2 * BTreeLeaf.MAX_KEYS + 1, newRecordID(2 * BTreeLeaf.MAX_KEYS + 1));
            tree.insert(BTreeLeaf.MAX_KEYS + 1, newRecordID(BTreeLeaf.MAX_KEYS + 1));
            tree.checkInvariants();
            tree.delete();
        }
    }

    /**
     * @param n number of keys to insert into the tree
     */
    private void fill(final BTreeIndex index, final int n) {
        this.initRandom();
        final int[] vals = IntStream.range(1, n + 1).toArray();
        for (int i = 1; i < vals.length; ++i) {
            final int j = this.getRandom().nextInt(i + 1);
            if (i != j) {
                final int tmp = vals[i];
                vals[i] = vals[j];
                vals[j] = tmp;
            }
        }
        for (int i = 0; i < vals.length; ++i) {
            final int next = vals[i];
            index.insert(next, newRecordID(next));
            if (i % 10000 == 9999) {
                index.checkInvariants();
            }
            final int fi = i;
            final int nxt = next;
            Arrays.stream(vals, 0, i + 1).forEach(val -> {
                assertTrue("Value " + val + " not present in tree after inserting " + nxt + ", iteration " + fi,
                        index.search(val).isPresent());
            });
        }
        assertEquals(vals.length, index.size());
    }

    /**
     * Tests filling the tree up until only one leaf is full.
     */
    @Test
    public void testFillLeaf() {
        // fill one leaf
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
            this.fill(index, BTreeLeaf.MAX_KEYS);
            index.delete();
        }
    }

    /**
     * Fills one leaf and forces a leaf split.
     */
    @Test
    public void testSplitLeaf() {
        // fill one leaf, forcing one split
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
            this.fill(index, BTreeLeaf.MAX_KEYS + 1);
            index.delete();
        }
    }

    /**
     * Force repeated leaf splits.
     */
    @Test
    public void testSplitLeaves() {
        for (int i = 1; i <= (BTreeBranch.MAX_KEYS + 1); ++i) {
            try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
                this.fill(index, BTreeLeaf.MAX_KEYS * i);
                index.delete();
            }
        }
    }

    /**
     * Fills a whole branch.
     */
    @Test
    public void testFillBranch() {
        // fill as many leaves as can be reference by one branch node
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
            this.fill(index, BTreeBranch.MAX_KEYS * BTreeLeaf.MAX_KEYS);
            index.delete();
        }
    }

    /**
     * Forces a branch split.
     */
    @Test
    public void testSplitBranch() {
        // "fill" a whole branch node and insert one additional entry, forcing a branch split
        try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
            this.fill(index, BTreeBranch.MAX_KEYS * BTreeLeaf.MAX_KEYS + 1);
            index.delete();
        }
    }

    /**
     * Splits branches repeatedly.
     */
    @Test
    public void testSplitBranches() {
        for (int i = 1; i <= (BTreeBranch.MAX_KEYS + 1); ++i) {
            try (BTreeIndex index = BTreeIndex.createIndex(this.getBufferManager(), null)) {
                this.fill(index, BTreeBranch.MAX_KEYS * BTreeLeaf.MAX_KEYS * i);
                index.delete();
            }
        }
    }
}
