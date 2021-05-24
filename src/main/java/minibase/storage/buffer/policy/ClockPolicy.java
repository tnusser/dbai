/*
 * @(#)ClockPolicyGroup99.java   1.0   Aug 2, 2006
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.storage.buffer.policy;

import java.util.BitSet;

public class ClockPolicy implements ReplacementPolicy {


    /**
     * Number of available buffers.
     */
    private final int numBuffers;
    /**
     * flags.
     */
    private final BitSet flags;
    /**
     * Next Victim to be evicted.
     */
    private int nextVictim;
    /**
     * Number of unpinned pages.
     */
    private int numPinned;
    /**
     * hand.
     */
    private int hand = 0;

    public ClockPolicy(final int numBuffers) {
        this.flags = new BitSet(2 * numBuffers);
        this.numBuffers = numBuffers;
        this.numPinned = 0;
        this.nextVictim = -1;
    }

    @Override
    public void stateChanged(final int pos, final PageState newState) {
        if (newState == PageState.PINNED) {
            this.nextVictim = -1;
            this.numPinned++;
            this.flags.set(pinned(pos));
            this.flags.clear(extraLife(pos));
        } else {
            this.numPinned--;
            this.flags.clear(pinned(pos));
            this.flags.set(extraLife(pos));
        }
    }

    @Override
    public int pickVictim() {
        if (nextVictim != -1) {
            return nextVictim;
        }
        if (numPinned == numBuffers) {
            return -1;
        }

        while (true) {
            final int pos = this.hand;
            if (this.flags.get(pinned(pos))) {
                // pinned page, skip
            } else if (this.flags.get(extraLife(pos))) {
                // page is referenced
                this.flags.clear(extraLife(pos));
            } else {
                // unpinned, unreferenced page
                nextVictim = pos;
                return pos;
            }
            this.hand = pos == numBuffers - 1 ? 0 : pos + 1;
        }
    }

    private int pinned(final int pos) {
        return 2 * pos;
    }

    private int extraLife(final int pos) {
        return 2 * pos + 1;
    }
}
