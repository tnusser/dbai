package minibase.storage.buffer.policy;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Implementation of the Most-Recently-Used Replacement Policy.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 */
public class MRUPolicy implements ReplacementPolicy {

    /** Stack of currently unpinned buffers. */
    private final Deque<Integer> stack;

    /**
     * Constructs a MRU policy managing the given number of buffers.
     *
     * @param numBuffers number of buffers to manage, all assumed to be free at the beginning
     */
    public MRUPolicy(final int numBuffers) {
        // we don't add buffers initially because free ones are managed by the buffer manager
        this.stack = new ArrayDeque<>();
    }

    @Override
    public void stateChanged(final int pos, final PageState newState) {
        if (newState == PageState.UNPINNED) {
            this.stack.push(pos);
        } else {
            // no duplicates, so we can stop after the first hit
            this.stack.removeFirstOccurrence(pos);
        }
    }

    @Override
    public int pickVictim() {
        return this.stack.isEmpty() ? -1 : this.stack.peek();
    }
}
