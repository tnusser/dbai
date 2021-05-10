package dbsai.asg02;

/**
 * Simple test class illustrating just-in-time optimization in the JVM.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 */
public final class PerformanceTest {

    private PerformanceTest() {
    }

    /**
     * Main method.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        for (int i = 0; i < 100; i++) {
            final long time = System.nanoTime();
            for (int j = 0; j < 100_000_000; j++) {
                Math.sqrt(j);
            }
            System.out.println(System.nanoTime() - time);
        }
    }
}
