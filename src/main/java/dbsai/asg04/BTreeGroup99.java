package dbsai.asg04;

import java.util.Arrays;

/**
 * Sample solution for the B+ Tree.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public final class BTreeGroup99 extends AbstractBTree {
    /**
     * Constructor specifying the degree {@code d} of the B+ Tree.
     *
     * @param d degree of the tree
     */
    public BTreeGroup99(final int d) {
        super(d);
    }

    @Override
    protected boolean containsKey(final int nodeID, final int key) {
        final Node node = this.getNode(nodeID);
        final int pos = search(node, key);
        return node.isLeaf() ? pos >= 0 : this.containsKey(node.getChildID(Math.abs(pos + 1)), key);
    }

    @Override
    protected long insertKey(final int nodeID, final int key) {
        final Node node = this.getNode(nodeID);

        // leaf node
        if (node.isLeaf()) {
            final int pos = -search(node, key) - 1;

            if (pos < 0) {
                // key already exists
                return NO_CHANGES;
            }

            // adjust the stored size
            incrementSize();

            if (node.getSize() < getMaxSize()) {
                // there is space left, insert key
                insert(node.getKeys(), pos, key);
                node.setSize(node.getSize() + 1);
                return NO_CHANGES;
            }

            // no space left, split leaf
            final int rightID = createNode(true);
            final Node right = getNode(rightID);
            final int minSize = getMinSize();
            System.arraycopy(node.getKeys(), minSize, right.getKeys(), 0, minSize);
            right.setSize(minSize);
            // truncate the first node
            node.setSize(minSize);

            if (pos < minSize) {
                // insert into the left node
                insert(node.getKeys(), pos, key);
                node.setSize(node.getSize() + 1);
            } else {
                // insert into right node, at corrected position
                insert(right.getKeys(), pos - minSize, key);
                right.setSize(right.getSize() + 1);
            }

            return keyIDPair(right.getKey(0), rightID);
        }
        // inner node - look for child in which to insert
        final int pos = Math.abs(search(node, key) + 1);
        final long result = this.insertKey(node.getChildID(pos), key);

        if (result == NO_CHANGES) {
            // nothing to do
            return NO_CHANGES;
        }

        final int midKey = getMidKey(result);
        final int childID = getChildID(result);

        if (node.getSize() < getMaxSize()) {
            // there is space left, insert the middle key and the child reference
            insert(node.getKeys(), pos, midKey);
            insert(node.getChildren(), pos + 1, childID);
            node.setSize(node.getSize() + 1);
            return NO_CHANGES;
        }

        // the node is full, we have to split the inner node
        final int rightID = createNode(false);
        final Node right = getNode(rightID);
        final int minSize = this.getMinSize();

        final int newMidKey;
        if (pos < minSize) {
            // the child is inserted into the left node
            newMidKey = node.getKey(minSize - 1);
            System.arraycopy(node.getKeys(), minSize, right.getKeys(), 0, minSize);
            System.arraycopy(node.getChildren(), minSize, right.getChildren(), 0, minSize + 1);
            insert(node.getKeys(), pos, midKey);
            insert(node.getChildren(), pos + 1, childID);
        } else if (pos == minSize) {
            // the child is inserted into the middle
            newMidKey = midKey;
            right.setChildID(0, childID);
            System.arraycopy(node.getKeys(), minSize, right.getKeys(), 0, minSize);
            System.arraycopy(node.getChildren(), minSize + 1, right.getChildren(), 1, minSize);
        } else {
            // the child is inserted into the right node
            newMidKey = node.getKey(minSize);
            System.arraycopy(node.getKeys(), minSize + 1, right.getKeys(), 0, minSize - 1);
            System.arraycopy(node.getChildren(), minSize + 1, right.getChildren(), 0, minSize);
            insert(right.getKeys(), pos - minSize - 1, midKey);
            insert(right.getChildren(), pos - minSize, childID);
        }
        right.setSize(minSize);
        node.setSize(minSize);

        return keyIDPair(newMidKey, rightID);
    }

    @Override
    protected boolean deleteKey(final int nodeID, final int key) {
        final Node node = getNode(nodeID);
        final int nodeSize = node.getSize();
        final int pos = search(node, key);

        // leaf node
        if (node.isLeaf()) {
            if (pos < 0) {
                // nothing to remove
                return true;
            }

            // node is found, so we delete it
            delete(node.getKeys(), pos);
            node.setSize(nodeSize - 1);
            // adjust the stored size
            decrementSize();

            return nodeSize > getMinSize();
        }

        // inner node, find key in keys and delete
        final int childPos = Math.abs(pos + 1);
        final int childID = node.getChildID(childPos);

        if (this.deleteKey(childID, key)) {
            // No changes to tree structure in children, we're done.
            return true;
        }

        // We have to merge the child node
        final Node child = getNode(childID);

        // We try filling the underflowing leaf first because that doesn't cascade.

        // can we steal an entry from the left sibling?
        if (childPos > 0) {
            final Node left = getNode(node.getChildID(childPos - 1));
            final int leftSize = left.getSize();
            if (leftSize > getMinSize()) {
                final int rightSize = child.getSize();
                final int midKey = node.getKey(childPos - 1);
                node.setKey(childPos - 1, left.getKey(leftSize - 1));
                System.arraycopy(child.getKeys(), 0, child.getKeys(), 1, rightSize);
                if (left.isLeaf()) {
                    child.setKey(0, left.getKey(leftSize - 1));
                } else {
                    System.arraycopy(child.getChildren(), 0, child.getChildren(), 1, rightSize + 1);
                    child.setKey(0, midKey);
                    child.setChildID(0, left.getChildID(leftSize));
                }
                left.setSize(leftSize - 1);
                child.setSize(rightSize + 1);
                return true;
            }
        }

        // ...or from the right sibling?
        if (childPos < nodeSize) {
            final Node right = getNode(node.getChildID(childPos + 1));
            final int rightSize = right.getSize();
            if (rightSize > getMinSize()) {
                final int leftSize = child.getSize();
                if (child.isLeaf()) {
                    child.setKey(leftSize, right.getKey(0));
                    node.setKey(childPos, right.getKey(1));
                    System.arraycopy(right.getKeys(), 1, right.getKeys(), 0, rightSize - 1);
                } else {
                    child.setKey(leftSize, node.getKey(childPos));
                    child.setChildID(leftSize + 1, right.getChildID(0));
                    node.setKey(childPos, right.getKey(0));
                    System.arraycopy(right.getKeys(), 1, right.getKeys(), 0, rightSize - 1);
                    System.arraycopy(right.getChildren(), 1, right.getChildren(), 0, rightSize);
                }
                child.setSize(leftSize + 1);
                right.setSize(rightSize - 1);
                return true;
            }
        }

        // we need to cascade, merge and delete the node
        if (childPos > 0) {
            // merge with left sibling
            mergeChildren(node, childPos - 1);
        } else if (childPos < nodeSize) {
            // merge with right sibling
            mergeChildren(node, childPos);
        } else {
            // we're the root node and there's no more sibling,
            // make the under-full child node the root
            setRoot(childID);
            removeNode(nodeID);
            return true;
        }

        node.setSize(nodeSize - 1);
        return nodeSize > getMinSize();
    }

    /**
     * Merges the two child nodes at positions {@code leftPos} and
     * {@code leftPos + 1} into the left one and deletes the right one.
     *
     * @param node    the parent node
     * @param leftPos position of the left child to merge
     */
    private void mergeChildren(final Node node, final int leftPos) {
        final Node left = this.getNode(node.getChildID(leftPos));
        final Node right = this.getNode(node.getChildID(leftPos + 1));
        final int leftSize = left.getSize();
        final int rightSize = right.getSize();
        if (left.isLeaf()) {
            // just concatenate the key arrays
            System.arraycopy(right.getKeys(), 0, left.getKeys(), leftSize, rightSize);
            left.setSize(leftSize + rightSize);
        } else {
            // we have to stick the middle key from the parent back in
            left.setKey(leftSize, node.getKey(leftPos));
            System.arraycopy(right.getKeys(), 0, left.getKeys(), leftSize + 1, rightSize);
            // ...and merge the child-pointer arrays
            System.arraycopy(right.getChildren(), 0, left.getChildren(), leftSize + 1, rightSize + 1);
            left.setSize(leftSize + 1 + rightSize);
        }
        this.removeNode(node.getChildID(leftPos + 1));
        delete(node.getKeys(), leftPos);
        delete(node.getChildren(), leftPos + 1);
    }

    /**
     * Searches the given node's key array for a given key.
     *
     * @param nd  node
     * @param key key to look for
     * @return index of the search key, if it is contained in the array;
     * otherwise, {@code (-(insertion point) - 1)}. The insertion point
     * is defined as the point at which the key would be inserted into
     * the array: the index of the first element in the range greater
     * than the key, or toIndex if all elements in the range are less
     * than the specified key. Note that this guarantees that the return
     * key will be {@code >= 0} if and only if the key is found.
     */
    private static int search(final Node nd, final int key) {
        return Arrays.binarySearch(nd.getKeys(), 0, nd.getSize(), key);
    }

    /**
     * Inserts a key at a given position into an array.
     *
     * @param arr array
     * @param pos position
     * @param key key
     */
    private static void insert(final int[] arr, final int pos, final int key) {
        System.arraycopy(arr, pos, arr, pos + 1, arr.length - pos - 1);
        arr[pos] = key;
    }

    /**
     * Deletes the key at a given position in the array.
     *
     * @param arr array
     * @param pos position
     */
    private static void delete(final int[] arr, final int pos) {
        System.arraycopy(arr, pos + 1, arr, pos, arr.length - pos - 1);
    }
}
