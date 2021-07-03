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
    private int outerOffset;

    /**
     * Innner input relation.
     */
    private final Operator inner;

    /**
     * Offset of a column in the inner relation’s schema.
     */
    private int innerOffset;

    /**
     * Buffer Manager used.
     */
    private final BufferManager bufferManager;


    /**
     * Join predicate used.
     */
    private final RecordComparator comparator;

    /**
     *
     */
    private final int bufferpages = 6;

    /**
     *
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
            //TODO TBC
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

                final byte[] prevInner = currentInner.clone();
                final LinkedList<byte[]> innerList = new LinkedList<>();


            }


        };
    }
}
