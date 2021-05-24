/*
 * @(#)FileScan.java   1.0   Jun 12, 2016
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2016 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.file;

import minibase.RecordID;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Common interface for scans over files.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public interface FileScan extends Iterator<byte[]>, AutoCloseable {
    /**
     * The empty file scan.
     */
    FileScan EMPTY = new FileScan() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public byte[] next() {
            throw new NoSuchElementException("Empty scan.");
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }

        @Override
        public RecordID lastID() {
            throw new IllegalStateException("Empty scan.");
        }

        @Override
        public void updateLast(final byte[] newRecord) {
            throw new IllegalStateException("Empty scan.");
        }
    };

    /**
     * Returns {@code true} if there are more tuples and {@code false} otherwise.
     *
     * @return {@code true} if there are more tuples available, {@code false} otherwise
     */
    @Override
    boolean hasNext();

    /**
     * Computes and returns the next tuple produced by this iterator. If there are no more tuples to return,
     * this method will throw a {@code NoSuchElementException}.
     *
     * @return next tuple
     * @throws NoSuchElementException if there are no more tuples to return
     */
    @Override
    byte[] next();

    /**
     * Resets this iterator to start at the first tuple again.
     */
    void reset();

    /**
     * Returns the ID of the last returned record.
     *
     * @return last record's ID
     * @throws IllegalStateException if no record has been returned yet (or since the last restart)
     */
    RecordID lastID();

    /**
     * Overwrites the last returned record with the given data.
     *
     * @param newRecord new record data
     * @throws IllegalStateException    if no record was returned since the last reset
     * @throws IllegalArgumentException if the length of {@code newRecord} does not fit the records of the file
     */
    void updateLast(byte[] newRecord);

    /**
     * Closes the iterator and releases all allocated resources, e.g., pinned pages.
     */
    @Override
    void close();
}
