/*
 * @(#)RunPage.java   1.0   Jan 12, 2017
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.access.file;

import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;
import minibase.storage.buffer.PageType;
import minibase.storage.file.DiskManager;

/**
 * Page type for the pages of a {@link Run} file.
 *
 * @author #TODO
 */
public final class RunPage implements PageType {

   /** Default constructor, hidden because this is a utility class. */
   private RunPage() {
      throw new AssertionError();
   }

}
