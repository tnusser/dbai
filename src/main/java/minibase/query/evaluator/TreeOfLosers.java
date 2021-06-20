package minibase.query.evaluator;

import java.util.ArrayList;

import minibase.storage.buffer.BufferManager;
import minibase.access.file.Run;
import minibase.access.file.RunPage;
import minibase.access.file.RunScan;
import minibase.query.evaluator.compare.RecordComparator;
import minibase.storage.buffer.Page;
import minibase.storage.buffer.PageID;


public class TreeOfLosers implements TupleIterator {

    /**
     * Runs from ExternalSort.
     */
    private final ArrayList<TupleIterator> scanRuns;

    /**
     * Flag to prevent operations on already closed runs.
     */
    private boolean closed;

    /**
     * Comparator with which to compare the records with.
     */
    private final RecordComparator comparator;

    /**
     * Instance of buffermanager that we are using.
     */
    private final BufferManager bufferManager;

    /**
     * List of all considered runs. Used to properly close all of them.
     */
    private final ArrayList<Run> listOfRuns;

    /**
     * Root of TupleIterator. Used as a starting point for traversal.
     */
    private TupleIterator root;

    TreeOfLosers(final ArrayList<Run> listOfRuns,
                 final RecordComparator recordComparator,
                 final BufferManager bufferManager) {
        this.listOfRuns = listOfRuns;
        this.scanRuns = new ArrayList<>();
        this.bufferManager = bufferManager;
        this.closed = false;
        this.comparator = recordComparator;

        for (int index = 0; index < listOfRuns.size(); index++) {
            this.scanRuns.add(new RunScan(this.bufferManager, listOfRuns.get(index)));
        }

        reset();
    }

    @Override
    public boolean hasNext() {
        if (closed) {
            throw new IllegalStateException("Has already been closed");
        }
        return root.hasNext();
    }

    @Override
    public byte[] next() {
        if (closed) {
            throw new IllegalStateException("Has already been closed");
        }
        return root.next();
    }

    @Override
    public void reset() {
        if (closed) {
            throw new IllegalStateException("Has already been closed");
        }
        for (int index = 0; index < scanRuns.size(); index++) {
            scanRuns.get(index).reset();
        }

        // Building Tree, generate new Layer
        TupleIterator[] layer = scanRuns.toArray(new TupleIterator[0]);
        while (layer.length > 1) {
            final int halfOfLayerLength = layer.length / 2;
            final TupleIterator[] nextLayer = new TupleIterator[halfOfLayerLength];

            // fill next Layer Array to generate Nodes from
            for (int i = 0; i < halfOfLayerLength; ++i) {
                nextLayer[i] = new NodeIterator(layer[i * 2], layer[i * 2 + 1], comparator);
            }

            //Generate new nodes
            if (layer.length % 2 == 1) {
                final int position = nextLayer.length - 1;
                nextLayer[position] = new NodeIterator(nextLayer[position], layer[layer.length - 1], comparator);
            }
            layer = nextLayer;
        }
        root = layer[0];
    }

    @Override
    public void close() {
        // Check if run has been closed before
        if (closed) {
            throw new IllegalStateException("Has already been closed");
        }
        closed = true;

        for (int index = 0; index < scanRuns.size(); index++) {
            scanRuns.get(index).close();
        }

        // Close all of the considered runs.
        // For all Pages: pin the current page considered, get ID of next page, unpin current page
        // Repeat for all  following pages
        for (int index = 0; index < listOfRuns.size(); index++) {
            Page<RunPage> currentPage = bufferManager.pinPage(listOfRuns.get(index).getFirstPageID());
            PageID nextPageID = RunPage.getNextID(currentPage);
            bufferManager.freePage(currentPage);
            // As long as a next page is available, continue with next page.
            while (nextPageID != PageID.INVALID) {
                currentPage = bufferManager.pinPage(nextPageID);
                nextPageID = RunPage.getNextID(currentPage);
                bufferManager.freePage(currentPage);
            }
        }
    }

    private class NodeIterator implements TupleIterator {

        /**
         * Current loser record of subtree.
         */
        private byte[] loserSubtree = null;

        /**
         * Flag if loser comes from @leftSubtree or @rightSubtree.
         */
        private boolean loserRight;

        /**
         * Participants of left subtree.
         */
        private final TupleIterator leftSubtree;

        /**
         * Participants of right subtree.
         */
        private final TupleIterator rightSubtree;

        NodeIterator(final TupleIterator left, final TupleIterator right, final RecordComparator comparator) {
            this.leftSubtree = left;
            this.rightSubtree = right;
        }

        @Override
        public boolean hasNext() {
            if (this.loserSubtree != null) {
                return true;
            }
            return leftSubtree.hasNext() || rightSubtree.hasNext();
        }

        @Override
        public byte[] next() {
            if (loserSubtree != null) {
                if (!loserRight) {
                    if (rightSubtree.hasNext()) {
                        final byte[] current = rightSubtree.next();
                        if (comparator.lessThan(loserSubtree, current)) {
                            final byte[] prevLoserSubtree = loserSubtree;
                            loserSubtree = current;
                            loserRight = true;
                            return prevLoserSubtree;
                        } else {
                            return current;
                        }
                    } else {
                        final byte[] prevLoserSubtree = loserSubtree;
                        if (leftSubtree.hasNext()) {
                            loserSubtree = leftSubtree.next();
                            loserRight = false;
                        } else {
                            loserSubtree = null;
                        }
                        return prevLoserSubtree;
                    }
                } else {
                    if (leftSubtree.hasNext()) {
                        final byte[] current = leftSubtree.next();
                        if (comparator.lessThan(current, loserSubtree)) {
                            return current;
                        } else {
                            final byte[] prevLoserSubtree = loserSubtree;
                            loserSubtree = current;
                            loserRight = false;
                            return prevLoserSubtree;
                        }
                    } else {
                        final byte[] prevLoserSubtree = loserSubtree;
                        if (rightSubtree.hasNext()) {
                            loserSubtree = rightSubtree.next();
                            loserRight = true;
                        } else {
                            loserSubtree = null;
                        }
                        return prevLoserSubtree;
                    }
                }
            } else {
                if (!rightSubtree.hasNext()) {
                    final byte[] current = leftSubtree.next();
                    if (leftSubtree.hasNext()) {
                        loserSubtree = leftSubtree.next();
                    }
                    loserRight = false;
                    return current;
                } else if (!leftSubtree.hasNext()) {
                    final byte[] current = rightSubtree.next();
                    if (rightSubtree.hasNext()) {
                        loserSubtree = rightSubtree.next();
                    }
                    loserRight = true;
                    return current;
                } else {
                    final byte[] currentLeft = leftSubtree.next();
                    final byte[] currentRight = rightSubtree.next();
                    if (comparator.lessThan(currentLeft, currentRight)) {
                        loserSubtree = currentRight;
                        loserRight = true;
                        return currentLeft;
                    } else {
                        loserSubtree = currentLeft;
                        loserRight = false;
                        return currentRight;
                    }
                }
            }
        }

        // TODO Do we need that?
        @Override
        public void reset() {
        }

        // TODO Do we need that?
        @Override
        public void close() {
        }
    }
}
