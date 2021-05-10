package dbsai.asg04;

/**
 * #TODO add comment.
 *
 * @author #TODO authors
 */
public final class BTree extends AbstractBTree {

    /**
     * Creates a new B+-Tree with the given branching factor.
     * @param d branching factor
     */
    public BTree(final int d) {
        super(d);
    }

    @Override
    protected boolean containsKey(final int nodeID, final int key) {
        // TODO implement this
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    protected long insertKey(final int nodeID, final int key) {
        // TODO implement this
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    protected boolean deleteKey(final int nodeID, final int key) {
        // TODO implement this
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
