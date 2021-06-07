/*
 * @(#)RunBuilder.java   1.0   Jan 12, 2017
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.file;

import minibase.storage.buffer.BufferManager;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;
import minibase.storage.buffer.UnpinMode;

/**
 * A builder for {@link Run}s of same-size records.
 * The builder cannot be reused and only releases its resources when {@link #finish()} is called.
 *
 * @author #TODO
 */
public final class RunBuilder {

   public RunBuilder(final BufferManager bufferManager, final int recordLength) {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }

   public void appendRecord(final byte[] record) {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }

   public Run finish() {
      //TODO implement this
      throw new UnsupportedOperationException("not yet implemented");
   }
}
