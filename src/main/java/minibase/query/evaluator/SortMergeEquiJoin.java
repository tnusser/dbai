package minibase.query.evaluator;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import minibase.query.schema.Schema;
import minibase.storage.buffer.BufferManager;
import minibase.query.evaluator.compare.RecordComparator;
import minibase.query.evaluator.compare.TupleComparator;


/**
 * @author Tobias Nusser, Andreas Bäuerle
 */
public class SortMergeEquiJoin extends AbstractOperator {

    /**
     * Outer input relation.
     */
    private final Operator outer;

    /**
     * Offset of a column in the outer relation’s schema.
     */
    private final int outerOffset;

    /**
     * Inner input relation.
     */
    private final Operator inner;

    /**
     * Offset of a column in the inner relation’s schema.
     */
    private final int innerOffset;

    /**
     * Buffer Manager used.
     */
    private final BufferManager bufferManager;


    /**
     * Join predicate used.
     */
    private final RecordComparator comparator;

    /**
     * Number of buffer pages used for creating initial runs.
     */
    private final int bufferpages = 6;

    /**
     * Maximum number of runs that are merged simultaneously.
     */
    private final int k = 2;


    /**
     * @param outer Outer input relation.
     * @param outerOffset Offset of a column in the outer relation’s schema.
     * @param inner Inner input relation.
     * @param innerOffset Offset of a column in the inner relation’s schema.
     * @param bufferManager The BufferManager used.
     */

    SortMergeEquiJoin(final Operator outer, final int outerOffset,
                        final Operator inner, final int innerOffset, final BufferManager bufferManager) {
        super(Schema.join(outer.getSchema(), inner.getSchema()));
        this.outer = outer;
        this.outerOffset = outerOffset;

        this.inner = inner;
        this.innerOffset = innerOffset;

        this.bufferManager = bufferManager;

        this.comparator = new TupleComparator(outer.getSchema(),
                          new int[] { outerOffset },
                          inner.getSchema(),
                          new int[] { innerOffset });
    }

    @Override
    public TupleIterator open() {

        // Sort both outer and inner relation
        final TupleIterator sortedOuter = new ExternalSort(bufferManager, outer,
                new TupleComparator(outer.getSchema(), new int[] {outerOffset},
                        new boolean[] { true }), bufferpages, k).open();

        final TupleIterator sortedInner = new ExternalSort(bufferManager, inner,
                new TupleComparator(inner.getSchema(), new int[] {innerOffset},
                        new boolean[] { true }), bufferpages, k).open();


        if (!sortedOuter.hasNext()) {
            sortedOuter.close();
            return TupleIterator.EMPTY;
        }

        if (!sortedInner.hasNext()) {
            sortedInner.close();
            sortedOuter.close();
            return TupleIterator.EMPTY;
        }

        return new TupleIterator() {
            /** Current tuples from the iterator of the outer and, respectively, the inner Relation. */
            private byte[] currentOuter = sortedOuter.next();
            private byte[] currentInner = sortedInner.next();
            private final ConcurrentLinkedQueue<byte[]> adjacentTuple = new ConcurrentLinkedQueue<>();

            @Override
            public boolean hasNext() {
                if (!adjacentTuple.isEmpty()) {
                    return true;
                }
                if (currentOuter == null || currentInner == null) {
                    return false;
                }

                while (SortMergeEquiJoin.this.comparator.lessThan(currentOuter, currentInner)) {
                    if (sortedOuter.hasNext()) {
                        currentOuter = sortedOuter.next();
                    } else {
                        return false;
                    }
                }

                while (SortMergeEquiJoin.this.comparator.greaterThan(currentOuter, currentInner)) {
                    if (sortedInner.hasNext()) {
                        currentInner = sortedInner.next();
                    } else {
                        return false;
                    }
                }

                final LinkedList<byte[]> tempInnerList = new LinkedList<>();
                final byte[] priorInner = currentInner.clone();

                // iterate through outer schema
                while (SortMergeEquiJoin.this.comparator.equals(currentOuter, priorInner)) {
                    currentInner = priorInner;
                    // iterate through inner schema
                    while (SortMergeEquiJoin.this.comparator.equals(currentOuter, currentInner)) {
                        adjacentTuple.add(Schema.join(currentOuter, currentInner));
                        tempInnerList.addLast(currentInner);

                        if (sortedInner.hasNext()) {
                            currentInner = sortedInner.next();
                        } else {
                            currentInner = null;
                            return true;
                        }
                    }
                    if (sortedOuter.hasNext()) {
                        currentOuter = sortedOuter.next();
                        while (sortedOuter.hasNext() && SortMergeEquiJoin.this.comparator.equals(
                                currentOuter, priorInner)) {
                            for (byte[] tempInnerVal : tempInnerList) {
                                adjacentTuple.add(Schema.join(currentOuter, tempInnerVal));
                                currentOuter = sortedOuter.next();
                            }
                        }
                        // reset tempInnerList
                        tempInnerList.clear();
                    } else {
                        currentOuter = null;
                        return !adjacentTuple.isEmpty();
                    }
                }
                return !adjacentTuple.isEmpty();
            }
            public byte[] next() {
                // validate the next tuple
                if (!hasNext()) {
                    close();
                    throw new NoSuchElementException("No more tuples to return.");
                }
                // return head of queue
                return adjacentTuple.poll();
            }

            @Override
            public void reset() {
                // reset iterator and temp arrays
                sortedOuter.reset();
                sortedInner.reset();
                currentOuter = null;
                currentInner = null;
                adjacentTuple.clear();
            }

            @Override
            public void close() {
                // close iterator and temp arrays
                sortedOuter.close();
                sortedInner.close();
                currentOuter = null;
                currentInner = null;
                adjacentTuple.clear();
            }
        };
    }
}
