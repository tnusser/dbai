/*
 * @(#)ClockPolicy.java   1.0   Aug 2, 2006
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.storage.buffer.policy;

public class ClockPolicy implements ReplacementPolicy {

    public ClockPolicy(final int numBuffers) {
        // TODO implement constructor
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void stateChanged(final int pos, final PageState newState) {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int pickVictim() {
        // TODO implement method
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
