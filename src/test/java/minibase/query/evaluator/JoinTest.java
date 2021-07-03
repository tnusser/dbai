package minibase.query.evaluator;

import minibase.access.file.FileScan;
import minibase.access.file.HeapFile;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class JoinTest extends EvaluatorBaseTest {

    @Test
    public void testPrint() {
        // test join relation with empty relation
        try (
                HeapFile sailors = (HeapFile) createSailors(2500);
                HeapFile reserves = (HeapFile) createReserves(100, 2500, 600)
        ) {

            final FileScan sailorScan = sailors.openScan();
            final FileScan reservesScan = reserves.openScan();

            // printSailor(sailorScan);
            // printReservations(reservesScan);

            sailorScan.close();
            reservesScan.close();

            final SortMergeEquiJoin sortedMergeJoinResult = new SortMergeEquiJoin(new TableScan(S_RESERVES, reserves),
                    0, new TableScan(S_SAILORS, sailors), 0, this.getBufferManager());
            try (TupleIterator iterator = sortedMergeJoinResult.open()) {
                //final int count = this.printReservationSMJSailors(iterator);
                //assertEquals(count, 100);
                //System.out.println("# matches: " + count);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
