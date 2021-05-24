/*
 * @(#)ReplacementStrategy.java   1.0   Oct 30, 2013
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.storage.buffer;

import minibase.storage.buffer.policy.*;

/**
 * Enumeration of the different buffer replacement policies supported by this factory.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 * @version 1.0
 */
public enum ReplacementStrategy {
    /**
     * Random strategy.
     */
    RANDOM() {
        @Override
        ReplacementPolicy newInstance(final int numBuffers) {
            return new RandomPolicy(numBuffers);
        }
    },

    /**
     * Least recently used strategy.
     */
    LRU() {
        @Override
        ReplacementPolicy newInstance(final int numBuffers) {
            return new LRUPolicy(numBuffers);
        }
    },

    /**
     * Most recently used strategy.
     */
    MRU() {
        @Override
        ReplacementPolicy newInstance(final int numBuffers) {
            return new MRUPolicy(numBuffers);
        }
    },

    /**
     * Clock strategy.
     */
    CLOCK() {
        @Override
        ReplacementPolicy newInstance(final int numBuffers) {
            return new ClockPolicy(numBuffers);
        }
    };

    /**
     * Factory method that creates a new buffer replacement policy for the given number of
     * buffers.
     *
     * @param numBuffers number of buffers
     * @return the buffer replacement policy
     */
    abstract ReplacementPolicy newInstance(int numBuffers);
}
