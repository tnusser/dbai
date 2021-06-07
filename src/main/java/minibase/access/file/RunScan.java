/*
 * @(#)RunScan.java   1.0   Jan 12, 2017
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.file;

import java.util.NoSuchElementException;

import minibase.query.evaluator.TupleIterator;
import minibase.storage.buffer.BufferManager;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;
import minibase.storage.buffer.UnpinMode;

/**
 * A sequential scan over a {@link Run}.
 *
 * @author #TODO
 */
public class RunScan implements TupleIterator {

   public RunScan(final BufferManager bufferManager, final Run run, final int recordLength) {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }

   @Override
   public boolean hasNext() {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }

   @Override
   public byte[] next() {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }

   @Override
   public void reset() {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }

   @Override
   public void close() {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }
}
