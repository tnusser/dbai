package dbsai.asg04;

import java.util.Arrays;

/**
 * Sample solution for the B+ Tree.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public final class BTreeGroup99Optimized extends AbstractBTree {
    /**
     * Constructor specifying the degree {@code d} of the B+ Tree.
     *
     * @param d degree of the tree
     */
    public BTreeGroup99Optimized(final int d) {
        super(d);
    }

    @Override
    protected boolean containsKey(final int nodeID, final int key) {
        // iterative version of the lookup algorithm
        Node node = this.getNode(nodeID);
        while (!node.isLeaf()) {
            node = this.getNode(node.getChildID(Math.abs(search(node, key) + 1)));
        }
        return search(node, key) >= 0;
    }

    @Override
    protected long insertKey(final int nodeID, final int key) {
        // keep track of the parent for rotations
        return this.insertKey(nodeID, key, -1, 0, 0);
    }

    /**
     * Helper method of {@link #insertKey(int, int)} which keeps track of the parent node
     * in order to be able to balance the tree and avoid page splitting.
     *
     * @param nodeID     ID of the node to insert into
     * @param key        key to insert
     * @param parentID   ID of this node's parent, {@code -1} if {@code nodeID} is the root node's ID
     * @param parentPos  position of {@code nodeID} in the parent, {@code 0} if ther eis no parent
     * @param parentSize size of the parent node, {@code 0} if there is no parent
     * @return if the node was split while inserting the given key, the return
     * key is a pair (as created by {@link #keyIDPair(int, int)}) of the
     * key between the old and the new node that has to be inserted into
     * the parent and the ID of the newly created node, otherwise
     * {@link #NO_CHANGES} is returned
     */
    private long insertKey(final int nodeID, final int key,
                           final int parentID, final int parentPos, final int parentSize) {
        final Node node = this.getNode(nodeID);
        final int nodeSize = node.getSize();
        if (node.isLeaf()) {
            final int pos = -search(node, key) - 1;
            if (pos < 0) {
                // key is already contained
                return NO_CHANGES;
            }

            // adjust the stored size
            incrementSize();

            if (nodeSize < this.getMaxSize()) {
                insert(node.getKeys(), pos, key);
                node.setSize(nodeSize + 1);
                return NO_CHANGES;
            }

            if (parentPos > 0) {
                final Node parent = this.getNode(parentID);
                final int leftID = parent.getChildID(parentPos - 1);
                final Node left = this.getNode(leftID);
                final int leftSize = left.getSize();
                final int move = (this.getMaxSize() - leftSize + 1) / 2;
                if (move > 0) {
                    // push keys to the left node
                    if (pos == 0 && move == 1) {
                        // no rotation, entry goes to the left page
                        insert(left.getKeys(), leftSize, key);
                        parent.setKey(parentPos - 1, node.getKey(0));
                    } else if (pos < move) {
                        this.rotateLeft(parent, parentPos - 1, left, node, move - 1);
                        insert(left.getKeys(), leftSize + pos, key);
                    } else if (pos == move) {
                        this.rotateLeft(parent, parentPos - 1, left, node, move);
                        insert(node.getKeys(), 0, key);
                        parent.setKey(parentPos - 1, key);
                    } else {
                        this.rotateLeft(parent, parentPos - 1, left, node, move);
                        insert(node.getKeys(), pos - move, key);
                    }
                    left.setSize(leftSize + move);
                    node.setSize(nodeSize - move + 1);
                    return NO_CHANGES;
                }
            }

            if (parentPos < parentSize) {
                final Node parent = this.getNode(parentID);
                final int rightID = parent.getChildID(parentPos + 1);
                final Node right = this.getNode(rightID);
                final int rightSize = right.getSize();
                final int move = (this.getMaxSize() - rightSize + 1) / 2;
                if (move > 0) {
                    // push keys to the right node
                    final int newSize = nodeSize - move + 1;
                    if (pos < newSize) {
                        this.rotateRight(parent, parentPos, node, right, move);
                        insert(node.getKeys(), pos, key);
                    } else if (pos == newSize) {
                        this.rotateRight(parent, parentPos, node, right, move - 1);
                        insert(right.getKeys(), 0, key);
                        parent.setKey(parentPos, key);
                    } else {
                        this.rotateRight(parent, parentPos, node, right, move - 1);
                        insert(right.getKeys(), pos - newSize, key);
                    }
                    right.setSize(rightSize + move);
                    node.setSize(nodeSize - move + 1);
                    return NO_CHANGES;
                }
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
        final long result = this.insertKey(node.getChildID(pos), key, nodeID, pos, nodeSize);

        if (result == NO_CHANGES) {
            // nothing to do
            return NO_CHANGES;
        }

        final int midKey = getMidKey(result);
        final int childID = getChildID(result);

        if (nodeSize < getMaxSize()) {
            // there is space left, insert the middle key and the child reference
            insert(node.getKeys(), pos, midKey);
            insert(node.getChildren(), pos + 1, childID);
            node.setSize(nodeSize + 1);
            return NO_CHANGES;
        }

        if (parentPos > 0) {
            // check if the left neighbor has free space
            final Node parent = this.getNode(parentID);
            final int leftID = parent.getChildID(parentPos - 1);
            final Node left = this.getNode(leftID);
            final int leftSize = left.getSize();
            final int move = (this.getMaxSize() - leftSize + 1) / 2;
            if (move > 0) {
                // there's space left, rotate keys over
                if (pos < move - 1) {
                    // the new entry goes into the left node
                    this.rotateLeft(parent, parentPos - 1, left, node, move - 1);
                    insert(left.getKeys(), leftSize + pos + 1, midKey);
                    insert(left.getChildren(), leftSize + pos + 2, childID);
                } else if (pos == move - 1) {
                    // the new entry goes into the middle
                    this.rotateLeft(parent, parentPos - 1, left, node, move);
                    insert(node.getKeys(), 0, parent.getKey(parentPos - 1));
                    parent.setKey(parentPos - 1, midKey);
                    insert(node.getChildren(), 0, childID);
                } else {
                    // the new entry goes into the right node
                    this.rotateLeft(parent, parentPos - 1, left, node, move);
                    insert(node.getKeys(), pos - move, midKey);
                    insert(node.getChildren(), pos - move + 1, childID);
                }
                node.setSize(nodeSize - move + 1);
                left.setSize(leftSize + move);
                return NO_CHANGES;
            }
        }

        if (parentPos < parentSize) {
            // check if the right neighbor has free space
            final Node parent = this.getNode(parentID);
            final int rightID = parent.getChildID(parentPos + 1);
            final Node right = this.getNode(rightID);
            final int rightSize = right.getSize();
            final int move = (this.getMaxSize() - rightSize + 1) / 2;
            if (move > 0) {
                // there's space left, rotate keys over
                final int newSize = nodeSize - move + 1;
                if (pos < newSize) {
                    // the new entry goes into the left node
                    this.rotateRight(parent, parentPos, node, right, move);
                    insert(node.getKeys(), pos, midKey);
                    insert(node.getChildren(), pos + 1, childID);
                } else if (pos == newSize) {
                    // the new entry goes into the middle
                    this.rotateRight(parent, parentPos, node, right, move - 1);
                    insert(right.getKeys(), 0, parent.getKey(parentPos));
                    parent.setKey(parentPos, midKey);
                    insert(right.getChildren(), 0, childID);
                } else {
                    // the new entry goes into the right node
                    this.rotateRight(parent, parentPos, node, right, move - 1);
                    insert(right.getKeys(), pos - newSize - 1, midKey);
                    insert(right.getChildren(), pos - newSize, childID);
                }
                node.setSize(newSize);
                right.setSize(rightSize + move);
                return NO_CHANGES;
            }
        }

        // we have to split the inner node
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
        final int childSize = child.getSize();

        // We try filling the underflowing leaf first because that doesn't
        // cascade.

        // can we steal an entry from the left sibling?
        if (childPos > 0) {
            final Node left = getNode(node.getChildID(childPos - 1));
            final int leftSize = left.getSize();
            if (leftSize > getMinSize()) {
                rotateRight(node, childPos - 1, left, child, (leftSize - childSize + 1) / 2);
                return true;
            }
        }

        // ...or from the right sibling?
        if (childPos < nodeSize) {
            final Node right = getNode(node.getChildID(childPos + 1));
            final int rightSize = right.getSize();
            if (rightSize > getMinSize()) {
                rotateLeft(node, childPos, child, right, (rightSize - childSize + 1) / 2);
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
     * Rotates {@code n} entries from the child node {@code left} to {@code right},
     * where {@code left} is the child node at position {@code leftPos} in {@code node}.
     *
     * @param node    parent node
     * @param leftPos position of the left child node
     * @param left    left child node
     * @param right   right child node, at position {@code leftPos + 1}
     * @param n       number of entries to move to the left
     */
    private void rotateLeft(final Node node, final int leftPos, final Node left, final Node right, final int n) {
        if (n > 0) {
            final int leftSize = left.getSize();
            final int rightSize = right.getSize();
            if (left.isLeaf()) {
                // insert keys into the left node
                System.arraycopy(right.getKeys(), 0, left.getKeys(), leftSize, n);
                // set new middle key
                node.setKey(leftPos, right.getKey(n));
            } else {
                // insert keys into the left node
                left.setKey(leftSize, node.getKey(leftPos));
                System.arraycopy(right.getKeys(), 0, left.getKeys(), leftSize + 1, n - 1);
                System.arraycopy(right.getChildren(), 0, left.getChildren(), leftSize + 1, n);
                // set new middle key
                node.setKey(leftPos, right.getKey(n - 1));
                // remove child IDs from the right node
                System.arraycopy(right.getChildren(), n, right.getChildren(), 0, rightSize - n + 1);
            }
            // remove keys from the right node
            System.arraycopy(right.getKeys(), n, right.getKeys(), 0, rightSize - n);
            left.setSize(leftSize + n);
            right.setSize(rightSize - n);
        }
    }

    /**
     * Rotates {@code n} entries from the child node {@code right} to {@code left},
     * where {@code left} is the child node at position {@code leftPos} in {@code node}.
     *
     * @param node    parent node
     * @param leftPos position of the left child node
     * @param left    left child node
     * @param right   right child node, at position {@code leftPos + 1}
     * @param n       number of entries to move to the right
     */
    private void rotateRight(final Node node, final int leftPos, final Node left, final Node right, final int n) {
        if (n > 0) {
            final int leftSize = left.getSize();
            final int rightSize = right.getSize();
            // set new middle key
            final int midKey = node.getKey(leftPos);
            node.setKey(leftPos, left.getKey(leftSize - n));
            // move keys in right node to make space
            System.arraycopy(right.getKeys(), 0, right.getKeys(), n, rightSize);
            if (left.isLeaf()) {
                // get all keys from the left node
                System.arraycopy(left.getKeys(), leftSize - n, right.getKeys(), 0, n);
            } else {
                // move child IDs in right node to make space
                System.arraycopy(right.getChildren(), 0, right.getChildren(), n, rightSize + 1);
                // get all but one keys from the left node and the last one from the middle
                System.arraycopy(left.getKeys(), leftSize - n + 1, right.getKeys(), 0, n - 1);
                right.setKey(n - 1, midKey);
                // get child IDs from the left node
                System.arraycopy(left.getChildren(), leftSize + 1 - n, right.getChildren(), 0, n);
            }
            left.setSize(leftSize - n);
            right.setSize(rightSize + n);
        }
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
