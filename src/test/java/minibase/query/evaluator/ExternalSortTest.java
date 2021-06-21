/*
 * @(#)ExternalSortTest.java   1.0   Jan 12, 2017
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.query.evaluator;

import java.util.*;
import java.util.stream.Collectors;

import minibase.access.file.Run;
import minibase.access.file.RunScan;
import minibase.catalog.DataType;
import org.junit.Test;

import minibase.BaseTest;
import minibase.access.file.FileScan;
import minibase.access.file.HeapFile;
import minibase.query.evaluator.compare.RecordComparator;
import minibase.query.evaluator.compare.TupleComparator;
import minibase.query.schema.Schema;
import minibase.query.schema.SchemaBuilder;
import minibase.storage.buffer.PageID;
import minibase.util.Convert;

import static org.junit.Assert.*;

public class ExternalSortTest extends BaseTest {
    /**
     * Test Schema.
     */
    private static final Schema DATA_SCHEMA =
            new SchemaBuilder()
                    .addField("x", DataType.BIGINT, 8)
                    .addField("y", DataType.INT, 4)
                    .addField("z", DataType.CHAR, 12)
                    .build();

    /**
     * Smaller test Schema.
     */
    private static final Schema SMALL_SCHEMA =
            new SchemaBuilder()
                    .addField("x", DataType.INT, 4)
                    .build();

    private int[] fillHeapFile(final HeapFile heapFile, final int minIncl, final int maxExcl, final int tuples) {
        final Random rng = this.getRandom();
        final int n = maxExcl - minIncl;
        final int[] counts = new int[n];
        for (int i = 0; i < tuples; i++) {
            final int r = rng.nextInt(n);
            counts[r]++;
            final int val = minIncl + r;
            final byte[] record = DATA_SCHEMA.newTuple();
            DATA_SCHEMA.setAllFields(record, (long) val, -(val % 1000), String.valueOf(val));
            heapFile.insertRecord(record);
        }
        return counts;
    }

    @Test
    public void initialRunsTest() {
        try (HeapFile file = HeapFile.createTemporary(this.getBufferManager())) {
            final int[] counts = this.fillHeapFile(file, -100000, 100000, 150000);
            final RecordComparator comp = new TupleComparator(DATA_SCHEMA, 1, 0);
            final ExternalSort sort = new ExternalSort(this.getBufferManager(), new TableScan(DATA_SCHEMA, file), comp);
            final Run initialRuns;
            try (FileScan scan = file.openScan()) {
                initialRuns = sort.initialRuns(scan);
            }

            final int[] newCounts = new int[counts.length];
            try (RunScan scan = new RunScan(this.getBufferManager(), initialRuns,
                    ExternalSort.RUN_OF_RUNS.getLength())) {
                while (scan.hasNext()) {
                    final byte[] runRecord = scan.next();
                    final PageID firstID = PageID.getInstance(ExternalSort.RUN_OF_RUNS.getIntField(runRecord, 0));
                    final long length = ExternalSort.RUN_OF_RUNS.getBigintField(runRecord, 1);
                    final Run run = new Run(firstID, length);
                    try (RunScan scan2 = new RunScan(this.getBufferManager(), run, DATA_SCHEMA.getLength())) {
                        byte[] previous = null;
                        while (scan2.hasNext()) {
                            final byte[] record = scan2.next();
                            final int val = (int) DATA_SCHEMA.getBigintField(record, 0);
                            final byte[] reference = DATA_SCHEMA.newTuple();
                            DATA_SCHEMA.setAllFields(reference, (long) val, -(val % 1000), String.valueOf(val));
                            assertArrayEquals("Record structure has not been preserved.", reference, record);
                            assertTrue(Arrays.toString(previous) + " > " + Arrays.toString(record),
                                    previous == null || comp.compare(previous, record) <= 0);
                            newCounts[val + 100000]++;
                            previous = record;
                        }
                    }
                }
            }
            assertArrayEquals(counts, newCounts);
        }
    }

    @Test
    public void test() {
        final byte[] input = new byte[2000];
        final int[] counts = new int[256];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (this.getRandom().nextInt(256) - 128);
            counts[input[i] + 128]++;
        }

        final Operator values = new Operator() {
            @Override
            public TupleIterator open() {
                return new TupleIterator() {
                    private int pos;

                    @Override
                    public void reset() {
                        this.pos = 0;
                    }

                    @Override
                    public byte[] next() {
                        return new byte[]{0, 0, 0, input[this.pos++]};
                    }

                    @Override
                    public boolean hasNext() {
                        return this.pos < input.length;
                    }

                    @Override
                    public void close() {
                    }
                };
            }

            @Override
            public Schema getSchema() {
                return SMALL_SCHEMA;
            }
        };

        final RecordComparator comp = new TupleComparator(SMALL_SCHEMA, 0);
        final Run initialRuns;
        try (TupleIterator iter = values.open()) {
            initialRuns = new ReplacementSorter(
                    this.getBufferManager(), 10, comp, SMALL_SCHEMA.getLength()).initialRuns(iter);
        }

        final int[] newCounts = new int[counts.length];
        try (RunScan scan = new RunScan(this.getBufferManager(), initialRuns,
                ExternalSort.RUN_OF_RUNS.getLength())) {
            while (scan.hasNext()) {
                final byte[] runRecord = scan.next();
                final PageID firstID = PageID.getInstance(ExternalSort.RUN_OF_RUNS.getIntField(runRecord, 0));
                final long length = ExternalSort.RUN_OF_RUNS.getBigintField(runRecord, 1);
                final Run run = new Run(firstID, length);
                try (RunScan scan2 = new RunScan(this.getBufferManager(), run, SMALL_SCHEMA.getLength())) {
                    int previous = Integer.MIN_VALUE;
                    while (scan2.hasNext()) {
                        final byte[] record = scan2.next();
                        final int val = SMALL_SCHEMA.getIntField(record, 0);
                        final byte[] reference = SMALL_SCHEMA.newTuple();
                        SMALL_SCHEMA.setIntField(reference, 0, val);
                        assertArrayEquals("Record structure has not been preserved.", reference, record);
                        assertTrue(previous + " > " + val, previous <= val);
                        newCounts[((byte) val) + 128]++;
                        previous = val;
                    }
                }
            }
        }
        assertArrayEquals(counts, newCounts);
    }

    @Test
    public void testOther() {
        final int size = 1000;
        final Operator values = new Operator() {
            @Override
            public TupleIterator open() {
                return new TupleIterator() {
                    private int pos;

                    @Override
                    public void reset() {
                        this.pos = 0;
                    }

                    @Override
                    public byte[] next() {
                        final int val = this.pos++;
                        final byte[] record = DATA_SCHEMA.newTuple();
                        DATA_SCHEMA.setAllFields(record, (long) val, val % 2, "");
                        return record;
                    }

                    @Override
                    public boolean hasNext() {
                        return this.pos < size;
                    }

                    @Override
                    public void close() {
                    }
                };
            }

            @Override
            public Schema getSchema() {
                return DATA_SCHEMA;
            }
        };

        final RecordComparator comp = new TupleComparator(DATA_SCHEMA, 1);
        final ExternalSort sort = new ExternalSort(this.getBufferManager(), values, comp);
        final Run initialRuns;
        try (TupleIterator iter = values.open()) {
            initialRuns = sort.initialRuns(iter);
        }

        final Run merged = sort.mergePhase(initialRuns);

        final BitSet seenIDs = new BitSet();
        try (RunScan scan2 = new RunScan(this.getBufferManager(), merged, DATA_SCHEMA.getLength())) {
            int i = 0;
            while (scan2.hasNext() && i < size) {
                final int section = i / 500;
                final byte[] record = scan2.next();
                final int rowIdx = Math.toIntExact(DATA_SCHEMA.getBigintField(record, 0));
                seenIDs.set(rowIdx);
                // is the row index correct?
                assertEquals("Iteration " + i, section, rowIdx % 2);
                // is the sort key correct?
                assertEquals("Iteration " + i, section, DATA_SCHEMA.getIntField(record, 1));
                i++;
            }
            // is the number of records correct?
            assertEquals(size, i);
            // have there been no duplicates?
            assertEquals(size, seenIDs.nextClearBit(0));
        }
    }

    @Test
    public void mergePhaseTest() {
        try (HeapFile file = HeapFile.createTemporary(this.getBufferManager())) {
            final int[] counts = this.fillHeapFile(file, -100000, 100000, 150000);
            final RecordComparator comp = new TupleComparator(DATA_SCHEMA, 1, 0);
            final ExternalSort sort = new ExternalSort(this.getBufferManager(), new TableScan(DATA_SCHEMA, file), comp);
            final Run initialRuns;
            try (FileScan scan = file.openScan()) {
                initialRuns = sort.initialRuns(scan);
            }

            final Run merged = sort.mergePhase(initialRuns);

            final int[] newCounts = new int[counts.length];
            try (RunScan scan = new RunScan(this.getBufferManager(), merged, DATA_SCHEMA.getLength())) {
                byte[] previous = null;
                while (scan.hasNext()) {
                    final byte[] record = scan.next();
                    final int val = (int) DATA_SCHEMA.getBigintField(record, 0);
                    final byte[] reference = DATA_SCHEMA.newTuple();
                    DATA_SCHEMA.setAllFields(reference, (long) val, -(val % 1000), String.valueOf(val));
                    assertArrayEquals("Record structure has not been preserved.", reference, record);
                    assertTrue(Arrays.toString(previous) + " > " + Arrays.toString(record),
                            previous == null || comp.compare(previous, record) <= 0);
                    newCounts[val + 100000]++;
                    previous = record;
                }
            }
            assertArrayEquals(counts, newCounts);
        }
    }

    @Test
    public void treeOfLosersTest() {
        final int n = 10;
        final Operator[] inputs = new Operator[n];
        final List<Integer> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            final int[] vals = new int[i];
            for (int j = 0; j < i; j++) {
                vals[j] = 100 * j + i;
                results.add(vals[j]);
            }
            inputs[i] = this.values(vals);
        }
        final List<Operator> list = Arrays.asList(inputs);
        Collections.shuffle(list, this.getRandom());
        final TupleIterator[] iters = list.stream().map(Operator::open)
                .collect(Collectors.toList()).toArray(new TupleIterator[n]);

        Collections.sort(results);
        final Iterator<Integer> reference = results.iterator();
        try (TreeOfLosers tree = new TreeOfLosers(new TupleComparator(SMALL_SCHEMA, 0), iters)) {
            while (tree.hasNext()) {
                assertTrue(reference.hasNext());
                assertEquals((int) reference.next(), Convert.readInt(tree.next(), 0));
            }
            assertFalse(reference.hasNext());
        }
    }

    private Operator values(final int[] values) {
        return new Operator() {
            @Override
            public TupleIterator open() {
                return new TupleIterator() {
                    private int pos;

                    @Override
                    public void reset() {
                        this.pos = 0;
                    }

                    @Override
                    public byte[] next() {
                        final byte[] next = new byte[4];
                        Convert.writeInt(next, 0, values[this.pos++]);
                        return next;
                    }

                    @Override
                    public boolean hasNext() {
                        return this.pos < values.length;
                    }

                    @Override
                    public void close() {
                    }
                };
            }

            @Override
            public Schema getSchema() {
                return SMALL_SCHEMA;
            }
        };
    }
}
