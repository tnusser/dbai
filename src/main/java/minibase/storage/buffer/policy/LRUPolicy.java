/*
 * @(#)LRUPolicyGroup99.java   1.0   Oct 11, 2013
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.storage.buffer.policy;

import java.util.ArrayDeque;
import java.util.Deque;

public class LRUPolicy implements ReplacementPolicy {


    /**
     * queue.
     */
    private final Deque<Integer> queue;

    public LRUPolicy(final int numBuffers) {
        this.queue = new ArrayDeque<>();
    }

    @Override
    public void stateChanged(final int pos, final PageState newState) {
        switch (newState) {
            case PINNED:
                this.queue.removeFirstOccurrence(pos);
                break;
            case UNPINNED:
                this.queue.add(pos);
                break;
            default:
        }
    }

    @Override
    public int pickVictim() {
        return this.queue.isEmpty() ? -1 : this.queue.peek();
    }
}
