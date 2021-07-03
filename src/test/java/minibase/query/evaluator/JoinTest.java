package minibase.query.evaluator;

import minibase.access.file.FileScan;
import minibase.access.file.HeapFile;
import minibase.util.Convert;
import org.junit.Test;

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

            // print the sailors
            while (sailorScan.hasNext()) {
                final byte[] nextTuple = sailorScan.next();
                System.out.print(Convert.readInt(nextTuple, 0) + "\t");
                System.out.print(Convert.readString(nextTuple, 4, 50) + "  \t");
                System.out.print(Convert.readInt(nextTuple, 54) + "\t");
                System.out.println(Convert.readFloat(nextTuple, 58));
            }

            // print the reserves
            while (reservesScan.hasNext()) {
                final byte[] nextTuple = reservesScan.next();
                System.out.print(Convert.readInt(nextTuple, 0) + "\t");
                System.out.print(Convert.readInt(nextTuple, 4) + "\t");
                System.out.print(Convert.readDate(nextTuple, 8) + "\t");
                System.out.println(Convert.readString(nextTuple, 11, 50) + "\t");
            }
            sailorScan.close();
            reservesScan.close();

            final SortMergeEquiJoin sortedMergeJoinResult = new SortMergeEquiJoin(new TableScan(S_RESERVES, reserves),
                    0, new TableScan(S_SAILORS, sailors), 0, this.getBufferManager());
            try (TupleIterator iterator = sortedMergeJoinResult.open()) {
                System.out.println("------------------------------");
                while (iterator.hasNext()) {
                    final byte[] nextTuple = iterator.next();

                    System.out.print(Convert.readInt(nextTuple, 0) + "\t");
                    System.out.print(Convert.readInt(nextTuple, 4) + "  \t");
                    System.out.print(Convert.readDate(nextTuple, 8) + "\t");
                    System.out.print(Convert.readString(nextTuple, 11, 50) + "\t");

                    System.out.print(Convert.readInt(nextTuple, 61) + "\t");
                    System.out.print(Convert.readString(nextTuple, 65, 50) + "\t");
                    System.out.print(Convert.readInt(nextTuple, 115) + "\t");
                    System.out.println(Convert.readFloat(nextTuple, 119) + "\t");
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
