package minibase.storage.buffer.policy;

import java.util.BitSet;

/**
 * Implementation of the Clock/Second-Chance Replacement Policy.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 */
public class ClockPolicy implements ReplacementPolicy {

    /** Cached next victim, {@code -1} if not set. */
    private int nextVictim;

    /** Number of unpinned pages. */
    private int numPinned;

    /** Number of available buffers. */
    private final int numBuffers;

    /**
     * Set of two boolean flags for each buffer.
     * The "pinned" flags are at even positions, the "visited" flags at odd positions.
     */
    private final BitSet flags;

    /** The clock's hand. */
    private int hand = 0;

    /**
     * Constructs a Clock policy managing the given number of buffers.
     *
     * @param numBuffers number of buffers to manage, all assumed to be unpinned at the beginning
     */
    public ClockPolicy(final int numBuffers) {
        this.flags = new BitSet(2 * numBuffers);
        this.numBuffers = numBuffers;
        this.numPinned = 0;
        this.nextVictim = -1;
    }

    /**
     * Returns the position of the given buffer's {@code pinned} flag in {@link #flags}.
     *
     * @param pos index of the buffer
     * @return position of the buffer's {@code pinned} flag
     */
    private static int pinned(final int pos) {
        return 2 * pos;
    }

    /**
     * Returns the position of the given buffer's {@code visited} flag in {@link #flags}.
     *
     * @param pos index of the buffer
     * @return position of the buffer's {@code visited} flag
     */
    private static int extraLife(final int pos) {
        return 2 * pos + 1;
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
        if (numPinned == numBuffers) {
            return -1;
        }
        while (nextVictim == -1) {
            if (this.flags.get(pinned(this.hand))) {
                // pinned page, skip
            } else if (this.flags.get(extraLife(this.hand))) {
                // page is referenced
                this.flags.clear(extraLife(this.hand));
            } else {
                // unpinned, unreferenced page
                nextVictim = this.hand;
            }
            this.hand = this.hand == numBuffers - 1 ? 0 : this.hand + 1;
        }
        return nextVictim;
    }
}
