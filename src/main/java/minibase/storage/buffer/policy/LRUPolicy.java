package minibase.storage.buffer.policy;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Implementation of the Least-Recently-Used Replacement Policy.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 */
public class LRUPolicy implements ReplacementPolicy {

    /** Queue of currently unpinned buffers. */
    private final Deque<Integer> queue;

    /**
     * Constructs a LRU policy managing the given number of buffers.
     *
     * @param numBuffers number of buffers to manage, all assumed to be free at the beginning
     */
    public LRUPolicy(final int numBuffers) {
        // we don't add buffers initially because free ones are managed by the buffer manager
        this.queue = new ArrayDeque<>();
    }

    @Override
    public void stateChanged(final int pos, final PageState newState) {
        switch (newState) {
            case PINNED:
                // no duplicates, so we can stop after the first hit
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
