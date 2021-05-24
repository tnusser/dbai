/*
 * @(#)MRUPolicyGroup99.java   1.0   Oct 11, 2013
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

public class MRUPolicy implements ReplacementPolicy {

    /**
     * stack.
     */
    private final Deque<Integer> stack;

    public MRUPolicy(final int numBuffers) {
        this.stack = new ArrayDeque<>();
    }

    @Override
    public void stateChanged(final int pos, final PageState newState) {
        if (newState == PageState.UNPINNED) {
            this.stack.push(pos);
        } else {
            this.stack.removeFirstOccurrence(pos);
        }
    }

    @Override
    public int pickVictim() {
        return this.stack.isEmpty() ? -1 : this.stack.peek();
    }
}
